package fr.an.jvm.thread.tool.utils;

import java.util.function.Supplier;

import fr.an.jvm.thread.tool.flamegraph.model.FlameGraphNodeDTO;
import fr.an.jvm.thread.tool.flamegraph.model.FlameGraphTreeNode;
import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FlameGraphAccumulator {
	
	private final Supplier<ThreadDumpInfo> threadDumpProvider;
	long prevThreadDumpTime;
	private FlameGraphTreeNode flameGraph = new FlameGraphTreeNode();
	int count;
	
	public void initTime() {
		prevThreadDumpTime = System.currentTimeMillis();
	}
	
	public void takeAndAccumulateNewThreadDump() {
		try {
	        long now = System.currentTimeMillis();
	        ThreadDumpInfo threadDump = threadDumpProvider.get();
	            
	        long millis = now - prevThreadDumpTime;
	        this.prevThreadDumpTime = now;
	        
	        flameGraph.accumulate(threadDump, millis);
	        count++;
		} catch(Exception ex) {
			// failedCount++;
			// ignore, no rethrow!
		}
    }

	public FlameGraphNodeDTO getFlameGraphCopyDTO() {
		return flameGraph.toDTO();
	}

}