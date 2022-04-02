package net.ro.ventana;

import net.ro.io.*;
import net.ro.main.*;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class Ventana extends JFrame {	// TODO : ADD SUPPORT FOR MULTIPLE LANGUAGES

	private final preferences pref;
	
	private final JFrame frame;
	
	private final windowManager wm;
	
	// ----------- Menu bar buttons -----------
	
	private final fileButtons fb;
	private final helpButtons hb;
	
	// ----------- Main buttons -----------

	private final JButton openMIDI;
	private final JButton openCSV;
	private final JButton openAMDS;
	
	private static final int[][] selectButtonsDims = {
			{100, 150, 125, 25},		// Select .mid
			{100, 242, 125, 25},		// Select .csv
			{100, 300, 125, 25}			// Select .amds
	};

	private final JButton midiCsv;
	private final JButton midiAmds;
	private final JButton csvAmds;
	private final JButton play;
	private final JButton stop;
	private final JButton connect;

	private final JLabel statusMid;
	private final JLabel statusAmds;
	private final JLabel statusCsv;

	private final JLabel[] statuses;
	
	private final ImageIcon ready;
	private final ImageIcon loaded;
	private final ImageIcon busy;	// Todo disable all loading buttons when playing
	
	private final ImageIcon[] icons;
	
	private static final int[][] iconsPositions = {
			{60, 153, 20, 20},
			{60, 246, 20, 20},
			{60, 303, 20, 20}
	};
	
	private static final int[][] mainButtonsDims = {
			{400, 125, 100, 25},	// .mid to .csv
			{400, 175, 100, 25},	// .mid to .amds
			{400, 242, 100, 25},	// .csv to .amds
			{450, 350, 100, 25},	//	Play / Pause
			{450, 	400, 100, 25},	//	Stop
			{450, 450, 100, 25},	//	Connect
	};
	
	private final mainButtons mb;
	
	private final int thickness = 2;
	private final int[][] separators = {
			{0, 118, 600, thickness},		// LOGO / .mid
			{0, 225, 600, thickness},		// .mid / .csv
			{0, 287, 600, thickness},		// .csv / .amds
			{0, 335, 600, thickness},		// .amds / controls
			{10, 350, 430, 160}				// output
	};
	
	private final JTextArea cnsl;		// Text console
	
	// ----------- Handlers -----------
	
	private final midiHandler midH;
	private final csvHandler csvH;
	private final amdsHandler amdsH;
	
	// ----------- Menubar -----------
	
	private final JMenu fileMenu;
	
	private final JMenuItem fileOpen;
	private final JMenuItem filePreferences;
	private final JMenuItem fileExit;
	
	private final JMenu helpMenu;

	private final JMenuItem helpHelp;
	private final JMenuItem helpTd;
	private final JMenuItem helpYT;
	private final JMenuItem helpAbout;
	
	// ----------- Preferences -----------
	
	private JFrame prefs;
	
	private JScrollBar prefVol;
	private JCheckBox preguntar;
	@SuppressWarnings("rawtypes")
	private JComboBox modo;
	
	private final static String[] modos = {"Left-centered", "Center-centered", "Right-centered"};
	
	private JButton reset;
	private JButton apply;
	private JButton apExt;
	private JButton exit;
	
	private int[][] prefBounds = {
			{125, 60, 200, 15},			// Volume scrollbar
			{50, 110, 300, 20},			// Checkbox
			{150, 200, 75, 25},			// Apply button
			{250, 200, 125, 25},		// Apply and Exit button
			{400, 200, 75, 25},			// Exit
			{0, 50, 500, thickness},	// Title / volume
			{0, 100, 500, thickness},	// volume / preguntar
			{0, 190, 500, thickness},	// mode / buttons
			{125, 155, 300, 25},		// Mode
			{0, 140, 500, thickness},	// preguntar / mode
			{25, 200, 75, 25}
	};
	
	private final serialComunication sc;
	
	public Ventana(preferences _pref) {
		pref = _pref;
		frame = this;
		
		setTitle("Amadeus");
		setSize(600, 600);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
        setLayout(null);
        
        wm = new windowManager(this);
        
        addWindowListener(wm);
        
		// --------------- Diseño del menu ---------------
		
		JMenuBar menubar = new JMenuBar();
		
		// --------------- File Menu ---------------
		
		// Declaracion de los items
		
		fileMenu = new JMenu("File");		// Declarar nuevo menu
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		fileOpen = new JMenuItem("Open", KeyEvent.VK_O);
		fileOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		filePreferences = new JMenuItem("Preferences", KeyEvent.VK_P);
		filePreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		fileExit = new JMenuItem("Exit", KeyEvent.VK_E);
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		
		// Añadirlos items al menu
		
		fileMenu.add(fileOpen);
		fileMenu.addSeparator();
		fileMenu.add(filePreferences);
		fileMenu.addSeparator();
		fileMenu.add(fileExit);
		
		fb = new fileButtons(this);
		
		fileOpen.addActionListener(fb);
		filePreferences.addActionListener(fb);
		fileExit.addActionListener(fb);
		
		// --------------- Help Menu ---------------
		
		// Declaracion items
		
		helpMenu = new JMenu("Help");			// Declarar nuevo menu
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		helpHelp = new JMenuItem("Help", KeyEvent.VK_H);
		helpTd = new JMenuItem("Tindie", KeyEvent.VK_T);
		helpYT = new JMenuItem("Youtube", KeyEvent.VK_Y);
		helpAbout = new JMenuItem("About", KeyEvent.VK_A);
		
		// Añadirlos al menu
		helpMenu.add(helpHelp);
		helpMenu.addSeparator();
		helpMenu.add(helpTd);
		helpMenu.add(helpYT);
		helpMenu.addSeparator();
		helpMenu.add(helpAbout);
		
		// Añadir las funciones
		
		hb = new helpButtons(this);

		helpTd.addActionListener(hb);
		helpYT.addActionListener(hb);
		helpAbout.addActionListener(hb);
		
		// --------------- Añadir menus -------------------

		menubar.add(fileMenu);
		menubar.add(helpMenu);
		
		setJMenuBar(menubar); // Acabar poniendo la menu
		
		// =============== MAIN WINDOW ====================
		
		mb = new mainButtons(this);
		
		openMIDI = new JButton("Open midi file");
		openMIDI.setBounds(selectButtonsDims[0][0], selectButtonsDims[0][1], selectButtonsDims[0][2], selectButtonsDims[0][3]);
		openMIDI.addActionListener(mb);
		openMIDI.setVisible(true);
		
		openCSV = new JButton("Open csv file");
		openCSV.setBounds(selectButtonsDims[1][0], selectButtonsDims[1][1], selectButtonsDims[1][2], selectButtonsDims[1][3]);
		openCSV.addActionListener(mb);
		openCSV.setVisible(true);
		
		openAMDS = new JButton("Open amds file");
		openAMDS.setBounds(selectButtonsDims[2][0], selectButtonsDims[2][1], selectButtonsDims[2][2], selectButtonsDims[2][3]);
		openAMDS.addActionListener(mb);
		openAMDS.setVisible(true);

		add(openCSV);
		add(openMIDI);
		add(openAMDS);
		
		// --------------- Añadir botones ---------------
		
		midiCsv  = new JButton("To .csv");
		midiCsv.setBounds(mainButtonsDims[0][0], mainButtonsDims[0][1], mainButtonsDims[0][2], mainButtonsDims[0][3]);
		midiCsv.addActionListener(mb);
		midiCsv.setVisible(true);
		
		midiAmds = new JButton("To .amds");
		midiAmds.setBounds(mainButtonsDims[1][0], mainButtonsDims[1][1], mainButtonsDims[1][2], mainButtonsDims[1][3]);
		midiAmds.addActionListener(mb);
		midiAmds.setVisible(true);
		
		csvAmds = new JButton("To .amds");
		csvAmds.setBounds(mainButtonsDims[2][0], mainButtonsDims[2][1], mainButtonsDims[2][2], mainButtonsDims[2][3]);
		csvAmds.addActionListener(mb);
		csvAmds.setVisible(true);
		
		play = new JButton("Play");
		play.setBounds(mainButtonsDims[3][0], mainButtonsDims[3][1], mainButtonsDims[3][2], mainButtonsDims[3][3]);
		play.addActionListener(mb);
		play.setVisible(true);
		
		stop = new JButton("Stop");
		stop.setBounds(mainButtonsDims[4][0], mainButtonsDims[4][1], mainButtonsDims[4][2], mainButtonsDims[4][3]);
		stop.addActionListener(mb);
		stop.setVisible(true);
		
		connect = new JButton("Connect");
		connect.setBounds(mainButtonsDims[5][0], mainButtonsDims[5][1], mainButtonsDims[5][2], mainButtonsDims[5][3]);
		connect.addActionListener(mb);
		connect.setVisible(true);
		
		plyButtons(false);
		midButtons(false);
		csvButtons(false);
		
		add(midiCsv);
		add(midiAmds);
		add(csvAmds);
		add(play);
		add(stop);
		add(connect);
		
		// --------------- Añadir indicadores ---------------

		ready = new ImageIcon(this.getClass().getResource("/ready.png"));
		loaded = new ImageIcon(this.getClass().getResource("/loaded.png"));
		busy = new ImageIcon(this.getClass().getResource("/busy.png"));

		icons = new ImageIcon[] {ready, loaded, busy};
		
		statusMid = new JLabel(ready);
		statusAmds = new JLabel(ready);
		statusCsv = new JLabel(ready);
		
		statuses = new JLabel[] {statusMid, statusAmds, statusCsv};

		statusMid.setBounds(iconsPositions[0][0], iconsPositions[0][1], iconsPositions[0][2], iconsPositions[0][3]);
		statusAmds.setBounds(iconsPositions[1][0], iconsPositions[1][1], iconsPositions[1][2], iconsPositions[1][3]);
		statusCsv.setBounds(iconsPositions[2][0], iconsPositions[2][1], iconsPositions[2][2], iconsPositions[2][3]);
		
		add(statusMid);
		add(statusAmds);
		add(statusCsv);
		
		// ================== AÑADIR TEXTO E IMAGENES ==================

		JLabel title = new JLabel();
		title.setIcon(new ImageIcon(this.getClass().getResource("/logo.png")));
		title.setBounds(0, 0, 597, 125);
		title.setVerticalAlignment(JLabel.NORTH);
		title.addMouseListener(new AmadeusClick(this));
		
		JLabel sep1 = new JLabel();
		sep1.setBackground(Color.LIGHT_GRAY);
		sep1.setOpaque(true);
		sep1.setBounds(separators[0][0], separators[0][1], separators[0][2], separators[0][3]);

		JLabel sep2 = new JLabel();
		sep2.setBackground(Color.LIGHT_GRAY);
		sep2.setOpaque(true);
		sep2.setBounds(separators[1][0], separators[1][1], separators[1][2], separators[1][3]);
		
		JLabel sep3 = new JLabel();
		sep3.setBackground(Color.LIGHT_GRAY);
		sep3.setOpaque(true);
		sep3.setBounds(separators[2][0], separators[2][1], separators[2][2], separators[2][3]);
		
		JLabel sep4 = new JLabel();
		sep4.setBackground(Color.LIGHT_GRAY);
		sep4.setOpaque(true);
		sep4.setBounds(separators[3][0], separators[3][1], separators[3][2], separators[3][3]);
		
		cnsl = new JTextArea();				// The console will be the main mean of comunication between the user and the program
		cnsl.setEditable(false);
		cnsl.setBackground(Color.WHITE);
		cnsl.setLineWrap(true);
		
		JScrollPane scroll = new JScrollPane(cnsl, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBounds(separators[4][0], separators[4][1], separators[4][2], separators[4][3]);
		
		JLabel sgnt = new JLabel("RO ;P - 2022");
		sgnt.setBounds(510, 515, 100, 10);
		sgnt.setVerticalTextPosition(JLabel.NORTH);
		
        add(title);
        
        add(sep1);
        add(sep2);
		add(sep3);
        add(sep4);
		
        add(scroll);
        
        add(sgnt);
        
        setUpPrefs();
        
        // Initialize classes

        csvH  = new csvHandler(this);
        midH  = new midiHandler(this);
        amdsH = new amdsHandler(this);
        
        sc = new serialComunication(this);
        
		setVisible(true);
		
		setCnsl("Welcome :)");
	}
	
	public void setAll(int newStatus) { for (char i = 0; i < statuses.length; i++) statuses[i].setIcon(icons[newStatus]); }
	
	public void setStatus(int source, int newStatus) { statuses[source].setIcon(icons[newStatus]); }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setUpPrefs() {		// It is only executed once when the main window is started
		prefs = new JFrame("Preferences");
		
		prefs.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		prefs.setSize(500, 300);
		prefs.setLayout(null);
		prefs.setLocationRelativeTo(frame);
		
		prefs.addWindowListener(wm);
		
		JLabel prefTitle = new JLabel("Preferences");
		prefTitle.setBounds(10, 10, 150, 25);
		prefTitle.setFont(new Font("Verdana", Font.PLAIN, 25));
		
		JLabel vdLabel = new JLabel("Volume:");
		vdLabel.setBounds(50, 55, 100, 25);
		
		JLabel vlLabel = new JLabel(String.valueOf((int) pref.getVolume()));
		vlLabel.setBounds(100, 55, 100, 25);

		JLabel mdLabel = new JLabel("Audio mode");
		mdLabel.setBounds(50, 155, 100, 25);
		
		JLabel sep1 = new JLabel();
		sep1.setOpaque(true);
		sep1.setBackground(Color.LIGHT_GRAY);
		sep1.setBounds(prefBounds[5][0], prefBounds[5][1], prefBounds[5][2], prefBounds[5][3]);
		
		JLabel sep2 = new JLabel();
		sep2.setOpaque(true);
		sep2.setBackground(Color.LIGHT_GRAY);
		sep2.setBounds(prefBounds[6][0], prefBounds[6][1], prefBounds[6][2], prefBounds[6][3]);
		
		JLabel sep3 = new JLabel();
		sep3.setOpaque(true);
		sep3.setBackground(Color.LIGHT_GRAY);
		sep3.setBounds(prefBounds[7][0], prefBounds[7][1], prefBounds[7][2], prefBounds[7][3]);
		
		JLabel sep4 = new JLabel();
		sep4.setOpaque(true);
		sep4.setBackground(Color.LIGHT_GRAY);
		sep4.setBounds(prefBounds[9][0], prefBounds[9][1], prefBounds[9][2], prefBounds[9][3]);
		
		ScrollBarList sc = new ScrollBarList(this, vlLabel);
		
		prefVol = new JScrollBar(Adjustable.HORIZONTAL, pref.getVolume(), 1, 1, 16);
		prefVol.addAdjustmentListener(sc);
		prefVol.setBounds(prefBounds[0][0], prefBounds[0][1], prefBounds[0][2], prefBounds[0][3]);
		
		preguntar = new JCheckBox("Ask when more than 6 channels are used", null, pref.getPreguntar());
		preguntar.setBounds(prefBounds[1][0], prefBounds[1][1], prefBounds[1][2], prefBounds[1][3]);
		
		modo = new JComboBox(modos);
		modo.setBounds(prefBounds[8][0], prefBounds[8][1], prefBounds[8][2], prefBounds[8][3]);
		
		reset = new JButton("Reset");
		reset.setBounds(prefBounds[10][0], prefBounds[10][1], prefBounds[10][2], prefBounds[10][3]);
		
		apply = new JButton("Apply");
		apply.setBounds(prefBounds[2][0], prefBounds[2][1], prefBounds[2][2], prefBounds[2][3]);
		
		apExt = new JButton("Apply and Exit");
		apExt.setBounds(prefBounds[3][0], prefBounds[3][1], prefBounds[3][2], prefBounds[3][3]);
		
		exit = new JButton("Exit");
		exit.setBounds(prefBounds[4][0], prefBounds[4][1], prefBounds[4][2], prefBounds[4][3]);
		
		prefs.add(prefTitle);
		
		prefs.add(vdLabel);
		prefs.add(vlLabel);
		prefs.add(mdLabel);

		prefs.add(sep1);
		prefs.add(sep2);
		prefs.add(sep3);
		prefs.add(sep4);
		
		preferencesButtons pb = new preferencesButtons(this);
		
		reset.addActionListener(pb);
		apply.addActionListener(pb);
		apExt.addActionListener(pb);
		exit.addActionListener(pb);

		prefs.add(prefVol);
		prefs.add(preguntar);
		prefs.add(modo);
		
		prefs.add(reset);
		prefs.add(apply);
		prefs.add(apExt);
		prefs.add(exit);
		
		resetValues();
		
		prefs.setVisible(false);
	}
	
	public void togglePrefs() { prefs.setVisible(!prefs.isVisible()); resetValues(); }

	private void resetValues() {
		prefVol.setValue((int) pref.getVolume());
		preguntar.setSelected(pref.getPreguntar());
		modo.setSelectedIndex(pref.getMode());
	}
	
	public void applyChanges() {
		csvH.setVolumen((char) prefVol.getValue());
		csvH.setMode((char) modo.getSelectedIndex());
		
		pref.setVolume((char) prefVol.getValue());
		pref.setPreguntar(preguntar.isSelected());
		pref.setMode((char) modo.getSelectedIndex());
		
		pref.guardarConfiguracion();
	}

	public boolean checkPrefs() {
		return !(prefVol.getValue() == (int) pref.getVolume()) || !(preguntar.isSelected() == pref.getPreguntar()) || !(modo.getSelectedIndex() == pref.getMode());
	}

	public void showPrefsDialog() {
		int result = JOptionPane.showOptionDialog(prefs, "Are you sure you want to close preferences?", "Exit Preferences?",  JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[] {"Don't save", "Save", "Cancel"}, 2);
		
		switch(result) {
		case JOptionPane.NO_OPTION:		// The user wants to save and exit
			applyChanges();
		case JOptionPane.YES_OPTION:	// The user doesn't want to save
			togglePrefs();
			break;
		case JOptionPane.CANCEL_OPTION:	// The user made a terrible mistake and feels ashamed
		default:
			break;	// Do nothing, he will think about it :)
		}
	}
	
	public void midButtons(boolean b) { midiCsv.setEnabled(b); midiAmds.setEnabled(b); }
	public void csvButtons(boolean b) { csvAmds.setEnabled(b); }
	public void plyButtons(boolean b) { play.setEnabled(b);  stop.setEnabled(b); }
	
	public void setPlay()    { play.setText("Play"); } // Set to a know position
	public void togglePlay() { play.setText(play.getText().equals("Play") ? "Pause" : "Play"); }
	
	public void setCnsl(String txt) { cnsl.setText(txt); }
	public void appCnsl(String txt) { cnsl.setText(cnsl.getText() + (cnsl.getText().isEmpty() ? "" : '\n') + txt); }
	
	public preferences getPreferences() { return pref; }
	
	public serialComunication getSerial() { return sc; }

	public csvHandler  getCSV()  { return csvH;  }
	public midiHandler getMIDI() { return midH;  }
	public amdsHandler getAMDS() { return amdsH; }
}

class windowManager implements WindowListener {

	private Ventana v;
	
	public windowManager(Ventana _v) { v = _v; }
	
	@Override
	public void windowActivated(WindowEvent arg0) {}

	@Override
	public void windowClosed(WindowEvent e) {}

	@Override
	public void windowClosing(WindowEvent e) {		
		switch(((JFrame) e.getSource()).getTitle()) {
		case "Preferences":
			if (v.checkPrefs())			// Preferences to save
				v.showPrefsDialog();
			else						// Nothing to save
				v.togglePrefs();
			break;
		default:		// Main Window
			break;	
		}}

	@Override
	public void windowDeactivated(WindowEvent arg0) {}

	@Override
	public void windowDeiconified(WindowEvent arg0) {}

	@Override
	public void windowIconified(WindowEvent arg0) {}

	@Override
	public void windowOpened(WindowEvent arg0) {}
}

class fileButtons implements ActionListener {
	
	Ventana v;
	
	public fileButtons(Ventana _v) { v = _v; }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (((JMenuItem) e.getSource()).getText()) {
			case "Open":
				break;
			case "Preferences":
				v.togglePrefs();
				break;
			case "Exit":
				// TODO : EXIT PROCEDURE
				break;
			default:
				v.appCnsl("Unhandled" + ((JMenuItem) e.getSource()).getText());			
		}
	}
}

