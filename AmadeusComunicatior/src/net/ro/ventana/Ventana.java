package net.ro.ventana;

import net.ro.io.*;
import net.ro.main.*;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("serial")
public class Ventana extends JFrame {

	private final preferences pref;
	
	private final JFrame frame;
	
	// Menu bar buttons
	
	private final fileButtons fb;
	private final loadButtons lb;
	private final helpButtons hb;
	
	// Main buttons

	private final JButton openMIDI;
	private final JButton openCSV;
	private final JButton openAMDS;
	
	private static final int[][] selectButtonsDims = {
			{100, 150, 125, 25},		// Select .mid
			{100, 250, 125, 25},		// Select .csv
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
			{400, 250, 100, 25},	// .csv to .amds
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
		
		JMenu fileMenu = new JMenu("File");		// Declarar nuevo menu
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenuItem fileOpen = new JMenuItem("Open", KeyEvent.VK_O);
		fileOpen.setAccelerator(KeyStroke.getKeyStroke(
		        KeyEvent.VK_O, ActionEvent.CTRL_MASK));
		JMenuItem fileSave = new JMenuItem("Save", KeyEvent.VK_S);
		fileSave.setAccelerator(KeyStroke.getKeyStroke(
			        KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		JMenuItem fileSaveAs = new JMenuItem("Save As");
		fileSaveAs.setAccelerator(KeyStroke.getKeyStroke(
			        KeyEvent.VK_S, ActionEvent.ALT_MASK));
		JMenuItem filePreferences = new JMenuItem("Preferences", KeyEvent.VK_P);
		JMenuItem fileExit = new JMenuItem("Exit", KeyEvent.VK_E);
		fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));
		
		// Añadirlos items al menu
		
		fileMenu.add(fileOpen);
		fileMenu.add(fileSave);
		fileMenu.add(fileSaveAs);
		fileMenu.addSeparator();
		fileMenu.add(filePreferences);
		fileMenu.addSeparator();
		fileMenu.add(fileExit);
		
		// Añadir las funciones - TODO : REHACER CON LAS FUNCIONES QUE DE VERDAD HAY
		
		fb = new fileButtons();
		
		fileOpen.addActionListener(fb);
		fileSave.addActionListener(fb);
		fileSaveAs.addActionListener(fb);
		filePreferences.addActionListener(fb);
		fileExit.addActionListener(fb);
		
		// --------------- Load Menu ---------------
		
		// Declaring items
		
		JMenu loadMenu = new JMenu("Load");
		loadMenu.setMnemonic(KeyEvent.VK_L);

		JMenuItem loadMIDI = new JMenuItem("Load MIDI", KeyEvent.VK_M);
		JMenuItem loadCSV = new JMenuItem("Load CSV", KeyEvent.VK_C);
		JMenuItem loadAmadeus = new JMenuItem("Load Amadeus", KeyEvent.VK_A);
		
		// Añadirlos al menu

		loadMenu.add(loadCSV);
		loadMenu.add(loadMIDI);
		loadMenu.add(loadAmadeus);
		
		// Añadir las funciones
		
		lb = new loadButtons();

		loadMIDI.addActionListener(lb);
		loadCSV.addActionListener(lb);
		loadAmadeus.addActionListener(lb);
		
		// --------------- Help Menu ---------------
		
		// Declaracion items
		
		JMenu helpMenu = new JMenu("Help");			// Declarar nuevo menu
		helpMenu.setMnemonic(KeyEvent.VK_H);
		
		JMenuItem helpTd = new JMenuItem("Tindie", KeyEvent.VK_T);
		JMenuItem helpYT = new JMenuItem("Youtube", KeyEvent.VK_Y);
		JMenuItem helpAbout = new JMenuItem("About", KeyEvent.VK_A);
		
		// Añadirlos al menu

		helpMenu.add(helpTd);
		helpMenu.add(helpYT);
		helpMenu.addSeparator();
		helpMenu.add(helpAbout);
		
		// Añadir las funciones
		
		hb = new helpButtons();

		helpTd.addActionListener(hb);
		helpYT.addActionListener(hb);
		helpAbout.addActionListener(hb);
		
		// --------------- Añadir menus -------------------

		menubar.add(fileMenu);
		menubar.add(loadMenu);
		menubar.add(helpMenu);
		
		setJMenuBar(menubar); // Acabar poniendo la menu
		
		// =============== MAIN WINDOW ====================
		
		mb = new mainButtons(pref, this);
		
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
		title.addMouseListener(new AmadeusClick(frame));
		
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
		
		JTextField cnsl = new JTextField();
		cnsl.setEditable(false);
		cnsl.setBackground(Color.WHITE);
		cnsl.setBounds(separators[4][0], separators[4][1], separators[4][2], separators[4][3]);
		
		JLabel sgnt = new JLabel("RO ;P - 2022");
		sgnt.setBounds(510, 515, 100, 10);
		sgnt.setVerticalTextPosition(JLabel.NORTH);
		
        add(title);
        
        add(sep1);
        add(sep2);
		add(sep3);
        add(sep4);
		
        add(cnsl);
        
        add(sgnt);
        
		setVisible(true);
	}
	
