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
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
public class Ventana extends JFrame {

	private final preferences pref;
	
	private final JFrame frame;
	
	// Menu bar buttons
	
	private final fileButtons fb;
	private final helpButtons hb;
	
	// Main buttons

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
	
	private static final int[][] mainButtonsDims = {
			{400, 125, 100, 25},	// .mid to .csv
			{400, 175, 100, 25},	// .mid to .amds
			{400, 242, 100, 25},	// .csv to .amds
			{450, 350, 100, 25},	//	Play / Pause
			{450, 400, 100, 25},	//	Stop
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
	
	// Menubar
	
	private final JMenu fileMenu;
	
	private final JMenuItem fileOpen;
	private final JMenuItem filePreferences;
	private final JMenuItem fileExit;
	
	private final JMenu helpMenu;

	private final JMenuItem helpHelp;
	private final JMenuItem helpTd;
	private final JMenuItem helpYT;
	private final JMenuItem helpAbout;
	
	// Preferences
	
	private JFrame prefs;
	
	private JScrollBar prefVol;
	private JCheckBox preguntar;
	
	private JButton apply;
	private JButton apExt;
	private JButton exit;
	
	private int[][] prefBounds = {
			{50, 60, 200, 15},			// Volume scrollbar
			{50, 120, 300, 25},			// Checkbox
			{150, 200, 75, 25},			// Apply button
			{250, 200, 125, 25},		// Apply and Exit button
			{400, 200, 75, 25},			// Exit
			{0, 50, 500, thickness},	// Title / volume
			{0, 100, 500, thickness},	// volume / check
			{0, 175, 500, thickness}	// check / buttons
	};
	
	// Otras variables
	
	private boolean preguntaCerrar = false;
	
	public Ventana(preferences _pref) {
		pref = _pref;
		frame = this;
		
		setTitle("Amadeus");
		setSize(600, 600);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
        setLayout(null);
        this.setOpacity(1);
        
		addWindowListener(new java.awt.event.WindowAdapter() {		// Funcion para cuando se cierre la ventana
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	// TODO : PONER UNA FUNCION AQUI QUE SE ENCARGUE DE CERRARLO todo 
		    	
		    	if (preguntaCerrar) {
			        if (JOptionPane.showConfirmDialog(frame, 
			            "Are you sure you want to close this window?", "Close Window?", 
			            JOptionPane.YES_NO_OPTION,
			            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
			            System.exit(0);
			        }
			    }
		    }
		});
		
		// --------------- Diseño del menu ---------------
		
		JMenuBar menubar = new JMenuBar();
		
		// --------------- File Menu ---------------
		
		// Declaracion de los items
		
		fileMenu = new JMenu("File");		// Declarar nuevo menu
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		fileOpen = new JMenuItem("Open", KeyEvent.VK_O);
		fileOpen.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		filePreferences = new JMenuItem("Preferences", KeyEvent.VK_P);
		fileExit = new JMenuItem("Exit", KeyEvent.VK_E);
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		
		// Añadirlos items al menu
		
		fileMenu.add(fileOpen);
		fileMenu.addSeparator();
		fileMenu.add(filePreferences);
		fileMenu.addSeparator();
		fileMenu.add(fileExit);
		
		// Añadir las funciones - TODO : REHACER CON LAS FUNCIONES QUE DE VERDAD HAY
		
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
		
		// ================== AÑADIR TEXTO E IMAGENES ==================

		JLabel title = new JLabel();
		title.setIcon(new ImageIcon("src/logo.png"));
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
        
		setVisible(true);
		
		setCnsl("Welcome :)");
	}
	
	private void setUpPrefs() {
		prefs = new JFrame("Preferences");
		prefs.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		prefs.setSize(500, 300);
		prefs.setLayout(null);
		prefs.setLocationRelativeTo(frame);
		
		JLabel prefTitle = new JLabel("Preferences");
		prefTitle.setBounds(0, 0, 150, 25);
		prefTitle.setFont(new Font("Verdana", Font.PLAIN, 18));
		
		JLabel vdLabel = new JLabel("Volume:");
		vdLabel.setBounds(260, 55, 100, 25);
		
		JLabel vlLabel = new JLabel(Integer.toString((int) pref.getVolume()));
		vlLabel.setBounds(310, 55, 100, 25);

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
		
		ScrollBarList sc = new ScrollBarList(vlLabel);
		
		prefVol = new JScrollBar(Adjustable.HORIZONTAL, pref.getVolume(), 1, 1, 16);
		prefVol.addAdjustmentListener(sc);
		prefVol.setBounds(prefBounds[0][0], prefBounds[0][1], prefBounds[0][2], prefBounds[0][3]);
		
		preguntar = new JCheckBox("Ask when more than 6 channels are used", null, pref.getPreguntar());
		preguntar.setBounds(prefBounds[1][0], prefBounds[1][1], prefBounds[1][2], prefBounds[1][3]);
		
		apply = new JButton("Apply");
		apply.setBounds(prefBounds[2][0], prefBounds[2][1], prefBounds[2][2], prefBounds[2][3]);
		
		apExt = new JButton("Apply and Exit");
		apExt.setBounds(prefBounds[3][0], prefBounds[3][1], prefBounds[3][2], prefBounds[3][3]);
		
		exit = new JButton("Exit");
		exit.setBounds(prefBounds[4][0], prefBounds[4][1], prefBounds[4][2], prefBounds[4][3]);
		
		preferencesButtons pb = new preferencesButtons(this);

		prefs.add(prefTitle);
		
		prefs.add(vdLabel);
		prefs.add(vlLabel);

		prefs.add(sep1);
		prefs.add(sep2);
		prefs.add(sep3);
		
		apply.addActionListener(pb);
		apExt.addActionListener(pb);
		exit.addActionListener(pb);

		prefs.add(prefVol);
		prefs.add(preguntar);

		prefs.add(apply);
		prefs.add(apExt);
		prefs.add(exit);
		
		prefs.setVisible(false);
	}
	
	public void togglePrefs() { prefs.setVisible(!prefs.isVisible()); }
	
	public void applyChanges() {
		pref.setVolume((char) prefVol.getValue());
		pref.setPreguntar(preguntar.isSelected());
		
		pref.guardarConfiguracion();
	}
	
	public void midButtons(boolean b) { midiCsv.setEnabled(b); midiAmds.setEnabled(b); }
	public void csvButtons(boolean b) { csvAmds.setEnabled(b); }
	public void plyButtons(boolean b) { play.setEnabled(b);  stop.setEnabled(b); }
	
	public void setCnsl(String txt) { cnsl.setText(txt); }
	public void appCnsl(String txt) { cnsl.setText(cnsl.getText() + (cnsl.getText().isEmpty() ? "" : '\n') + txt); }
	
	public preferences getPreferences() { return pref; }
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
				break;
			default:
				v.appCnsl("Unhandled" + ((JMenuItem) e.getSource()).getText());			
		}
	}
}