class helpButtons implements ActionListener {
	
	private Ventana v;
	
	public helpButtons(Ventana _v) { v = _v; }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (((JMenuItem) e.getSource()).getText()) {
		case "Help":
			// TODO : HANDLE HELP
			v.appCnsl("TODO : Handle Help");
			break;
		case "Tindie":
			openLink("https://www.tindie.com/products/derspanischinventions/ym2149-midi-chiptune-synthesizer-amadeus-board/");
			break;
		case "Youtube":
			openLink("https://www.youtube.com/user/ROBERTO1OOO");
			break;
		case "About":
			JOptionPane.showMessageDialog(null, "Amadeus Comunicator \n\n Developped by: ROBERTO1OOO \n\n Version: " + v.getPreferences().getVersion(), "About", 1, new ImageIcon("src/logo.png"));
			break;
		default:
			v.appCnsl("Unhandlded button " + ((JMenuItem) e.getSource()).getText());
		}
	}
	
	private void openLink(String url) {
		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
		    try {
				Desktop.getDesktop().browse(new URI(url));
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (URISyntaxException e1) {
				e1.printStackTrace();
			}
		}
	}
}

class mainButtons implements ActionListener {
	
	private final Ventana v;
	
	private final JFileChooser fc;
	
	private int returnVal;
	
