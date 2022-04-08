package net.ro.main;

import java.util.prefs.Preferences;

import net.ro.ventana.Ventana;

public class preferences {
	
	private Ventana v;
	
	private Preferences prefs;
	
	private final String vers = "1.2";
	
	// ------------- Data in the JSON -------------
	private String musicPath;
	private String version;
	private char volumen = 0;				// Value
	private boolean preguntar = false;
	private String preferedPort = "NONE";
	private char mode;
	private int language;	// 0 = English | 1 = Español
	
	public preferences() {	// Initializes the preferences
		prefs = Preferences.userRoot().node(this.getClass().getName());
		
		loadConfig();
	}
	
	public void addVentana(Ventana _v) { v = _v; }
	
	// ----------- MUSIC PATH FUNCTIONS -----------
	
	public String getMusicPath() { return musicPath; }

	public void setMusicPath(String musicPath) { this.musicPath = musicPath; }

	// ----------- VERSION FUNCTIONS -----------
	
	public String getVersion() { return version; }
	
	// ----------- VOLUME FUNCTIONS -----------
	
	public char getVolume() { return volumen; }
	
	public void setVolume(char vol)  { volumen = vol; }
	
	// ----------- PREGUNTAR FUNCTIONS -----------
	
	public boolean getPreguntar() { return preguntar; }
	
	public void setPreguntar(boolean p) { preguntar = p; }
	
	// ----------- PORT FUNCTIONS -----------
	
	public String getPreferedPort() { return preferedPort; }
	
	public void setPreferedPort(String s) { preferedPort = s; }
	
	// ----------- MODE FUNCTIONS -----------
	
	public char getMode() { return mode; }
	
	public void setMode(char c) { mode = c; }
	
	// ----------- LANGUAGE FUNCTIONS -----------
	
	public int getLanguage() { return language; }
	
	public void setLanguage(int s) { language = s; }
	
	// ----------- WRITING FUNCTIONS -----------
	
	public void setNewPath(String dir) {
		musicPath = dir;				// If something went wrong, a null file will come
		if (dir != null)				// If file not null
			guardarConfiguracion();		// Safe
	}
    
	private void loadConfig() {
		musicPath = prefs.get("PATH", "null");
        version = prefs.get("VERSION", vers);
        volumen = (char) prefs.getInt("VOLUMEN", 10);
        if (volumen > 15 || volumen <= 0)	// Sanity check
        	volumen = 10;
        	
        preguntar = prefs.getBoolean("PREGUNTAR", true);
        preferedPort = prefs.get("PORT", "null");
        mode = (char) prefs.getInt("MODO", 0);
        language = prefs.getInt("LANGUAGE", 0);
	}
	
	public void resetConfig() {
		prefs.put("PATH", "null");
		prefs.put("VERSION", vers);
		prefs.putInt("VOLUMEN", 10);
		prefs.putBoolean("PREGUNTAR", true);
		prefs.put("PORT", "null");
		prefs.putInt("MODO", 0);
		prefs.put("LANGUAGE", "english");
		
		loadConfig();
	}
	
	public void guardarConfiguracion() {
		prefs.put("PATH", musicPath);
		prefs.putInt("VOLUMEN", (int) volumen);
		prefs.putBoolean("PREGUNTAR", preguntar);
		prefs.put("PORT", preferedPort);
		prefs.putInt("MODO", (int) mode);
		prefs.putInt("LANGUAGE", language);
			
		v.appCnsl("Preferences saved");
	}
}