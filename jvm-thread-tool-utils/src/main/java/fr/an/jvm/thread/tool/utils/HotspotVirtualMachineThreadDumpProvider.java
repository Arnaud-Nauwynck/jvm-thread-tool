package fr.an.jvm.thread.tool.utils;

import java.util.function.Supplier;

import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import lombok.AllArgsConstructor;
import sun.tools.attach.HotSpotVirtualMachine;

@AllArgsConstructor
public class HotspotVirtualMachineThreadDumpProvider implements Supplier<ThreadDumpInfo> {

	private final HotSpotVirtualMachine vm;
	
	@Override
	public ThreadDumpInfo get() {
		return HotspotVirtualMachineUtils.getThreadDump(vm);
	}

}
