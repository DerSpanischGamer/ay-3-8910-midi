package net.ro.io;

import java.io.File;

import javax.swing.JFrame;

public class amdsHandler {
	
	JFrame frame;
	
	public amdsHandler(JFrame _frame) {frame = _frame;}
	
	public void clean() {
		frame.setTitle("Amadeus");
		// TODO : SET TO NULL ALL VARIABLES
	}
	
	public void processNewFile(File file) {
		if (file == null)
			return;
	}
}
