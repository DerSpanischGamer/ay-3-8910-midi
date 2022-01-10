package net.ro.main;

import java.io.File;
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
	
	private FileWriter fw;
	
	public preferences() {	// Initializes the preferences
        try
        {
        	JSONObject obj = (JSONObject) new JSONParser().parse(new FileReader("src/preferences.json"));	//JSON parser object to parse read file
        	
        	musicPath = obj.get("musicPath").toString();
        	version = obj.get("version").toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
			e.printStackTrace();
		}
	}

	// ----------- MUSIC PATH FUNCTIONS -----------
	
	public String getMusicPath() {
		return musicPath;
	}

	public void setMusicPath(String musicPath) {
		this.musicPath = musicPath;
	}

	// ----------- VERSION FUNCTIONS -----------
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	// ----------- WRITING FUNCTIONS -----------
	public void setNewPath(File file) {
		musicPath = file.getAbsolutePath();
		guardarConfiguracion();
	}
    
	@SuppressWarnings("unchecked")
	private void guardarConfiguracion() {
		JSONObject obj = new JSONObject();
		obj.put("musicPath", musicPath);
		obj.put("version", version);
		
		try {
			fw = new FileWriter("src/preferences.json");
			fw.write(obj.toJSONString());
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