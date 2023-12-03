package fr.an.jvm.thread.tool.server;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Test {

	public void doSomething() throws Exception {
		int maxRetry = 5;
		for(int retry = 0; retry < maxRetry; retry++) {
			try {
				anyTreatmentCanFail();
				break; // success!
			} catch(Exception ex) {
				log.warn("Failed .. retry " + retry + "/" + maxRetry, ex);
				sleep(100 * Math.pow(2, maxRetry)); // wait a little
			}
		}
	}
	
	public void anyTreatmentCanFail() {
		
	}
	public static void sleep(double d) {
	}
	// exponentialTimeWait()
}