	// Filtros
    private final FileNameExtensionFilter midFil = new FileNameExtensionFilter("MIDI Files", "mid");
    private final FileNameExtensionFilter csvFil = new FileNameExtensionFilter("CSV Files", "csv");
    private final FileNameExtensionFilter amdsFil = new FileNameExtensionFilter("Amadeus Files", "amds");
	
	public mainButtons(Ventana _v) {
		v = _v;
		
		fc = new JFileChooser();						// Create new instance of the file chooser
		fc.setAcceptAllFileFilterUsed(false);			// It will always be filtered
		fc.setMultiSelectionEnabled(false);				// Disable multifile selection
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (v.getPreferences().getMusicPath() != "null")
			fc.setCurrentDirectory(new File(v.getPreferences().getMusicPath()));
		
		String id = ((JButton) e.getSource()).getText();	// Get the text in the button to decide the action
		
		switch (id) {	// TODO : AÑADIR FUNCIONES PARA TODOS ESTOS
			case "Open midi file":
				v.getMIDI().processNewFile(getFile(midFil, "Select a .mid file", true));
				break;
			case "Open csv file":
				v.getCSV().processNewFile(getFile(csvFil, "Select a .csv file", true));
				break;
			case "Open amds file":
				v.getAMDS().processNewFile(getFile(amdsFil, "Select a .amds file", true));
				break;
			case "To .csv":
				fc.setSelectedFile(new File(v.getMIDI().getDir() + ".csv"));	// Set the directory not to the last search but to the last file
				v.getMIDI().toCsv(getFile(csvFil, "Select a .csv file", false));
				break;
			case "To .amds":
				// TODO : CHECK IF IT COMES FROM .mid or .csv
			case "Play":
				v.getSerial().play();
				break;
			case "Pause":
				v.getSerial().pause();
				break;
			case "Stop":
				v.getSerial().stop();
				break;
			case "Connect":
				v.appCnsl("Attempting to connect...");
				
				if (v.getSerial().connectPort())
					((JButton) e.getSource()).setText("Disconnect");	// If it has successfully connected, then change the button to say Disconnect
				break;
			case "Disconnect":
				v.setCnsl("Disconnecting");
				
				if (v.getSerial().disconnectPort())
					((JButton) e.getSource()).setText("Connect");		// If it has successfully connected then change the button to say Connect
				break;
			default:
				System.out.println("Unhandled button " + id);
		}
	}
	
