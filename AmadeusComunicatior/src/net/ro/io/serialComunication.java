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
	
	private sender s = null;
	
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

					    puerto.setComPortParameters(115200, 8, 1, 0); 						// default connection settings for Amadeus Board
					    puerto.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0); // Block until bytes can be written
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
	
	public void play() {
		if (playing) { // This means the song has already started playing, therefore we need to pause the song
			s.sendMsgThread("play");
			paused = false;
			
			return;
		}
		
		// If we're here, it means that it is the first time playing play
		if (estado == null || puerto == null || !puerto.isOpen())
			return;

		v.togglePlay();
		
		playing = true;
		paused = false;
		
		v.setAll(2);
		
		if (estado == incoming.CSV)		// First we need to load the notes and times
			s = new sender(puerto, v.getCSV().getNotas(), v.getCSV().getTiempos());
		else			// Amadeus
			System.out.println("Play amds file");
		
		s.start();
	}
	
	public void pause() {	// Start playing the song again
		if (estado == null || !playing || paused || s == null)
			return;
		
		v.togglePlay();
		
		paused = false;
		
		s.sendMsgThread("pause");
		
		setEstado(estado);
	}
	
	public void stop() {	// Stop playing the song
		if (estado == null || !playing || s == null || !v.getSerial().isConnected())
			return;

		v.setPlay();
		s.sendMsgThread("stop");	// This should kill the Thread	
		
		s = null;
		
		paused = false;
		playing = false;
	
		setEstado(estado);
	}
	
	public boolean isConnected() { return connected; }
	
	public incoming getEstado() { return estado; }
	
	public void setEstado(incoming _e) {
		estado = _e;
	
		if (estado == null)
			return;
		
		v.setAll(0);
		v.setStatus(estado.ordinal() + 1, 1);
	} 
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
		boolean stop = false;
	    String msg = "";
		
	    int index = 0;
	    
		while(!stop) {
			while(play) {
				try {
					Thread.sleep((int) tiempoEntre.get(index));
					puerto.writeBytes(new byte[] {(byte) notas.get(index)[0], (byte) notas.get(index)[1], (byte) notas.get(index)[2]}, 3);
					
					index++;
					
					while ((msg = queue.poll()) != null) {		// Check if there are new commands
				    	
						switch(msg) {
							case "pause":
								play = false;
								break;
							case "stop":
								play = false;
								stop = true;
							default:
								System.out.println("Unhandled msg for sender " + msg);
								break;
				     	}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			while((msg = queue.poll()) != null) {	// Once we are paused, check if there are more messages like start or stop
				if (msg == "stop")
					stop = true;
				else if (msg == "play")
					play = true;
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