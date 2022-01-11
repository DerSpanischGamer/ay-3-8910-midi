package net.ro.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class serialComunication {
	
}

class sender extends Thread {
	private BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	
	private boolean playing = false;	// Play / Stop
	private boolean paused = false;		// Play / Pause
	
	public void run() {
	    String msg;
		
		playing = true;
		
		while(true){
	       while ((msg = queue.poll()) != null) {
	    	   // process msg
	       }
	       // do other stuff
	     }
	}
	
	public void sendMsgThread(String msg) {
		try {
			queue.put(msg);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}