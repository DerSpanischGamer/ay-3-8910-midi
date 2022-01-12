package net.ro.main;

import net.ro.ventana.Ventana;

public class launcher {

	private static preferences pref;
	
	public static void main(String[] args) {
		System.out.println("Init...");
		
		pref = new preferences();	// Start preferences
		new Ventana(pref);	// Crear la ventana
	}
}