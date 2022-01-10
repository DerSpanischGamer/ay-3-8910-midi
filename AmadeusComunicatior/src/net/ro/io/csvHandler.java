package net.ro.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class csvHandler {
	
	String file;
	
	BufferedReader reader;
	String line;
	
	List<String[]> lista;
	
	public csvHandler() {}
	
	public void processNewFile(File _file) {
		line = "";
		reader = null;
		lista = new ArrayList<String[]>();
		
		file = _file.getAbsolutePath();
		
		try {
			reader = new BufferedReader(new FileReader(file));
			
			while((line = reader.readLine()) != null) { // Keep reading as long as there're more lines
				String[] row = line.split(",");
				
				lista.add(row);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		for (String[] index : lista) {
			for (String i : index) {
				System.out.print(i + " ");
			}
			System.out.print('\n');
		}
	}
	
	public void getSongSize() {
		
	}
	
	public void loadArray(char[][] array) {
		
	}
}
