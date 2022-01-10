package net.ro.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class midiHandler {

	File file;
	String outFile;
	
	public midiHandler() {		
	}
	
	public void processNewFile(File _file) {
		file = _file;
	}
	
	public void toCsv() {
		if (file.getAbsolutePath().isEmpty() || !file.getAbsolutePath().endsWith(".mid"))		// Error check
			return;
		
		outFile = file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".mid")) + ".csv";	// Create the output name
		
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
			if (exitVal == 0) {
				System.out.println("Success");
				System.out.println(outSuc);
			} else { 
				System.out.println("lol, error");
				System.out.println(outErr);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void toAmds() {
		
	}
}