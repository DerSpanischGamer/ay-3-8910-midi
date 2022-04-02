package net.ro.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import net.ro.io.serialComunication.incoming;
import net.ro.ventana.Ventana;

public class csvHandler {
	
	private final Ventana v;
	
	private String file;
	
	private BufferedReader reader;
	private String line;
	
	private List<String[]> lista;
	
	// ---------- MIDI VARIABLES ---------- TODO : WHEN BORED AND *NOT IN EXAMS* ADD SUPPORT FOR CHANGING TEMPO

	private final static int[][] combis = {{-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {-1, -1}, {15, 209}, {14, 238}, {14, 24}, {13, 77}, {12, 142}, {11, 218}, {11, 47}, {10, 143}, {9, 247}, {9, 104}, {8, 224}, {8, 97}, {7, 232}, {7, 119}, {7, 12}, {6, 166}, {6, 71}, {5, 237}, {5, 151}, {5, 71}, {4, 251}, {4, 180}, {4, 112}, {4, 48}, {3, 244}, {3, 187}, {3, 134}, {3, 83}, {3, 35}, {2, 246}, {2, 203}, {2, 163}, {2, 125}, {2, 90}, {2, 56}, {2, 24}, {1, 250}, {1, 221}, {1, 195}, {1, 169}, {1, 145}, {1, 123}, {1, 101}, {1, 81}, {1, 62}, {1, 45}, {1, 28}, {1, 12}, {0, 253}, {0, 238}, {0, 225}, {0, 212}, {0, 200}, {0, 189}, {0, 178}, {0, 168}, {0, 159}, {0, 150}, {0, 142}, {0, 134}, {0, 126}, {0, 119}, {0, 112}, {0, 106}, {0, 100}, {0, 94}, {0, 89}, {0, 84}, {0, 79}, {0, 75}, {0, 71}, {0, 67}, {0, 63}, {0, 59}, {0, 56}, {0, 53}, {0, 50}, {0, 47}, {0, 44}, {0, 42}, {0, 39}, {0, 37}, {0, 35}, {0, 33}, {0, 31}, {0, 29}, {0, 28}, {0, 26}, {0, 25}, {0, 23}, {0, 22}, {0, 21}, {0, 19}, {0, 18}, {0, 17}, {0, 16}, {0, 15}, {0, 14}, {0, 14}, {0, 13}, {0, 12}, {0, 11}, {0, 11}, {0, 10}, {0, 9}, {0, 9}, {0, 8}, {0, 8}, {0, 7}, {0, 7}, {0, 7}, {0, 6}, {0, 6}, {0, 5}, {0, 5}, {0, 5}, {0, 4}, {0, 4}, {0, 4}, {0, 4}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 3}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 2}, {0, 1}, {0, 1}, {0, 1}, {0, 1}, {0, 1}, {0, 1}, {0, 1}};
	
	private int pulsos = 24;						// Pulses per ms (normally 24)
	
	private char mode = 0;			// Sound mode
	private char[][] canales = {					// CHIP 1 : [  0  |  1  |  2  ]    CHIP 2 : [  3  |  4  |  5  ]
			{0, 1, 2, 3, 4, 5},		// First come first served 			(left-ear-heavy)
			{2, 3, 4, 1, 0, 5},		// Centralized sound 	   			(should sond more centered)
			{5, 4, 3, 2, 1, 0}		// idk, just want to experiment		(right-ear-heavy ?) should be the opposite of the first case
	};
	
	private int[] dispo = {-1, -1, -1, -1, -1, -1};	// Available channels
	private double tempo;							// Stores the ms / pulso ; to get ms multiply times pulsos
	
	private boolean maxCanales = false;				// Set to true if the song uses more than 6 channels simulaneously
	private char canalesUtilizados = 0;				// Max channels used at once
	
	private ArrayList<Integer[]> preNotas;				// Array to prepare the data : {totalTimeStamp , 1 = turn on : 0 = turn off , note}
	
	// IMPORTANT : note = midi value ; value = YM2149 register value (2 values are needed)
	
	private int ultimaVez;							// Stores the last time interval in order to compute the difference
	private char volumen = 10;						// Stores the volume (in value of register) of the boards
	
	private int[][] notasInt;						// preNotas is a List and notasInt is an array: new boss, same as the old one
	private List<char[]> notas;							// Saves the notes to send : {chip , register , value}
	private List<Integer> tiempoEntreNotas;			// Saves the time to wait between notes
	
	public csvHandler(Ventana _v) {
		v = _v;
		
		volumen = v.getPreferences().getVolume();
		mode = v.getPreferences().getMode();
	}
	
	public void reset() {
		v.csvButtons(false);
		v.plyButtons(false);
		v.midButtons(false);
		
		// Reset files variables
		
		file = null;
		
		reader = null;
		line = "";
	
		lista = null;
		
		// Reset treatment variables
		
		preNotas = null;
		
		notas = null;
		tiempoEntreNotas = null;
		
		// Reset external things
		
		v.getSerial().setEstado(null);
		
		v.setTitle("Amadeus");
	}
	
	public void processNewFile(File _file) {
		if (_file == null)
			return;
		
		reset();
		v.getMIDI().reset();
		v.getAMDS().reset();
		
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
			return;
		} finally {
			try {
				reader.close();	
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
		
		v.setTitle("Amadeus - " + file + " - csv");
	
		csvArray();
	}
	
	private int getAvailableChannel() {
		for (char i = 0; i < dispo.length; i++) {
			if (dispo[i] == -1)
				return i;
		}
		
		return -1;
	}
	
	private int getChannel(int nota) {						// Returns the channel that is playing the note nota
		for (char i = 0; i < dispo.length; i++) {
			if (dispo[i] == nota)
				return i;
		}
		
		return -1;
	}
	
	private void addNote(int time, int nota) {
		int diff = time - ultimaVez;
		if (diff < 0)
			return;		// Sanity check
		
		int posicion = getAvailableChannel();
		if (posicion == -1 || combis[nota][0] == -1)
			return;		// If no available channel or the note yields an unplayable value, then don't add it
		
		char realPosicion = canales[mode][posicion];
		char chip  = (char) (realPosicion / 3);
		char canal = (char) (realPosicion % 3);
		
		tiempoEntreNotas.add(diff);		// Add the time to wait since the last note
		tiempoEntreNotas.add(0);
		tiempoEntreNotas.add(0);
		
		ultimaVez = time;			// Update last timestamp used
		
		dispo[posicion] = nota;		// State that the channel is not available

		notas.add(new char[] {chip, (char) (canal * 2), (char) combis[nota][1]});			// Store the values to send
		notas.add(new char[] {chip, (char) ((canal * 2) + 1), (char) combis[nota][0]});
		notas.add(new char[] {chip, (char) (canal + 8), volumen});
	}
	
	private void quitarNota(int time, int nota) {
		int diff = time - ultimaVez;
		if (diff < 0)
			return;		// Sanity check
		
		int posicion = getChannel(nota);
		if (posicion == -1)
			return;		// Second sanity check
		
		tiempoEntreNotas.add(diff);
		
		ultimaVez = time;		// Update last time
		
		dispo[posicion] = -1;
		
		char realPosicion = canales[mode][posicion];
		
		notas.add(new char[] { (char) (realPosicion / 3), (char) ((realPosicion % 3) + 8), 0});		// Mute the channel
	}
	
	private void csvArray() {	// Reads the csv in detail and stores in order to be ready to be send
		v.setCnsl("csv loaded: " + file);
		
		ultimaVez = 0;			// Reset the last time counter
		
		maxCanales = false;
		canalesUtilizados = 0;
		
		tempo = 0;				// Reset tempo
		
		preNotas = new ArrayList<>();
		
		notas = new ArrayList<char[]>();
		tiempoEntreNotas = new ArrayList<Integer>();
		
		try {
			for (String[] row : lista) {
				if (row[2].equals(" Header"))
					pulsos = Integer.parseInt(row[5].substring(1));
				
				if (tempo == 0 && row[2].equals(" Tempo") && Integer.parseInt(row[1].substring(1)) == 0)
					tempo = (float) Integer.parseInt(row[3].substring(1)) / (1000 * pulsos);						// ms / clk to obtain ms multiply times pulsos
				
				if (row[2].equals(" Note_on_c"))
					preNotas.add(new Integer[] {(int) Math.round(Integer.parseInt(row[1].substring(1)) * tempo), 1, Integer.parseInt(row[4].substring(1))});
				else if (row[2].equals(" Note_off_c"))
					preNotas.add(new Integer[] {(int) Math.round(Integer.parseInt(row[1].substring(1)) * tempo), 0, Integer.parseInt(row[4].substring(1))});
			}
			
			// All the notes have been extracted, it's time to start processing
			
			notasInt = getArray(preNotas);									// List to array
			
			Arrays.sort(notasInt, (a, b) -> Integer.compare(a[0], b[0]));	// Sort the array by their time stamp
			
			char maxCanals = 0;
			
			// Channel check
			for (int[] row : notasInt) {
				if (row[1] == 1)
					canalesUtilizados++;
				else
					canalesUtilizados--;
				
				if (canalesUtilizados > 6) {
					v.appCnsl("At " + row[0] + " " + (int) canalesUtilizados + " would be needed.");
					maxCanales = true;
					if (canalesUtilizados > maxCanals)
						maxCanals = canalesUtilizados;
				}
			}
			
			if (canalesUtilizados != 0) {
				v.setCnsl("Operation failed, there are still " + Integer.toString((int) canalesUtilizados) + " channels opened");
				file = null;
				return;
			}
			
			// If more than 6 channels are used in parallel, the user is asked if we wishes to continue or not
			if (v.getPreferences().getPreguntar() && maxCanales && JOptionPane.showConfirmDialog(v, 
		            (int) maxCanals + " channels would be needed. \n The file may not play properly due to this. \n Do you want to continue?", "Continue?", 
		            JOptionPane.YES_NO_OPTION,
		            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION)
		            	arrayValues();
			else if (!maxCanales)
				arrayValues();		// If no more than 6 channels, we continue too
			else {
				v.setCnsl("Operation cancelled, not enough channels to play the song.");
				return;				// If the user doesn't accept the channels then exit
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			v.appCnsl("Error processing csv file");
		}
	}
	
	private void arrayValues() {
		// If we are here is bcs we playing them tunes
		v.getSerial().setEstado(incoming.CSV);
		
		// Enable the buttons that are actually necesary
		
		v.plyButtons(true);
		v.midButtons(false);
		v.csvButtons(true);
		
		for (int[] tone : notasInt) {
			if (tone[1] == 1)
				addNote(tone[0], tone[2]);
			else
				quitarNota(tone[0], tone[2]);
		}
	}
	
	private int[][] getArray(List<Integer[]> list) {
		int[][] array = new int[list.size()][3];
		
		for (int i = 0; i < list.size(); i++)
			for (char j = 0; j < list.get(i).length; j++)
				array[i][j] = list.get(i)[j];
		
		return array;
	}
	
	public void toAmds() {
		// TODO : DO
	}
	
	// Volume management
	
	public void setVolumen(char v) { volumen = v; }
	
	public void setMode(char c) { mode = c; }
	
	public List<char[]> getNotas() { return notas; }
	
	public List<Integer> getTiempos() { return tiempoEntreNotas; }
	
	public int getSongSize() {
		if (tiempoEntreNotas != null) {
			int suma = 0;
			
			for (int t : tiempoEntreNotas)
				suma += t;
			
			return (int) Math.round(suma * tempo * pulsos);
		}
		
		return -1;
	}
}
