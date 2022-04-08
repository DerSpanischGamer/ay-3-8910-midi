package net.ro.main;

import net.ro.ventana.Ventana;

public class launcher {

	private static languageManager lgn;
	private static preferences pref;
	private static Ventana v;
	
	public static void main(String[] args) {
		System.out.println("Init...");

		
		pref = new preferences();			// Start preferences
		lgn = new languageManager(pref); 	// Start language manager
		v = new Ventana(pref, lgn);			// Crear la ventana
		pref.addVentana(v);					// Add ventana to preferences
	}
}