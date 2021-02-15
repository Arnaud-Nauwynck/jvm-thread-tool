package fr.an.jvm.thread.tool.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ThreadToolServer {

	public static void main(String[] args) {
		SpringApplication.run(ThreadToolServer.class, args);
	}
	
	@Bean
	public CommandLineRunner commandLineRunner(ThreadToolStatefullService service) {
		return args -> {
			for(int i = 0; i < args.length; i++) {
				String a = args[i];
				if (a.equals("--pid")) {
					int pid = Integer.parseInt(args[++i]);
					service.attachPid(pid);
					System.out.println("attach to pid: " + pid);
				}
			}
		};
	}

}

@Service
class ThreadToolStatefullService {
	int pid;
	
	public void attachPid(int pid) {
		this.pid = pid;
		//TODO
	}
}

@RestController
class ThreadToolRestController {
	
	@Autowired
	private ThreadToolStatefullService service;
	
	@GetMapping("/flame-graph")
	public FlameGraphNodeDTO getFlameGraph() {
		FlameGraphNodeDTO res = new FlameGraphNodeDTO("root", 10);
		
		FlameGraphNodeDTO child1 = res.addChild("child1", 5);
		child1.addChild("child1.1", 3);

		res.addChild("child2", 4);
		
		return res;
	}

}
