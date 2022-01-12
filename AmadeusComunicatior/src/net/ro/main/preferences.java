package net.ro.main;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class preferences {
	
	// ------------- Data in the JSON -------------
	private String musicPath;
	private String version;
	private char volumen;
	private boolean preguntar;
	private String preferedPort;
	private char mode;
	
	private FileWriter fw;
	
	public preferences() {	// Initializes the preferences
        try
        {
        	JSONObject obj = (JSONObject) new JSONParser().parse(new FileReader("src/preferences.json"));	//JSON parser object to parse read file
        	
        	musicPath = obj.get("musicPath").toString();
        	version = obj.get("version").toString();
        	volumen = (char) Integer.parseInt(obj.get("volumen").toString());
        	if (volumen > 15 || volumen <= 0)
        		volumen = 10;
        	preguntar = (boolean) obj.get("preguntar");
        	preferedPort = obj.get("preferedPort").toString();
        	mode = (char) Integer.parseInt(obj.get("mode").toString());
        	
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
			e.printStackTrace();
		}
	}

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
	
	// ----------- WRITING FUNCTIONS -----------
	
	public void setNewPath(String dir) {
		musicPath = dir;				// If something went wrong, a null file will come
		if (dir != null)				// If file not null
			guardarConfiguracion();		// Safe
	}
    
	@SuppressWarnings("unchecked")
	public void guardarConfiguracion() {
		JSONObject obj = new JSONObject();
		
		obj.put("musicPath", musicPath);
		obj.put("version", version);
		obj.put("volumen", (int) volumen);
		obj.put("preguntar", preguntar);
		obj.put("preferedPort", preferedPort);
		obj.put("mode", (int) mode);
		
		try {
			fw = new FileWriter("src/preferences.json");
			fw.write(obj.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}