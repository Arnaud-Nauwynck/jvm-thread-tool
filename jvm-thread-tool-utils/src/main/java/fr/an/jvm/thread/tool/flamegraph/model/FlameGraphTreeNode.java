package fr.an.jvm.thread.tool.flamegraph.model;

import fr.an.jvm.thread.tool.threaddumps.model.ThreadDumpInfo;
import lombok.val;

public class FlameGraphTreeNode {

	private FlameGraphNode rootNode = new FlameGraphNode("");

	public void accumulate(ThreadDumpInfo threadDump, double millis) {
		for (val thread: threadDump.getThreads()) {
			FlameGraphNode currNode = rootNode;
			currNode.incrValue(millis);
			for(val stack: thread.getStack()) {
				FlameGraphNode childNode = currNode.getOrCreateChild(stack.getMethodFullName());
				childNode.incrValue(millis);
				currNode = childNode;
			}
		}
	}

	public FlameGraphNodeDTO toDTO() {
		return rootNode.toDTO();
	}

}
