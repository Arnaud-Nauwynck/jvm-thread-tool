package fr.an.jvm.thread.tool.flamegraph.model;

public class FlameGraphDTOUtil {
	
	public static FlameGraphNodeDTO getDefaultFlameGraph() {
		FlameGraphNodeDTO res = new FlameGraphNodeDTO("root", 10);
		
		FlameGraphNodeDTO child1 = res.addChild("child1", 5);
		child1.addChild("child1.1", 3);
	
		res.addChild("child2", 4);
		
		return res;
	}
}