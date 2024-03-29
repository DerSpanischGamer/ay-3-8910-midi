package net.ro.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import net.ro.ventana.Ventana;

public class midiHandler {

	Ventana v;
	
	File file;
	String outFile;
	
	public midiHandler(Ventana _v) {v = _v;}
	
	public void reset() {
		v.plyButtons(false);
		v.midButtons(false);
		
		file = null;
		outFile = null;
		
		v.setTitle("Amadeus");
	}
	
	public void processNewFile(File _file) {	// Loads the file and awaits instructions on how to treat it
		if (_file == null)
			return;
		
		reset();
		v.getCSV().reset();
		v.getAMDS().reset();
		
		file = _file;
		
		outFile = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".mid"));	// Get the output file without extension
		
		v.setTitle("Amadeus - " + _file.getAbsolutePath() + " - mid");
		v.setCnsl(v.getText("midLdd") + _file.getAbsolutePath());
		
		v.plyButtons(false);
		v.midButtons(true);
		v.csvButtons(false);
	}
	
	public void toCsv(File outF) {
		if (outF == null || outF.getAbsolutePath().isEmpty() || !outF.getAbsolutePath().endsWith(".mid"))		// Error check
			return;
		
		outFile = outF.getAbsolutePath();
		
		System.out.println(file.getAbsolutePath());
		System.out.println(outFile);

		System.out.println();
		System.out.println();
		System.out.println();
		
		try {
			String comando = "cmd /c Midicsv.exe \"" + file.getAbsolutePath() + "\" \"" + outFile + "\"";
			Process process = Runtime.getRuntime().exec(comando);
			
			System.out.println(comando);
			
			StringBuilder outSuc = new StringBuilder();
			StringBuilder outErr = new StringBuilder();
			
			BufferedReader readSuc = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader readErr = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			
			String line;

			while((line = readSuc.readLine()) != null) { outSuc.append(line + '\n'); }
			while((line = readErr.readLine()) != null) { outErr.append(line + '\n'); }
			
			int exitVal = process.waitFor();
			if (exitVal == 0) {	// Success => go to csvHandler
				v.appCnsl(v.getText("midScs"));
				System.out.println(outSuc);
				
			} else {
				v.setCnsl(v.getText("midErr"));
				v.appCnsl(outErr.toString());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
			return;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return;
		}
		
		// If we are here, everything has gone properly
	}
	
	public void toAmds() {
		// TODO : OK, HEAR ME OUT, WE GO FROM MID TO CSV, CSV TO AMDS, AND THEN WE DELETE CSV
	}
	
	public String getDir() {
		return outFile;
	}
}