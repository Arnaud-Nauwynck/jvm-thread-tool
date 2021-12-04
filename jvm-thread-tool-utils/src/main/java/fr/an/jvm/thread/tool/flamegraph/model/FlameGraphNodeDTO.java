package fr.an.jvm.thread.tool.flamegraph.model;

import java.util.ArrayList;
import java.util.List;

public class FlameGraphNodeDTO {
	
	public String name;
	public double value;
	public List<FlameGraphNodeDTO> children;
	
	public FlameGraphNodeDTO() {
	}

	public FlameGraphNodeDTO(String name, double value) {
		this(name, value, new ArrayList<>());
	}
	
	public FlameGraphNodeDTO(String name, double value, List<FlameGraphNodeDTO> children) {
		super();
		this.name = name;
		this.value = value;
		this.children = children;
	}

	public FlameGraphNodeDTO addChild(String name, double value) {
		FlameGraphNodeDTO res = new FlameGraphNodeDTO(name, value);
		addChild(res);
		return res;
	}
	
	public void addChild(FlameGraphNodeDTO child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}
	
}
