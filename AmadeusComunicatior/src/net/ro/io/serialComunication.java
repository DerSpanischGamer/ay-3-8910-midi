package net.ro.io;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.fazecast.jSerialComm.SerialPort;

import net.ro.ventana.Ventana;

public class serialComunication {
	
	private final Ventana v;
	
	// Connection variables
	
	private SerialPort puerto;
	private SerialPort[] availablePorts;
	
	private boolean connected;

	// Player variables
	
	private sender s;
	
	public enum incoming { CSV, AMDS };
	private incoming estado = null;
	
	private boolean playing = false;
	private boolean paused = false;
	
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
				
				if (port.getSystemPortName().equals(v.getPreferences().getPreferedPort())) {
					if (!port.openPort())
						v.setCnsl("Couldn't open the port" + port.getSystemPortName());
					else {
						v.appCnsl("Connected to port : " + port.getSystemPortName());
						puerto = port; 
						return true;
					}
				}
			}
		} else {
			for (SerialPort port : availablePorts) {
				if (port.isOpen())
					continue;
				
				if (port.openPort()) {
					puerto = port;		// Save the port for future reference
					
					v.appCnsl("Connected to port : " + port.getSystemPortName());
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
			
			return result;			// Return the result
		}
		
		return false;	// No port to close, return false
	}
	
	public void play() {	// TODO : DIFFERENTIATE IF ITS THE FIRST TIME OR PAUSED
		/*if (estado == null || playing || puerto == null || !puerto.isOpen())
			return;*/

		v.togglePlay();
		
		if (paused) {
			s.sendMsgThread("play");
			paused = false;
			return;
		}
		
		// If we are here, we're starting the song from the beginning: i.e. new Thread and all
		
		playing = true;
		paused = false;
		
		if (estado == incoming.CSV)		// First we need to load the notes and times
			s = new sender(puerto, v.getCSV().getNotas(), v.getCSV().getTiempos());
		else			// Amadeus
			System.out.println("Play amds file");
		s.start();
	}
	
	public void pause() {
		if (estado == null || !playing || paused || s == null)
			return;
		
		v.togglePlay();
		
		paused = false;
		
		s.sendMsgThread("pause");
	}
	
	public void stop() {
		if (estado == null || !playing || s == null || !v.getSerial().isConnected())
			return;
		
		s.sendMsgThread("stop");	// This should kill the Thread	
		
		s = null;
		
		paused = false;
		playing = false;
	}
	
	public boolean isConnected() { return connected; }
	
	public incoming getEstado() { return estado; }
	
	public void setEstado(incoming _e) { estado = _e; } 
}

class sender extends Thread {
	
	private final SerialPort puerto;
	
	private final List<char[]>  notas;
	private final List<Integer> tiempoEntre;
	
	private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
	
	public sender(SerialPort _puerto, List<char[]> _notas, List<Integer> _tiempoEntre) {
		puerto = _puerto;
		
		notas = _notas;
		tiempoEntre = _tiempoEntre;
	}
	
	public void run() {
		boolean play = true;
	    String msg = "";
		
	    int index = 0;
	    
		while(true) {
			while(play) {
				try {
					puerto.writeBytes(new byte[] {(byte) notas.get(index)[0], (byte) notas.get(index)[1], (byte) notas.get(index)[2]}, 3);
					// TODO : ENVIA LAS NOTAS EN EL BUEN MOMENTO PERO NO SUENA NADA
					Thread.sleep(tiempoEntre.get(index));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				index++;
				
				while ((msg = queue.poll()) != null) {
			    	
					switch(msg) {
					case "play":
						play = true;
						break;
					case "pause":
						play = false;
						break;
					case "stop":
						return;
					default:
						System.out.println("Unhandled msg for sender " + msg);
						break;
			     	}
				}
			}
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

// TODO : CREATE A LISTENER CLASS SO IF IT IS DISCONNECTED, IT STOPS AND RESETS