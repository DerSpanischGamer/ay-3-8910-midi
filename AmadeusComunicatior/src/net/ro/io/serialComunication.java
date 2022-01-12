package net.ro.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fazecast.jSerialComm.SerialPort;

import net.ro.ventana.Ventana;

public class serialComunication {
	
	private final Ventana v;
	
	private SerialPort puerto;
	private SerialPort[] availablePorts;
	
	private boolean connected;
	
	public serialComunication(Ventana _v) {
		v = _v;
		
		connected = false;
		
		reloadPorts();
	}
	
	public void reloadPorts() { availablePorts = SerialPort.getCommPorts(); }

	public boolean connectPort() {
		reloadPorts();		// Before connecting, reload the ports
		
		if (availablePorts.length == 0) {
			v.appCnsl("No ports to connect to.");
			return false;
		}
		
		if (!v.getPreferences().getPreferedPort().equals("null")) {		// Attempt to connect to default port
			for (SerialPort port : availablePorts) {
				if (port.isOpen())
					continue;
				
				if (port.getSystemPortName().equals(v.getPreferences().getPreferedPort()))
						if (!port.openPort())
							v.setCnsl("Couldn't open the port" + port.getSystemPortName());
						else {
							puerto = port; 
							return true;
						}
			}
		} else {
			for (SerialPort port : availablePorts) {
				if (port.isOpen())
					continue;
				
				if (port.openPort()) {
					puerto = port;		// Save the port for future reference
					v.getPreferences().setPreferedPort(puerto.getSystemPortName());
					return true;		// Returns a true stating that it has been able to connect TODO : TEST
				}
			}
		}
		
		v.appCnsl("Unable to connect to any port.");
		return false;
	}
	
	public boolean disconnectPort() {
		if (puerto.isOpen()) {
			boolean result = puerto.closePort();
			
			if (result)
				puerto = null;		// If it has successfully disconnected, empty the port
			
			return result;
		}
		
		return false;
	}
	
	public boolean isConnected() { return connected; }
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