	public void changeTitle(String newTitle) {
		setTitle(newTitle);
	}
}

class fileButtons implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();
		System.out.println("File " + source.getText());	
	}
	
}

class loadButtons implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();
		System.out.println("Load " + source.getText());
	}
	
}

class helpButtons implements ActionListener {
	
	@Override
	public void actionPerformed(ActionEvent e) {
		switch (((JMenuItem) e.getSource()).getText()) {
		case "Tindie":
			openLink("https://www.tindie.com/products/derspanischinventions/ym2149-midi-chiptune-synthesizer-amadeus-board/");
		case "Youtube":
			openLink("https://www.youtube.com/user/ROBERTO1OOO");
			break;
		case "About":
			break;
		default:
			System.out.println("Unhandlded button " + ((JMenuItem) e.getSource()).getText());
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
	
	private final preferences pref;
	private final JFrame frame;
	
	private final JFileChooser fc;
	
	private int returnVal;
	
	// Handlers
	private final midiHandler midH = new midiHandler();
	private final csvHandler csvH =  new csvHandler();
	private final amdsHandler amdsH = new amdsHandler();
	
	// Filtros
    private final FileNameExtensionFilter midFil = new FileNameExtensionFilter("MIDI Files", "mid");
    private final FileNameExtensionFilter csvFil = new FileNameExtensionFilter("CSV Files", "csv");
    private final FileNameExtensionFilter amdsFil = new FileNameExtensionFilter("Amadeus Files", "amds");
	
	public mainButtons(preferences _pref, JFrame _frame) {
		pref = _pref;
		frame = _frame;
		
		fc = new JFileChooser();						// Create new instance of the file chooser
		fc.setAcceptAllFileFilterUsed(false);			// It will always be filtered
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (pref.getMusicPath() != "null")
			fc.setCurrentDirectory(new File(pref.getMusicPath()));
		
		String id = ((JButton) e.getSource()).getText();	// Get the text in the button to decide the action
		
		switch (id) {	// TODO : AÑADIR FUNCIONES PARA TODOS ESTOS
		case "Open midi file":
			midH.processNewFile(getFile(midFil));
			break;
		case "Open csv file":
			csvH.processNewFile(getFile(csvFil));
			break;
		case "Open amds file":
			amdsH.processNewFile(getFile(amdsFil));
			break;
		default:
			System.out.println("Unhandled button " + id);
		}
	}
	
	private File getFile(FileNameExtensionFilter ff) {
		File file = null;
		returnVal = fc.showOpenDialog(frame);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
		}
			
		pref.setNewPath(file);
		return file;
	}
}


class AmadeusClick implements MouseListener {
	private JFrame frame;
	
	public AmadeusClick(JFrame _frame) { frame = _frame; }

	@Override
	public void mouseClicked(MouseEvent arg0) {		// Silly Easter Egg that I like - Feeling cute, might add "Dark mode" later
		frame.getContentPane().setBackground((frame.getContentPane().getBackground() == Color.DARK_GRAY) ? new Color(238, 238, 238) : Color.DARK_GRAY);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
}