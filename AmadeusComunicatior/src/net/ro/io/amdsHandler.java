package net.ro.io;

import java.io.File;
import java.util.List;

import net.ro.ventana.Ventana;

public class amdsHandler {
	
	private final Ventana v;

	private List<char[]> notas;
	private List<Integer> tiempoEntreNotas;
	
	public amdsHandler(Ventana _v) { v = _v; }
	
	public void clean() {
		v.setTitle("Amadeus");
		// TODO : SET TO NULL ALL VARIABLES
	}
	
	public void processNewFile(File file) {
		if (file == null)
			return;
		
		// TODO : LOAD amds FILE
	}
	
	public List<char[]> getNotas() { return notas; }
	public List<Integer> getTiempos() { return tiempoEntreNotas; }
}
