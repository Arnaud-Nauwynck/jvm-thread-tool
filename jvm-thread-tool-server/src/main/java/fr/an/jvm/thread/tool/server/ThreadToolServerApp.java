package fr.an.jvm.thread.tool.server;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import fr.an.jvm.thread.tool.server.service.ThreadToolService;

@SpringBootApplication
public class ThreadToolServerApp {

	public static void main(String[] args) {
		SpringApplication.run(ThreadToolServerApp.class, args);
	}
	
	@Bean
	public CommandLineRunner commandLineRunner(ThreadToolService service) {
		return args -> {
			for(int i = 0; i < args.length; i++) {
				String a = args[i];
				if (a.equals("--pid")) {
					int pid = Integer.parseInt(args[++i]);
					service.attachPid(pid);
					System.out.println("attach to pid: " + pid);
					service.start();
				}
			}
		};
	}

}
