package net.ro.ventana;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class Ventana extends JFrame {
	
	private final JFrame frame;
	
	// Menu bar buttons
	
	private final fileButtons fb;
	private final loadButtons lb;
	private final helpButtons hb;
	
	// Main buttons
	
	private final JButton openCSV;
	private final JButton openMIDI;
	private final JButton openAMDS;
	
	private static final int[][] button = {
			{0, 0, 100, 25},
			{0, 50, 100, 25},
			{0, 100, 100, 25}
	};
	
	private final mainButtons mb;
	
	// Otras variables
	
	private boolean preguntaCerrar = false;
	
	
	public Ventana() {
		frame = this;
		
		setTitle("Amadeus");
		setSize(600, 600);
		setResizable(false);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setLayout(null);
		
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
		fileMenu.add(filePreferences);
		fileMenu.add(fileExit);
		
		// Añadir las funciones - TODO : REHACER
		
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
		
		JMenuItem helpYT = new JMenuItem("Youtube", KeyEvent.VK_Y);
		JMenuItem helpAbout = new JMenuItem("About", KeyEvent.VK_A);
		
		// Añadirlos al menu

		helpMenu.add(helpYT);
		helpMenu.add(helpAbout);
		
		// Añadir las funciones
		
		hb = new helpButtons();

		helpYT.addActionListener(hb);
		helpAbout.addActionListener(hb);
		
		// --------------- Añadir menus -------------------

		menubar.add(fileMenu);
		menubar.add(loadMenu);
		menubar.add(helpMenu);
		
		setJMenuBar(menubar); // Acabar poniendo la menu
		
		// =============== MAIN WINDOW ====================
		
		mb = new mainButtons(new JFileChooser(), this);
		
		openMIDI = new JButton("Open midi file");
		openMIDI.setBounds(button[0][0], button[0][1], button[0][2], button[0][3]);
		openMIDI.addActionListener(mb);
		openMIDI.setVisible(true);
		
		openCSV = new JButton("Open csv file");
		openCSV.setBounds(button[1][0], button[1][1], button[1][2], button[1][3]);
		openCSV.addActionListener(mb);
		openCSV.setVisible(true);
		
		openAMDS = new JButton("Open amds file");
		openAMDS.setBounds(button[2][0], button[2][1], button[2][2], button[2][3]);
		openAMDS.addActionListener(mb);
		openAMDS.setVisible(true);

		add(openCSV);
		add(openMIDI);
		add(openAMDS);
		
		setVisible(true);
	}
	
	public void changeTitle(String newTitle) {
		setTitle(newTitle);
	}
}

class fileButtons implements ActionListener, ItemListener {
	
	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();
		System.out.println("File " + source.getText());	
	}
	
}

class loadButtons implements ActionListener, ItemListener {

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();
		System.out.println("Load " + source.getText());	
		
	}
	
}

class helpButtons implements ActionListener, ItemListener {

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JMenuItem source = (JMenuItem) e.getSource();
		System.out.println("Help " + source.getText());
	}
	
}

class mainButtons implements ActionListener {
	// TOOD : FIX
	
	private final JFileChooser fc;
	private final JFrame frame;
	
	public mainButtons(JFileChooser _fc, JFrame _frame) {
		fc = _fc;
		frame = _frame;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		String id = ((JButton) e.getSource()).getText();
		
		switch (id) {
		case "Open midi file":
			break;
		case "Open csv file":
			break;
		case "Open amds file":
			break;
		default:
			System.out.println("Unhandled button " + id);
		}
	        int returnVal = fc.showOpenDialog(frame);

	        if (returnVal == JFileChooser.APPROVE_OPTION) {
	            File file = fc.getSelectedFile();
	   }
	}
	
}