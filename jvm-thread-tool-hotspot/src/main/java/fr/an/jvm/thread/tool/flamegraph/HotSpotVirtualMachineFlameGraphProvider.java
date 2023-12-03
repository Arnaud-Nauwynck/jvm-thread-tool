package fr.an.jvm.thread.tool.flamegraph;


import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fr.an.jvm.thread.tool.flamegraph.model.FlameGraphNodeDTO;
import fr.an.jvm.thread.tool.utils.FlameGraphAccumulator;
import fr.an.jvm.thread.tool.utils.HotspotVirtualMachineThreadDumpProvider;
import fr.an.jvm.thread.tool.utils.HotspotVirtualMachineUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import sun.tools.attach.HotSpotVirtualMachine;

@Slf4j
public class HotSpotVirtualMachineFlameGraphProvider {

    @Getter
    private int pid;

    private HotSpotVirtualMachine vm;

    private ScheduledExecutorService scheduledExecutor;

    private FlameGraphAccumulator acc;
    private ScheduledFuture<?> schedule;

    // ------------------------------------------------------------------------

    public HotSpotVirtualMachineFlameGraphProvider(int pid, ScheduledExecutorService scheduledExecutor) {
        this.pid = pid;
        this.scheduledExecutor = scheduledExecutor;
    }

    // ------------------------------------------------------------------------

    public void init() {
        this.vm = HotspotVirtualMachineUtils.attachVM(pid);
        this.acc = new FlameGraphAccumulator(new HotspotVirtualMachineThreadDumpProvider(vm));
    }

    public void close() {
        if (null != vm) {
            try {
                vm.detach();
            } catch (IOException ex) {
                log.warn("Failed vm.detach() .. ignore ex " + ex.getMessage());
            }
            this.vm = null;
        }
    }

    public void startScheduleAccumulateToFlames(long freqMillis) {
        acc.initTime();
        this.schedule = scheduledExecutor.scheduleWithFixedDelay(() -> acc.takeAndAccumulateNewThreadDump(),
                freqMillis, freqMillis, TimeUnit.MILLISECONDS);
    }

    public void stopSchedule() {
        if (null != schedule) {
            this.schedule.cancel(false);
            this.schedule = null;
        }
    }

    public FlameGraphNodeDTO getFlameGraphCopyDTO() {
        try {
            return scheduledExecutor.submit(() -> acc.getFlameGraphCopyDTO()).get();
        } catch (InterruptedException e) {
            throw new RuntimeException("", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("", e);
        }
    }

}

