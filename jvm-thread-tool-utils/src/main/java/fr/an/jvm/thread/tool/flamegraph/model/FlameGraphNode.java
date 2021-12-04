package fr.an.jvm.thread.tool.flamegraph.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.val;

public class FlameGraphNode {

	@Getter
	public final String name;

	@Getter
	private double value;

	@Getter
	private Map<String,FlameGraphNode> children = new LinkedHashMap<>();
	
	public FlameGraphNode(String name) {
		this.name = name;
	}

	public FlameGraphNode(String name, double value) {
		this.name = name;
		this.value = value;
	}
	
	public FlameGraphNode getOrCreateChild(String name) {
		FlameGraphNode res = children.get(name);
		if (null == res) {
			res = new FlameGraphNode(name);
			children.put(name, res);
		}
		return res;
	}

	public void incrValue(double incr) {
		this.value += incr;
	}

	public FlameGraphNodeDTO toDTO() {
		FlameGraphNodeDTO res = new FlameGraphNodeDTO();
		res.name = name;
		res.value = value;
		for(val child: children.values()) {
			res.addChild(child.toDTO());	
		}
		return res;
	}
	
}
