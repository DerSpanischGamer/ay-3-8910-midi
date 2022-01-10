package net.ro.main;

import net.ro.ventana.Ventana;

public class main {

	private static preferences pref;
	private static Ventana v;
	
	public static void main(String[] args) {
		System.out.println("Init...");
		
		pref = new preferences();	// Start preferences
		v = new Ventana(pref);	// Crear la ventana
	}
}