class helpButtons implements ActionListener {
	
	private Ventana v;
	
	public helpButtons(Ventana _v) {v = _v;}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (((JMenuItem) e.getSource()).getText()) {
		case "Help":
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
	
	// Handlers
	private final midiHandler midH;
	private final csvHandler csvH;
	private final amdsHandler amdsH;
	
	// Filtros
    private final FileNameExtensionFilter midFil = new FileNameExtensionFilter("MIDI Files", "mid");
    private final FileNameExtensionFilter csvFil = new FileNameExtensionFilter("CSV Files", "csv");
    private final FileNameExtensionFilter amdsFil = new FileNameExtensionFilter("Amadeus Files", "amds");
	
	public mainButtons(Ventana _v) {
		v = _v;
		
		midH = new midiHandler(v);
		csvH = new csvHandler(v);
		amdsH = new amdsHandler(v);
		
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
			midH.processNewFile(getFile(midFil, "Select a .mid file", true));
			break;
		case "Open csv file":
			csvH.processNewFile(getFile(csvFil, "Select a .csv file", true));
			break;
		case "Open amds file":
			amdsH.processNewFile(getFile(amdsFil, "Select a .amds file", true));
			break;
		case "To .csv":
			fc.setSelectedFile(new File(midH.getDir() + ".csv"));	// Set the directory not to the last search but to the last file
			midH.toCsv(getFile(csvFil, "Select a .csv file", false));
			break;
		case "To .amds":
			// TODO : CHECK IF IT COMES FROM .mid or .csv
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
	
	public ScrollBarList(JLabel l) { label = l; }
	
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
		case "Apply and Exit":
			v.togglePrefs();
		case "Apply":
			v.applyChanges();
			break;
		case "Exit":
			// TODO : IF HE WANTS TO EXIT
			v.togglePrefs();
			break;
		default:
			v.appCnsl("Unhandled button: " + ((JButton) e.getSource()).getText());
			break;
			
		}
	}
}