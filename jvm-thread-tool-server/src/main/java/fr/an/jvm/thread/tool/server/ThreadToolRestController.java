package fr.an.jvm.thread.tool.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.an.jvm.thread.tool.flamegraph.model.FlameGraphNodeDTO;
import fr.an.jvm.thread.tool.server.service.ThreadToolService;

@RestController
class ThreadToolRestController {
	
	@Autowired
	private ThreadToolService service;
	
	FlameGraphNodeDTO lastFlameGraph;

//	@PutMapping("/start")
//	public void start() {
//	}
	
//	@PutMapping("/stop")
//	public void stop() {
//	}

	@GetMapping("/flame-graph")
	public FlameGraphNodeDTO getFlameGraph() {
		FlameGraphNodeDTO res = service.getFlameGraph();
		this.lastFlameGraph = res; // for debugging
		res = this.lastFlameGraph; // for debugging
		return res;
	}

}