	private File getFile(FileNameExtensionFilter ff, String title, boolean open) {
		File file = null;
		
		fc.setDialogTitle(title);
		
		fc.resetChoosableFileFilters();
		fc.addChoosableFileFilter(ff);
		
		if (open)
			returnVal = fc.showOpenDialog(v);
		else
			returnVal = fc.showSaveDialog(v);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		}
		
		if (file != null)
			v.getPreferences().setNewPath(file.getParent());
		return file;
	}
}


class AmadeusClick implements MouseListener {
	
	private Ventana v;
	
	public AmadeusClick(Ventana _v) { v = _v; }

	@Override
	public void mouseClicked(MouseEvent arg0) {		// Silly Easter Egg that I like - Feeling cute, might add "Dark mode" later
		v.getContentPane().setBackground((v.getContentPane().getBackground() == Color.DARK_GRAY) ? new Color(238, 238, 238) : Color.DARK_GRAY);
	}

	// Silly but nice animation :3
	
	@Override
	public void mouseEntered(MouseEvent e) {
		Component logo = v.getContentPane().getComponentAt(10, 10);
		logo.setBounds(-2, -2, 597, 125);	
	}

	@Override
	public void mouseExited(MouseEvent e) {
		Component logo = v.getContentPane().getComponentAt(10, 10);
		logo.setBounds(0, 0, 597, 125);
	}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}
}

class ScrollBarList implements AdjustmentListener {
	
	private final JLabel label;
	
	public ScrollBarList(Ventana _v, JLabel l) { label = l; }
	
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		label.setText(Integer.toString(((JScrollBar) e.getSource()).getValue()));
	}
}

class preferencesButtons implements ActionListener {
	
	private Ventana v;

	public preferencesButtons(Ventana _v) { v = _v; }
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch(((JButton) e.getSource()).getText()) {
		case "Reset":
			v.getPreferences().resetConfig();
			v.togglePrefs();
			break;
		case "Apply and Exit":
			v.applyChanges();
			v.togglePrefs();
			break;
		case "Apply":
			v.applyChanges();
			break;
		case "Exit":
			if (!v.checkPrefs())	// If the user hasn't touch anything, leave
				v.togglePrefs();
			else					// If not, ask
				v.showPrefsDialog();
			break;
		default:
			v.appCnsl("Unhandled button: " + ((JButton) e.getSource()).getText());
			break;
			
		}
	}
}