package net.ro.main;

import net.ro.ventana.Ventana;

public class launcher {

	private static Ventana v;
	private static preferences pref;
	
	public static void main(String[] args) {
		System.out.println("Init...");

		pref = new preferences();	// Start preferences
		v = new Ventana(pref);		// Crear la ventana
		pref.addVentana(v);
	}
}