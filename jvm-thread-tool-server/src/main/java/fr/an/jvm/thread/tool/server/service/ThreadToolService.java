package fr.an.jvm.thread.tool.server.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.stereotype.Service;

import fr.an.jvm.thread.tool.flamegraph.VirtualMachineFlameGraphProvider;
import fr.an.jvm.thread.tool.flamegraph.model.FlameGraphDTOUtil;
import fr.an.jvm.thread.tool.flamegraph.model.FlameGraphNodeDTO;

@Service
public class ThreadToolService {
	
	private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
	
	private int pid;
	private VirtualMachineFlameGraphProvider vmFlameGraphProvider;
	private long freqMillis = 250;
	
	public void attachPid(int pid) {
		this.pid = pid;
		this.vmFlameGraphProvider = new VirtualMachineFlameGraphProvider(pid, scheduledExecutorService);
		this.vmFlameGraphProvider.init();
	}
	
	public void start() {
		this.vmFlameGraphProvider.startScheduleAccumulateToFlames(freqMillis);	
	}
	
	public void stop() {
		this.vmFlameGraphProvider.stopSchedule();	
	}

	public FlameGraphNodeDTO getFlameGraph() {
		return vmFlameGraphProvider.getFlameGraphCopyDTO();
//		return FlameGraphDTOUtil.getDefaultFlameGraph();
	}
}