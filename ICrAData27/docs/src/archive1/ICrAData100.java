/**
 * 
 * InterCriteria Analysis Data
 * 
 * Author: Nikolay Ikonomov
 * Version: 1.0.0
 * Date: January 25, 2018
 * 
 */

package icradata;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;

@SuppressWarnings("serial")
public class ICrAData extends JFrame {
	
	/// Main entry point
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ICrAData();
			}
		});
	}
	
	/// Global variables
	private String ud = System.getProperty("user.dir");
	//private String fs = System.getProperty("file.separator");
	/// JTextArea internally uses \n which is in conflict with line.separator
	/// http://docs.oracle.com/javase/8/docs/api/javax/swing/text/DefaultEditorKit.html
	private String ls = "\n"; //System.getProperty("line.separator");
	
	/// DecimalFormat - # is optional, while 0 is always written - ###.#### or 000.0000
	private DecimalFormat numFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
	private SimpleDateFormat logDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Font monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 12);
	private Font monoFont2 = new Font(Font.MONOSPACED, Font.PLAIN, 14);
	private Font sansFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
	private Font sansFont2 = new Font(Font.SANS_SERIF, Font.BOLD, 18);
	private Color backClr = new Color(238, 238, 238);
	private String MU = "\u03BC";
	private String NU = "\u03BD";
	
	/// Result of the calculations
	private HashMap<String, double[][]> result = null;
	private File fileName = null;
	private Point pp = new Point();
	
	private JTextArea textA, textB;
	private JScrollPane scrollA, scrollB, resScroll;
	private JComboBox<String> comboType, comboAggr, comboMethod, comboTable, comboSize, comboGrid;
	private JCheckBox chkOrdPair, chkTranspose, chkCount;
	private JSpinner spinMatCount, spinPoints;
	private JPopupMenu rightMouse;
	
	private String[] typeNames = new String[] {"Standard ICrA", "Second Order ICrA", "Aggregated ICrA"};
	private String[] aggrNames = new String[] {"Average", "MaxMin", "MinMax"};
	private String[] methodNames = new String[] {MU + "-biased", "Unbiased", NU + "-biased", "Balanced", "Weighted"};
	private String[] sizeDim = new String[] {"400", "600", "800", "1000", "2000", "3000", "5000"};
	private String[] objNames = null; /// Object headers
	private String[] critNames = null; /// Criteria headers
	private String[] objNames2 = null; /// Only for second order
	private String[] critNames2 = null; /// Only for second order
	private int valSize = 400; /// Plot size in pixels
	private int pointSize = 5; /// Point size of the plot
	
	/// Glass points, double[] saves X, Y, width, height of the rectangle
	private Vector<double[]> glassA = new Vector<double[]>();
	private Vector<double[]> glassB = new Vector<double[]>();
	private MyGlass glass;
	
	/// Colors for the tables
	private String[] colors = new String[] {
			"#FF0000", // 0 Red
			"#FF00FF", // 1 Magenta
			"#9370DB", // 2 DarkMagenta
			"#FFA500", // 3 Orange
			"#FF6347", // 4 Tomato
			"#808000", // 5 Olive
			"#87CEFA", // 6 LightSkyBlue
			"#1E90FF", // 7 DodgerBlue
			"#0000FF", // 8 Blue
			"#00FF7F", // 9 SpringGreen
			"#008000" // 10 Green
	};
	/*private String[] colors = new String[] {
			"#FF0000", // 0 Red gp
			"#FFA500", // 1 Orange gp
			"#FF6347", // 2 Tomato w3
			"#FF00FF", // 3 Magenta gp
			"#9932CC", // 4 DarkOrchid w3
			"#A52A2A", // 5 Brown w3
			"#87CEEB", // 6 LightSkyBlue w3
			"#1E90FF", // 7 DodgerBlue w3
			"#0000FF", // 8 Blue gp
			"#00C000", // 9 WebGreen gp
			"#008040" // 10 DarkSpringGreen gp
	};*/
	
	
	/// Constructor
	private ICrAData() {
		
		/// This is required
		Locale.setDefault(Locale.US);
		
		/// Look and feel
		try {
			/// Bold mono fonts are better on Windows
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				monoFont = new Font(Font.MONOSPACED, Font.BOLD, 12);
				monoFont2 = new Font(Font.MONOSPACED, Font.BOLD, 14);
			} else
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		} catch (Exception ex) {
			UIManager.put("swing.boldMetal", Boolean.FALSE);
		}
		
		/// Set tool-tips to show after half a second and stay 60 seconds
		ToolTipManager.sharedInstance().setInitialDelay(500);
		ToolTipManager.sharedInstance().setDismissDelay(60000);
		
		
		/// Variables for the constructor
		Dimension dim70 = new Dimension(70, 25);
		Dimension dim100 = new Dimension(100, 25);
		Dimension dim125 = new Dimension(125, 25);
		Dimension dim150 = new Dimension(150, 25);
		Dimension scrollSize = new Dimension(500, 300);
		//Dimension frameSize = new Dimension(1000, 700);
		
		/// Items for panel 1
		JButton btnOpen = new JButton("Open File");
		btnOpen.setToolTipText("Open a file.");
		btnOpen.setPreferredSize(dim150);
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnOpenListener(evt);
			}
		});
		
		JButton btnSave = new JButton("Save File");
		btnSave.setToolTipText("Save the opened file, or choose a name for a new file.");
		btnSave.setPreferredSize(dim150);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveListener(evt);
			}
		});
		
		JButton btnSave2 = new JButton("Save Copy");
		btnSave2.setToolTipText("Save a copy of the text in the input panel, under a new file name.");
		btnSave2.setPreferredSize(dim150);
		btnSave2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnSaveCopyListener(evt);
			}
		});
		
		comboType = new JComboBox<String>(typeNames);
		comboType.setToolTipText("Select an ICrA type for the input data.");
		comboType.setPreferredSize(dim150);
		comboType.setFont(monoFont);
		comboType.addItemListener(new ComboTypeListener());
		
		comboAggr = new JComboBox<String>(aggrNames);
		comboAggr.setToolTipText("Select aggregation type.");
		comboAggr.setPreferredSize(dim150);
		comboAggr.setFont(monoFont);
		comboAggr.setVisible(false);
		
		spinMatCount = new JSpinner(new SpinnerNumberModel(3,3,10000,1));
		spinMatCount.setToolTipText("Select the number of matrices. Minimum of 3 matrices.");
		spinMatCount.setPreferredSize(dim70);
		spinMatCount.setFont(monoFont);
		spinMatCount.setVisible(false);
		
		chkOrdPair = new JCheckBox("Ordered Pair (" + MU + "," + NU + ")");
		chkOrdPair.setToolTipText("<html>Input two sets of data to load as ordered pair (" + MU + "," + NU + "). " +
				"Data after #input1 is for " + MU + ", while data after #input2 is for " + NU + ".<br/>" +
					"When using Second Order / Aggregated, input two data sets of at least three matrices each.</html>");
		chkOrdPair.setBackground(Color.WHITE);
		chkOrdPair.setFont(monoFont);
		
		chkTranspose = new JCheckBox("Transpose Matrix");
		chkTranspose.setToolTipText("Transpose each matrix independently.");
		chkTranspose.setBackground(Color.WHITE);
		chkTranspose.setFont(monoFont);
		
		/// Items for panel 2
		JButton btnAnalysis = new JButton("Analysis");
		btnAnalysis.setToolTipText("Make the calculations and display them.");
		btnAnalysis.setPreferredSize(dim150);
		btnAnalysis.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAnalysisListener(evt);
			}
		});
		
		JButton btnViewData = new JButton("View Data");
		btnViewData.setToolTipText("View the matrix data.");
		btnViewData.setPreferredSize(dim150);
		btnViewData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnViewDataListener(evt);
			}
		});
		
		JButton btnAbout = new JButton("About");
		btnAbout.setToolTipText("About the program.");
		btnAbout.setPreferredSize(dim150);
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnAboutListener(evt);
			}
		});
		
		comboMethod = new JComboBox<String>(methodNames);
		comboMethod.setToolTipText("Select a method for the calculations.");
		comboMethod.setPreferredSize(dim150);
		comboMethod.setFont(monoFont);
		
		comboTable = new JComboBox<String>(new String[] {"Primary View", "Secondary View"});
		comboTable.setToolTipText("Select a view for the tables.");
		comboTable.setPreferredSize(dim150);
		comboTable.setFont(monoFont);
		comboTable.addItemListener(new ComboTableListener());
		
		JSpinner spinDigits = new JSpinner(new SpinnerNumberModel(4,1,20,1));
		spinDigits.setToolTipText("Select the number of digits after the decimal separator.");
		spinDigits.setPreferredSize(dim70);
		spinDigits.setFont(monoFont);
		spinDigits.addChangeListener(new SpinDigitsListener());
		
		chkCount = new JCheckBox("Count");
		chkCount.setToolTipText("<html>Ordered pair - count the incomparable values. " +
				"This is connected with the method selector.<br/>" +
				"Affects only " + MU + "-biased, " + NU + "-biased and Balanced. " +
				"See the documentation for explanation.</html>");
		chkCount.setBackground(backClr);
		chkCount.setFont(monoFont);
		
		/// Items for panel 3
		comboSize = new JComboBox<String>(sizeDim);
		comboSize.setToolTipText("Select the plot size in pixels or input a value from 100 to 100000.");
		comboSize.setPreferredSize(dim100);
		comboSize.setFont(monoFont);
		comboSize.setEditable(true);
		comboSize.addItemListener(new ComboSizeListener());
		
		spinPoints = new JSpinner(new SpinnerNumberModel(5,1,20,1));
		spinPoints.setToolTipText("Select the size of the plot points.");
		spinPoints.setPreferredSize(dim70);
		spinPoints.setFont(monoFont);
		spinPoints.addChangeListener(new SpinPointsListener());
		
		comboGrid = new JComboBox<String>(new String[] {"Triangle", "Ticks", "Grid", "Text"});
		comboGrid.setToolTipText("Select the plot type.");
		comboGrid.setPreferredSize(dim100);
		comboGrid.setFont(monoFont);
		comboGrid.addItemListener(new ComboGridListener());
		
		JButton btnExport = new JButton("Export");
		btnExport.setToolTipText("Export the graphic.");
		btnExport.setPreferredSize(dim125);
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				btnExportListener(evt);
			}
		});
		
		/// Panel 1A
		JPanel panel1a = new JPanel();
		panel1a.setBackground(Color.WHITE);
		panel1a.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel1a.add(btnOpen);
		panel1a.add(btnSave);
		panel1a.add(btnSave2);
		
		/// Panel 1B
		JPanel panel1b = new JPanel();
		panel1b.setBackground(Color.WHITE);
		panel1b.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel1b.add(comboType);
		panel1b.add(comboAggr);
		panel1b.add(spinMatCount);
		
		/// Panel 1C
		JPanel panel1c = new JPanel();
		panel1c.setBackground(Color.WHITE);
		panel1c.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel1c.add(chkOrdPair);
		panel1c.add(chkTranspose);
		
		/// Panel 1
		JPanel panel1 = new JPanel();
		panel1.setBackground(Color.WHITE);
		panel1.setLayout(new GridLayout(3,0));
		panel1.add(panel1a);
		panel1.add(panel1b);
		panel1.add(panel1c);
		
		/// Panel 2A
		JPanel panel2a = new JPanel();
		panel2a.setBackground(backClr);
		panel2a.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel2a.add(btnAnalysis);
		panel2a.add(btnViewData);
		panel2a.add(btnAbout);
		
		/// Panel 2B
		JPanel panel2b = new JPanel();
		panel2b.setBackground(backClr);
		panel2b.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel2b.add(comboMethod);
		panel2b.add(comboTable);
		panel2b.add(spinDigits);
		panel2b.add(chkCount);
		
		/// Panel 2
		JPanel panel2 = new JPanel();
		panel2.setBackground(backClr);
		panel2.setLayout(new GridLayout(2,0));
		panel2.add(panel2a);
		panel2.add(panel2b);
		
		/// Panel 3
		JPanel panel3 = new JPanel();
		panel3.setBackground(Color.WHITE);
		panel3.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
		panel3.add(comboSize);
		panel3.add(spinPoints);
		panel3.add(comboGrid);
		panel3.add(btnExport);
		
		/// Undo manager
		final UndoManager undoMan = new UndoManager();
		/// Actions for the manager - AbstractAction implements Action and ActionListener
		Action actUndo = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				if (undoMan.canUndo())
					undoMan.undo();
			}
		};
		Action actRedo = new AbstractAction() {
			public void actionPerformed(ActionEvent evt) {
				if (undoMan.canRedo())
					undoMan.redo();
			}
		};
		actUndo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		actRedo.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		
		/// Right mouse menu
		JMenuItem mCut = new JMenuItem(new DefaultEditorKit.CutAction());
		JMenuItem mCopy = new JMenuItem(new DefaultEditorKit.CopyAction());
		JMenuItem mPaste = new JMenuItem(new DefaultEditorKit.PasteAction());
		JMenuItem mUndo = new JMenuItem(actUndo);
		JMenuItem mRedo = new JMenuItem(actRedo);
		mCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		mCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));
		mPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK));
		mUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
		mRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
		mCut.setBackground(backClr);
		mCopy.setBackground(backClr);
		mPaste.setBackground(backClr);
		mUndo.setBackground(backClr);
		mRedo.setBackground(backClr);
		mCut.setText("Cut");
		mCopy.setText("Copy");
		mPaste.setText("Paste");
		mUndo.setText("Undo");
		mRedo.setText("Redo");
		
		JSeparator mSep = new JSeparator();
		mSep.setBackground(backClr);
		
		rightMouse = new JPopupMenu();
		rightMouse.add(mCut);
		rightMouse.add(mCopy);
		rightMouse.add(mPaste);
		rightMouse.add(mSep);
		rightMouse.add(mUndo);
		rightMouse.add(mRedo);
		
		/// Items for text areas
		textA = new JTextArea("# Open file or copy/paste data here" + ls +
				"# Column separators: tab semicolon comma" + ls +
				"# Recognized numbers: 1.7 and 1,7" + ls + ls +
				"#objectnames: O1, O2, O3, O4, O5" + ls +
				"#criternames: C1, C2, C3, C4" + ls + ls +
				"#input1" + ls +
				"6;5;3;7;6" + ls +
				"7;7;8;1;3" + ls +
				"4;3;5;9;1" + ls +
				"4;5;6;7;8" + ls + ls);
		textA.addMouseListener(new RightMouseListener());
		textA.setDragEnabled(true);
		textA.setFont(monoFont2);
		textA.getDocument().addUndoableEditListener(undoMan);
		textA.getInputMap().put((KeyStroke)actUndo.getValue(Action.ACCELERATOR_KEY), "ACTION_UNDO");
		textA.getInputMap().put((KeyStroke)actRedo.getValue(Action.ACCELERATOR_KEY), "ACTION_REDO");
		textA.getActionMap().put("ACTION_UNDO", actUndo);
		textA.getActionMap().put("ACTION_REDO", actRedo);
		
		textB = new JTextArea();
		textB.setFont(monoFont2);
		textB.setEditable(false);
		
		JScrollPane scrollTextA = new JScrollPane(textA);
		scrollTextA.setPreferredSize(new Dimension(450, 460));
		scrollTextA.setBorder(null);
		
		JScrollPane scrollTextB = new JScrollPane(textB);
		scrollTextB.setPreferredSize(new Dimension(450, 100));
		scrollTextB.setBorder(null);
		
		scrollA = new JScrollPane();
		scrollA.setPreferredSize(scrollSize);
		scrollA.getViewport().addChangeListener(new ScrollAListener());
		scrollA.setBorder(null);
		
		scrollB = new JScrollPane();
		scrollB.setPreferredSize(scrollSize);
		scrollB.getViewport().addChangeListener(new ScrollBListener());
		scrollB.setBorder(null);
		
		resScroll = new JScrollPane();
		resScroll.getViewport().setBackground(Color.WHITE);
		resScroll.setBorder(null);
		
		/// Panel text areas
		JPanel panelAreas = new JPanel();
		panelAreas.setLayout(new BorderLayout());
		panelAreas.add(panel1, BorderLayout.NORTH);
		panelAreas.add(scrollTextA, BorderLayout.CENTER);
		
		/// Split text areas
		JSplitPane splitAreas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, panelAreas, scrollTextB);
		splitAreas.setOneTouchExpandable(true);
		splitAreas.setResizeWeight(1.0);
		splitAreas.setDividerSize(10);
		
		/// Split tables
		JSplitPane splitTables = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, scrollA, scrollB);
		splitTables.setOneTouchExpandable(true);
		splitTables.setResizeWeight(0.5);
		splitTables.setDividerSize(10);
		
		/// Panel tables
		JPanel panelTables = new JPanel();
		panelTables.setLayout(new BorderLayout());
		panelTables.add(panel2, BorderLayout.NORTH);
		panelTables.add(splitTables, BorderLayout.CENTER);
		
		/// Panel graphic
		JPanel panelGraphic = new JPanel();
		panelGraphic.setLayout(new BorderLayout());
		panelGraphic.add(panel3, BorderLayout.NORTH);
		panelGraphic.add(resScroll, BorderLayout.CENTER);
		
		/// Split left-center
		JSplitPane splitLeft = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, splitAreas, panelTables);
		splitLeft.setOneTouchExpandable(true);
		splitLeft.setResizeWeight(0.0);
		splitLeft.setDividerSize(10);
		
		/// Split center-right
		JSplitPane splitRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, splitLeft, panelGraphic);
		splitRight.setOneTouchExpandable(true);
		splitRight.setResizeWeight(1.0);
		splitRight.setDividerSize(10);
		
		
		/// Main frame layout
		setLayout(new BorderLayout());
		getContentPane().add(splitRight, BorderLayout.CENTER);
		
		setTitle("ICrAData v1.0.0");
		setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				exitListener(evt);
			}
		});
		
		/// Glass pane
		glass = new MyGlass(this.getContentPane());
		setGlassPane(glass);
		
		//setMinimumSize(frameSize);
		//setPreferredSize(frameSize);
		pack(); /// Size of frame equal to the size of its components
		//setResizable(false);
		setLocationByPlatform(true);
		//setLocationRelativeTo(null); /// Center frame on screen
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		/// These must be here, do not move
		setVisible(true);
		
	}
	
	/// Show custom message and append to lower text area
	private void showMessage(String msgType, String msgText) {
		if (msgType.equals("info"))
			textB.append(msgText + ls);
		else if (msgType.equals("warn"))
			textB.append("[WARN] " + msgText + ls);
		else
			textB.append("[ERROR] " + msgText + ls);
		
		textB.setCaretPosition(textB.getText().length());
	}
	
	/// Prompt on window closing
	private void exitListener(WindowEvent evt) {
		if (JOptionPane.showConfirmDialog(
				this, "Exit the program?", "Exit",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE)
				== JOptionPane.YES_OPTION)
			System.exit(0);
	}
	
	/// Open file
	private void btnOpenListener(ActionEvent evt) {
		
		JFileChooser chOpen = new JFileChooser(ud);
		chOpen.setMultiSelectionEnabled(false);
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
		chOpen.addChoosableFileFilter(new FileNameExtensionFilter("Comma separated file (*.csv)", "csv"));
		
		if (chOpen.showOpenDialog(this) != JFileChooser.APPROVE_OPTION)
			return;
		
		File openFile = chOpen.getSelectedFile();
		
		try {
			String str = "";
			String line = "";
			
			BufferedReader br = new BufferedReader(new FileReader(openFile));
			while ((line = br.readLine()) != null) {
				str += line + ls;
				
				/// Load values for data type and matrix count
				if (line.startsWith("#datatype:") && line.length() > 10 && checkInt(line.substring(10).trim())) {
					int val = Integer.parseInt(line.substring(10).trim());
					if (val >= 0 && val <= 2)
						comboType.setSelectedIndex(val);
				}
				if (line.startsWith("#matcount:") && line.length() > 10 && checkInt(line.substring(10).trim())) {
					int val = Integer.parseInt(line.substring(10).trim());
					if (val >= 3 && val <= 10000)
						spinMatCount.setValue(val);
				}
				if (line.startsWith("#ordpair:") && line.length() > 9 && checkInt(line.substring(9).trim())) {
					if (Integer.parseInt(line.substring(9).trim()) == 1)
						chkOrdPair.setSelected(true);
					else
						chkOrdPair.setSelected(false);
				}
				if (line.startsWith("#transpose:") && line.length() > 11 && checkInt(line.substring(11).trim())) {
					if (Integer.parseInt(line.substring(11).trim()) == 1)
						chkTranspose.setSelected(true);
					else
						chkTranspose.setSelected(false);
				}
			}
			br.close();
			
			textA.setText(str);
			fileName = chOpen.getSelectedFile();
			showMessage("info", "Opened file " + openFile.getName() + " (" + openFile + ")");
			
		} catch (Exception ex) {
			showMessage("error", "Could not open file " + openFile.getName() + " (" + openFile + ")");
		}
	}
	
	/// Save file
	private void btnSaveListener(ActionEvent evt) {
		
		if (fileName != null) {
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
				bw.write(saveText()); // textA.getText().replace("\n", "\r\n")
				bw.close();
				
				showMessage("info", "Saved file " + fileName.getName() + " (" + fileName + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save file " + fileName.getName() + " (" + fileName + ")");
			}
			
		} else if (textA.getText().length() > 0) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
			
			if (chSave.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			
			File saveFile = chSave.getSelectedFile();
			if (!saveFile.toString().toLowerCase().endsWith(".txt"))
				saveFile = new File(chSave.getSelectedFile() + ".txt");
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
				bw.write(saveText()); // textA.getText().replace("\n", "\r\n")
				bw.close();
				
				fileName = saveFile;
				showMessage("info", "Saved file " + saveFile.getName() + " (" + saveFile + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save file " + saveFile.getName() + " (" + saveFile + ")");
			}
			
		} else
			showMessage("info", "No file opened and no text written in the input panel");
		
	}
	
	/// Save file as copy
	private void btnSaveCopyListener(ActionEvent evt) {
		
		if (textA.getText().length() > 0) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
			
			if (chSave.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
				return;
			
			File saveFile = chSave.getSelectedFile();
			if (!saveFile.toString().toLowerCase().endsWith(".txt"))
				saveFile = new File(chSave.getSelectedFile() + ".txt");
			
			try {
				BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
				bw.write(saveText()); // textA.getText().replace("\n", "\r\n")
				bw.close();
				
				showMessage("info", "Saved a copy as " + saveFile.getName() + " (" + saveFile + ")");
				
			} catch (Exception ex) {
				showMessage("error", "Could not save a copy as " + saveFile.getName() + " (" + saveFile + ")");
			}
		} else
			showMessage("info", "No text written in the input panel");
		
	}
	
	/// Save the text with date and matrix count
	private String saveText() {
		
		/// Get the text
		String[] line = textA.getText().split("\n");
		int vType = comboType.getSelectedIndex();
		int vCount = (int)spinMatCount.getValue();
		int vPair = (chkOrdPair.isSelected() ? 1 : 0);
		int vTranspose = (chkTranspose.isSelected() ? 1 : 0);
		
		/// Find what is in the text
		boolean markDate = false;
		boolean markType = false;
		boolean markCount = false;
		boolean markPair = false;
		boolean markTranspose = false;
		for (int i = 0; i < line.length; i++) {
			if (line[i].startsWith("#ICrAData")) {
				line[i] = "#ICrAData " + logDate.format(new Date());
				markDate = true;
			}
			if (line[i].startsWith("#datatype")) {
				line[i] = "#datatype: " + vType;
				markType = true;
			}
			if (line[i].startsWith("#matcount:") && vType > 0) {
				line[i] = "#matcount: " + vCount;
				markCount = true;
			}
			if (line[i].startsWith("#ordpair")) {
				line[i] = "#ordpair: " + vPair;
				markPair = true;
			}
			if (line[i].startsWith("#transpose")) {
				line[i] = "#transpose: " + vTranspose;
				markTranspose = true;
			}
		}
		
		/// Return as string and replace \n with \r\n
		String strData = (markDate ? "" : "#ICrAData " + logDate.format(new Date()) + "\r\n") +
				(markType ? "" : "#datatype: " + vType + "\r\n") +
				(!markCount && vType > 0  ? "#matcount: " + vCount + "\r\n" : "") +
				(markPair ? "" : "#ordpair: " + vPair + "\r\n") +
				(markTranspose ? "" : "#transpose: " + vTranspose + "\r\n");
		for (int i = 0; i < line.length; i++)
			strData += line[i] + "\r\n";
		
		return strData;
	}
	
	/// View matrix data
	private void btnViewDataListener(ActionEvent evt) {
		
		try {
			if (result != null) {
				
				/// Make a local copy of the hash map
				final HashMap<String, double[][]> mmap = new HashMap<String, double[][]>();
				Iterator<String> it = result.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					mmap.put(key, result.get(key));
				}
				
				/// And the headers
				final String[][] headers = new String[][] {
					new String[objNames.length],
					new String[critNames.length],
					new String[objNames2.length],
					new String[critNames2.length]
				};
				for (int i = 0; i < objNames.length; i++)
					headers[0][i] = objNames[i];
				for (int i = 0; i < critNames.length; i++)
					headers[1][i] = critNames[i];
				for (int i = 0; i < objNames2.length; i++)
					headers[2][i] = objNames2[i];
				for (int i = 0; i < critNames2.length; i++)
					headers[3][i] = critNames2[i];
				
				/// List with all keys from the hash map
				int countKeys = 0;
				String[] listKeys = new String[mmap.size()];
				it = mmap.keySet().iterator();
				while (it.hasNext())
					listKeys[countKeys++] = it.next();
				Arrays.sort(listKeys);
				
				/// Separator \t ; , &
				final String[] separator = new String[] {"\t", ";", ",", " & "};
				/// Locale
				final Locale[] locale = new Locale[] {new Locale("bg", "BG"), Locale.US};
				
				Dimension dim70 = new Dimension(70, 25);
				Dimension dim125 = new Dimension(125, 25);
				Dimension dim150 = new Dimension(170, 25);
				final JFrame viewFrame = new JFrame();
				
				/// List with keys
				final DefaultListModel<String> listModel = new DefaultListModel<String>();
				for (int i = 0; i < countKeys; i++)
					listModel.addElement(listKeys[i]);
				final JList<String> listData = new JList<String>(listModel);
				listData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				listData.setFont(monoFont);
				JScrollPane scrollList = new JScrollPane(listData);
				scrollList.setPreferredSize(new Dimension(175, 400));
				/// Text area for matrix
				final JTextArea textMatrix = new JTextArea();
				textMatrix.addMouseListener(new RightMouseListener());
				textMatrix.setDragEnabled(true);
				textMatrix.setFont(monoFont2);
				JScrollPane scrollText = new JScrollPane(textMatrix);
				scrollText.setPreferredSize(new Dimension(800, 500));
				
				/// Label for data
				JLabel lblData = new JLabel("Select matrix:");
				lblData.setPreferredSize(dim150);
				lblData.setFont(monoFont);
				/// Combo separator
				final JComboBox<String> comboSeparator = new JComboBox<String>(
						new String[] {"Tab \\t", "Semicolon ;", "Comma ,", "TeX &"});
				comboSeparator.setToolTipText("Select the column separator.");
				comboSeparator.setPreferredSize(dim125);
				comboSeparator.setFont(monoFont);
				/// Combo Locale
				final JComboBox<String> comboLocale = new JComboBox<String>(
						new String[] {"Comma", "Point"});
				comboLocale.setToolTipText("Select the decimal separator.");
				comboLocale.setPreferredSize(dim125);
				comboLocale.setFont(monoFont);
				/// Digits spinner
				final JSpinner spinDigits = new JSpinner(new SpinnerNumberModel(4,1,20,1));
				spinDigits.setToolTipText("Select the number of digits after the decimal separator.");
				spinDigits.setPreferredSize(dim70);
				spinDigits.setFont(monoFont);
				/// Check box for header
				final JCheckBox chkHeader = new JCheckBox("Show Header");
				chkHeader.setToolTipText("Show the table header.");
				chkHeader.setBackground(backClr);
				chkHeader.setFont(monoFont);
				chkHeader.setSelected(true);
				
				/// Listeners
				listData.addMouseListener(new MouseListener() {
					public void mouseClicked(MouseEvent evt) {
						if (listData.getSelectedIndex() >= 0)
							textMatrix.setText(showArray(mmap.get(listModel.get(listData.getSelectedIndex())),
								separator[comboSeparator.getSelectedIndex()],
								locale[comboLocale.getSelectedIndex()],
								(int)spinDigits.getValue(),
								getHeader(listModel.get(listData.getSelectedIndex()), headers),
								chkHeader.isSelected()));
					}
					public void mousePressed(MouseEvent evt) {}
					public void mouseReleased(MouseEvent evt) {}
					public void mouseEntered(MouseEvent evt) {}
					public void mouseExited(MouseEvent evt) {}
				});
				
				comboSeparator.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED) {
							if (listData.getSelectedIndex() >= 0)
								textMatrix.setText(showArray(mmap.get(listModel.get(listData.getSelectedIndex())),
									separator[comboSeparator.getSelectedIndex()],
									locale[comboLocale.getSelectedIndex()],
									(int)spinDigits.getValue(),
									getHeader(listModel.get(listData.getSelectedIndex()), headers),
									chkHeader.isSelected()));
						}
					}
				});
				
				comboLocale.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent evt) {
						if (evt.getStateChange() == ItemEvent.SELECTED) {
							if (listData.getSelectedIndex() >= 0)
								textMatrix.setText(showArray(mmap.get(listModel.get(listData.getSelectedIndex())),
									separator[comboSeparator.getSelectedIndex()],
									locale[comboLocale.getSelectedIndex()],
									(int)spinDigits.getValue(),
									getHeader(listModel.get(listData.getSelectedIndex()), headers),
									chkHeader.isSelected()));
						}
					}
				});
				
				spinDigits.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent evt) {
						if (listData.getSelectedIndex() >= 0)
							textMatrix.setText(showArray(mmap.get(listModel.get(listData.getSelectedIndex())),
								separator[comboSeparator.getSelectedIndex()],
								locale[comboLocale.getSelectedIndex()],
								(int)spinDigits.getValue(),
								getHeader(listModel.get(listData.getSelectedIndex()), headers),
								chkHeader.isSelected()));
					}
				});
				
				chkHeader.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						if (listData.getSelectedIndex() >= 0)
							textMatrix.setText(showArray(mmap.get(listModel.get(listData.getSelectedIndex())),
								separator[comboSeparator.getSelectedIndex()],
								locale[comboLocale.getSelectedIndex()],
								(int)spinDigits.getValue(),
								getHeader(listModel.get(listData.getSelectedIndex()), headers),
								chkHeader.isSelected()));
					}
				});
				
				JButton btnClose = new JButton("Close");
				btnClose.setPreferredSize(dim125);
				btnClose.setToolTipText("Close the window.");
				btnClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						viewFrame.setVisible(false);
					}
				});
				
				/// Panel for items
				JPanel panelBtn = new JPanel();
				panelBtn.setBackground(backClr);
				panelBtn.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));
				panelBtn.add(lblData);
				panelBtn.add(comboSeparator);
				panelBtn.add(comboLocale);
				panelBtn.add(spinDigits);
				panelBtn.add(chkHeader);
				panelBtn.add(btnClose);
				
				/// Frame options
				viewFrame.setTitle(typeNames[comboType.getSelectedIndex()] + " - " +
						(comboType.getSelectedIndex() == 2 ? aggrNames[comboAggr.getSelectedIndex()] + " - " : "") +
						methodNames[comboMethod.getSelectedIndex()]);
				viewFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
				
				viewFrame.setLayout(new BorderLayout());
				viewFrame.add(panelBtn, BorderLayout.NORTH);
				viewFrame.add(scrollList, BorderLayout.WEST);
				viewFrame.add(scrollText, BorderLayout.CENTER);
				
				viewFrame.pack();
				viewFrame.setLocationByPlatform(true);
				//viewFrame.setLocationRelativeTo(null); /// Center frame on screen
				viewFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
				
				viewFrame.setVisible(true);
			}
			
		} catch (Exception ex) {
			showMessage("error", "Could not view the internal data");
		}
	}
	
	/// Get matrix header
	private String[] getHeader(String key, String[][] headers) {
		if (key.equals("Input1") || key.equals("Input2"))
			return headers[0];
		else if (key.equals("MatrixMU") || key.equals("MatrixNU") || key.equals("MatrixDist"))
			return headers[1];
		else if (key.contains("Input"))
			return headers[2];
		else if (key.contains("MatrixMU") || key.contains("MatrixNU") || key.contains("MatrixDist"))
			return headers[3];
		else if (key.contains("PlotPoints"))
			return new String[] {MU, NU, "Row", "Column", "Distance"};
		else
			return null;
	}
	
	/// About the program
	private void btnAboutListener(ActionEvent evt) {
		
		JFrame aboutFrame = new JFrame();
		JTextArea text = new JTextArea();
		text.addMouseListener(new RightMouseListener());
		text.setDragEnabled(true);
		text.setFont(monoFont2);
		text.setText("InterCriteria Analysis proposed for the first time by this article:" + ls +
				"  Atanassov K., D. Mavrov, V. Atanassova," + ls +
				"  Intercriteria Decision Making: A New Approach for Multicriteria Decision Making," + ls +
				"    Based on Index Matrices and Intuitionistic Fuzzy Sets," + ls +
				"  Issues in Intuitionistic Fuzzy Sets and Generalized Nets, Vol. 11, 2014, 1-8." + ls + ls +
				"Main paper for ICrAData software:" + ls +
				"  Ikonomov N., P. Vassilev, O. Roeva," + ls +
				"  ICrAData � Software for InterCriteria Analysis," + ls +
				"  International Journal Bioautomation, Vol. 22(1), 2018." + ls + ls + ls +
				"InterCriteria Analysis Data" + ls +
				"  Version: 1.0.0" + ls +
				"  Date: January 25, 2018" + ls +
				"  Compiled by: Eclipse Indigo SR2" + ls + ls);
		
		JScrollPane scrollText = new JScrollPane(text);
		scrollText.setPreferredSize(new Dimension(900, 500));
		
		/// Frame options
		aboutFrame.setTitle("About");
		aboutFrame.setIconImage(Toolkit.getDefaultToolkit().getImage("docs/x-icon.jpg"));
		aboutFrame.add(scrollText);
		
		aboutFrame.pack();
		aboutFrame.setLocationByPlatform(true);
		//aboutFrame.setLocationRelativeTo(null); /// Center frame on screen
		aboutFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		aboutFrame.setVisible(true);
	}
	
	/// Export the graphic to PNG or TeX file
	private void btnExportListener(ActionEvent evt) {
		
		if (result != null) {
			JFileChooser chSave = new JFileChooser(ud);
			chSave.setMultiSelectionEnabled(false);
			chSave.setAcceptAllFileFilterUsed(false);
			chSave.setFileFilter(new FileNameExtensionFilter("PNG image (*.png)", "png"));
			chSave.setFileFilter(new FileNameExtensionFilter("TeX file (*.tex)", "tex"));
			
			if (chSave.showSaveDialog(null) != JFileChooser.APPROVE_OPTION)
				return;
			
			File chName = chSave.getSelectedFile();
			FileNameExtensionFilter chExtFilter = (FileNameExtensionFilter)chSave.getFileFilter();
			
			if (chExtFilter.getExtensions()[0].equals("png")) {
				if (!chName.toString().toLowerCase().endsWith(".png"))
					chName = new File(chName.toString() + ".png");
				
				try {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					
					BufferedImage img = new BufferedImage(resPanel.getWidth(), resPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
					Graphics g = img.getGraphics();
					resPanel.printAll(g);
					
					//Graphics2D g2 = (Graphics2D)img.getGraphics();
					//resPanel.printAll(g2);
					//resPanel.paintAll(g2);
					
					//FileOutputStream fos = new FileOutputStream(ud + "test.jpg");
					//JPEGImageEncoderImpl jpeg = new JPEGImageEncoderImpl(fos);
					
					ImageIO.write(img, "png", chName.getAbsoluteFile());
					showMessage("info", "Saved PNG image " + chName.getName() + " (" + chName + ")");
					
				} catch (Exception ex) {
					showMessage("error", "Could not save PNG image " + chName.getName());
				}
				
			} else if (chExtFilter.getExtensions()[0].equals("tex")) {
				if (!chName.toString().toLowerCase().endsWith(".tex"))
					chName = new File(chName.toString() + ".tex");
				
				try {
					BufferedWriter bw = new BufferedWriter(new FileWriter(chName));
					bw.write(exportTeX(result.get("PlotPoints")).replace("\n", "\r\n"));
					bw.close();
					
					showMessage("info", "Saved TeX file " + chName.getName() + " (" + chName + ")");
					
				} catch (Exception ex) {
					showMessage("error", "Could not save TeX file " + chName.getName() + " (" + chName + ")");
				}
			}
		}
	}
	
	/// Export to TeX
	private String exportTeX(double[][] arrPoints) {
		
		/// Triangle 0  Ticks 1  Grid 2  Text 3
		int vGrid = comboGrid.getSelectedIndex();
		StringBuilder res = new StringBuilder();
		
		/// picture \makebox(0,0)[cc]{$Z_1$}   c r l b t
		/// http://www.emerson.emory.edu/services/latex/latex_51.html
		/// https://tex.stackexchange.com/questions/32791/picture-environment-rotating-text
		res.append("%%% ICrAData TeX Export " + logDate.format(new Date()) + ls +
			"\\documentclass[11pt]{article}" + ls +
			(vGrid == 3 ? "\\usepackage{graphicx}" + ls : "") + 
			"\\begin{document}" + ls +
			"\\thispagestyle{empty}" + ls + ls +
			"%%% Change unitlength and font size to scale the graphic" + ls +
			"%%% Font sizes: \\tiny \\scriptsize \\footnotesize \\small \\normalsize \\large \\Large \\LARGE \\huge \\Huge" + ls +
			"\\begin{center}" + ls +
			(vGrid == 3 ? "\\newcommand{\\myticks}{\\scriptsize}" + ls +
				"\\newcommand{\\mytext}{\\normalsize}" + ls : "") +
			"\\setlength{\\unitlength}{20pt} %10pt=4mm" + ls +
			"\\linethickness{0.5pt}" + ls +
			"\\begin{picture}" +
			(vGrid == 3 ? "(11.5,11.5)(-1.5,-1.5)" + ls : "(10,10)" + ls) +
			"\\put(0,0){\\line(0,1){10}}" + ls +
			"\\put(0,0){\\line(1,0){10}}" + ls +
			"\\put(10,0){\\line(-1,1){10}}" + ls);
		
		/// Ticks
		if (vGrid == 1) {
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){0.3}}" + ls +
					"\\put(-0.15," + i + "){\\line(1,0){0.3}}" + ls);
		}
		
		/// Grid
		if (vGrid == 2) {
			res.append("\\put(10,0){\\line(0,1){10}}" + ls +
				"\\put(0,10){\\line(1,0){10}}" + ls);
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){10.3}}" + ls +
					"\\put(-0.15," + i + "){\\line(1,0){10.3}}" + ls);
		}
		
		/// Text
		if (vGrid == 3) {
			res.append("\\put(5,-1.2){\\makebox(0,0)[cc]{\\mytext Degree of agreement, $\\mu$}}" + ls +
				"\\put(-1.3,5){\\makebox(0,0)[cc]{\\rotatebox{90}{\\mytext Degree of disagreement, $\\nu$}}}" + ls +
				"\\put(0,-0.4){\\makebox(0,0)[cc]{\\myticks $0$}}" + ls +
				"\\put(10,-0.4){\\makebox(0,0)[cc]{\\myticks $1$}}" + ls +
				"\\put(-0.33,0){\\makebox(0,0)[cc]{\\myticks $0$}}" + ls +
				"\\put(-0.33,10){\\makebox(0,0)[cc]{\\myticks $1$}}" + ls);
			for (int i = 1; i < 10; i++)
				res.append("\\put(" + i + ",-0.15){\\line(0,1){0.3}}" + ls +
					"\\put(" + i + ",-0.4){\\makebox(0,0)[cc]{\\myticks $0." + i + "$}}" + ls +
					"\\put(-0.15," + i + "){\\line(1,0){0.3}}" + ls +
					"\\put(-0.5," + i + "){\\makebox(0,0)[cc]{\\myticks $0." + i + "$}}" + ls);
		}
		
		/// Points
		for (int i = 0; i < arrPoints.length; i++)
			res.append("\\put(" + arrPoints[i][0]*10 + "," + arrPoints[i][1]*10 + "){\\circle*{0.23}}" + ls);
		
		res.append("\\end{picture}" + ls +
			"\\end{center}" + ls + ls +
			"\\end{document}" + ls);
		
		return res.toString();
	}
	
	/// Selector for ICrA type
	private class ComboTypeListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (comboType.getSelectedIndex() == 0) {
					comboAggr.setVisible(false);
					spinMatCount.setVisible(false);
				} else if (comboType.getSelectedIndex() == 1) {
					comboAggr.setVisible(false);
					spinMatCount.setVisible(true);
				} else {
					comboAggr.setVisible(true);
					spinMatCount.setVisible(true);
				}
			}
		}
	}
	
	/// Selector for plot size
	private class ComboSizeListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (comboSize.getSelectedIndex() >= 0)
					valSize = Integer.parseInt(sizeDim[comboSize.getSelectedIndex()]);
				else if (checkInt((String)comboSize.getSelectedItem()))
					valSize = Integer.parseInt((String)comboSize.getSelectedItem());
				if (valSize < 100 || valSize > 100000)
					valSize = 400;
				
				if (result != null) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setPreferredSize(new Dimension(valSize, valSize));
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
	}
	
	/// Spinner for point size
	private class SpinPointsListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			JSpinner spinner = (JSpinner)evt.getSource();
			pointSize = (int)spinner.getValue();
			if (result != null) {
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				resPanel.setMyPoint((int)spinner.getValue());
				resPanel.repaint();
				resPanel.revalidate();
			}
		}
	}
	
	/// Selector for plot type
	private class ComboGridListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (result != null) {
					MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
					resPanel.setMyGrid(comboGrid.getSelectedIndex());
					resPanel.repaint();
					resPanel.revalidate();
				}
			}
		}
	}
	
	/// Primary or secondary view
	private class ComboTableListener implements ItemListener {
		public void itemStateChanged(ItemEvent evt) {
			if (evt.getStateChange() == ItemEvent.SELECTED) {
				if (result != null) {
					if (comboTable.getSelectedIndex() == 0) {
						/// Create the tables for primary view
						MyTable tableA = new MyTable(result.get("MatrixMU"), critNames, MU, true);
						MyTable tableB = new MyTable(result.get("MatrixNU"), critNames, NU, true);
						scrollA.setViewportView(tableA);
						scrollB.setViewportView(tableB);
					} else {
						/// Create the tables for secondary view
						MyTable tableA = new MyTable(result.get("MatrixMU"), result.get("MatrixNU"), critNames, "(" + MU + "," + NU + ")");
						MyTable tableB = new MyTable(result.get("MatrixDist"), critNames, "Distance", false);
						scrollA.setViewportView(tableA);
						scrollB.setViewportView(tableB);
					}
				}
			}
		}
	}
	
	/// Spinner for number of digits
	private class SpinDigitsListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			JSpinner spinner = (JSpinner)evt.getSource();
			
			/// Make 0.0000, must be with dot, not with comma
			String strDig = "0.";
			for (int i = 0; i < (int)spinner.getValue(); i++)
				strDig += "0";
			
			/// Number format
			numFormat = new DecimalFormat(strDig, new DecimalFormatSymbols(Locale.US));
			
			if (result != null) {
				if (comboTable.getSelectedIndex() == 0) {
					/// Create the tables for primary view
					MyTable tableA = new MyTable(result.get("MatrixMU"), critNames, MU, true);
					MyTable tableB = new MyTable(result.get("MatrixNU"), critNames, NU, true);
					scrollA.setViewportView(tableA);
					scrollB.setViewportView(tableB);
				} else {
					/// Create the tables for secondary view
					MyTable tableA = new MyTable(result.get("MatrixMU"), result.get("MatrixNU"), critNames, "(" + MU + "," + NU + ")");
					MyTable tableB = new MyTable(result.get("MatrixDist"), critNames, "Distance", false);
					scrollA.setViewportView(tableA);
					scrollB.setViewportView(tableB);
				}
				
				/// Refresh the graphic
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				resPanel.repaint();
				resPanel.revalidate();
			}
		}
	}
	
	/// Synchronize scrolling for A
	private class ScrollAListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			int valH = scrollA.getHorizontalScrollBar().getValue();
			int valV = scrollA.getVerticalScrollBar().getValue();
			//System.out.println(valH + " " + valV);
			scrollB.getHorizontalScrollBar().setValue(valH);
			scrollB.getVerticalScrollBar().setValue(valV);
		}
	}
	
	/// Synchronize scrolling for B
	private class ScrollBListener implements ChangeListener {
		public void stateChanged(ChangeEvent evt) {
			int valH = scrollB.getHorizontalScrollBar().getValue();
			int valV = scrollB.getVerticalScrollBar().getValue();
			//System.out.println(valH + " " + valV);
			scrollA.getHorizontalScrollBar().setValue(valH);
			scrollA.getVerticalScrollBar().setValue(valV);
		}
	}
	
	/// Listener for right mouse button on text components
	private class RightMouseListener implements MouseListener {
		public void mouseClicked(MouseEvent evt) {
			if (SwingUtilities.isRightMouseButton(evt))
				rightMouse.show(evt.getComponent(), evt.getX(), evt.getY());
		}
		
		public void mousePressed(MouseEvent evt) {}
		public void mouseReleased(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
	}
	
	/// Panel for plotting - implements MouseListener, MouseMotionListener, ComponentListener
	private class MyPanel extends JPanel implements Scrollable, MouseListener, MouseMotionListener {
		
		private double[][] points;
		private Rectangle[] arrRect;
		private int plotPoint = 0;
		private int plotGrid = 0;
		private int plotMarkRow = -1;
		private int plotMarkCol = -1;
		
		private MyPanel(double[][] arrPoints) {
			points = arrPoints;
			setBackground(Color.WHITE);
			addMouseListener(this);
			addMouseMotionListener(this);
			ToolTipManager.sharedInstance().registerComponent(this);
		}
		
		private void setMyPoint(int val) {
			plotPoint = val;
		}
		
		private void setMyGrid(int val) {
			plotGrid = val;
		}
		
		private void setMyMarker(int row, int col) {
			plotMarkRow = row;
			plotMarkCol = col;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setFont(sansFont);
			
			int width = getWidth();
			int height = getHeight();
			int delta = 20;
			if (plotGrid == 3)
				delta = 60;
			
			/// Grid
			g2.draw(new Line2D.Double(delta, delta, delta, height-delta)); // left
			g2.draw(new Line2D.Double(delta, height-delta, width-delta, height-delta)); // bottom
			g2.draw(new Line2D.Double(delta, delta, width-delta, height-delta)); // main diagonal
			
			if (plotGrid == 2) {
				g2.draw(new Line2D.Double(width-delta, delta, width-delta, height-delta)); // right
				g2.draw(new Line2D.Double(delta, delta, width-delta, delta)); // top
				//g2.draw(new Line2D.Double(width-delta, delta, delta, height-delta)); // secondary diagonal
			}
			
			/// Scale so that there are boundaries on the graph
			int widthScale = width-2*delta;
			int heightScale = height-2*delta;
			int line = 5;
			
			/// Write marks
			double[] marks = new double[] {0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
			double[] marks2 = new double[] {0.9, 0.8, 0.7, 0.6, 0.5, 0.4, 0.3, 0.2, 0.1};
			if (plotGrid == 1) {
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+line, delta+heightScale*marks[i]));
				
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], height-delta-line, delta+widthScale*marks[i], height-delta+line));
				
			} else if (plotGrid == 2) {
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], width-delta+line, delta+heightScale*marks[i]));
				
				for (int i = 0; i < marks.length; i++)
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], delta-line, delta+widthScale*marks[i], height-delta+line));
				
			} else if (plotGrid == 3) {
				for (int i = 0; i < marks.length; i++) {
					g2.draw(new Line2D.Double(delta-line, delta+heightScale*marks[i], delta+line, delta+heightScale*marks[i]));
					int widthMark = g2.getFontMetrics(sansFont).stringWidth(String.valueOf(marks2[i]));
					g2.drawString(String.valueOf(marks2[i]), (int)(delta-4*line-widthMark/2), (int)(delta+heightScale*marks[i]+1.35*line));
				}
				
				for (int i = 0; i < marks.length; i++) {
					g2.draw(new Line2D.Double(delta+widthScale*marks[i], height-delta-line, delta+widthScale*marks[i], height-delta+line));
					int widthMark = g2.getFontMetrics(sansFont).stringWidth(String.valueOf(marks2[i]));
					g2.drawString(String.valueOf(marks[i]), (int)(delta+widthScale*marks[i]-widthMark/2), (int)(height-delta+4.5*line));
				}
				
				g2.drawString("0", (int)(delta-3.3*line), (int)(delta+heightScale+1.35*line));
				g2.drawString("1", (int)(delta-3.3*line), (int)(delta+1.35*line));
				
				g2.drawString("0", (int)(delta-0.7*line), (int)(delta+heightScale+4.5*line));
				g2.drawString("1", (int)(delta+widthScale-line), (int)(delta+heightScale+4.5*line));
				
				g2.setFont(sansFont2);
				int widthMU = g2.getFontMetrics(sansFont2).stringWidth("Degree of agreement, " + MU);
				int widthNU = g2.getFontMetrics(sansFont2).stringWidth("Degree of disagreement, " + NU);
				g2.drawString("Degree of agreement, " + MU, (int)(delta+widthScale*marks[4]-widthMU/2), (int)(height-delta+10*line));
				
				AffineTransform g2Orig = g2.getTransform();
				g2.rotate(Math.toRadians(-90));
				g2.drawString("Degree of disagreement, " + NU, (int)(-(delta+heightScale*marks[4]+widthNU/2)), (int)(delta-9*line));
				g2.setTransform(g2Orig);
			}
			
			/// Colors
			//g2.setPaint(Color.RED);
			g2.setColor(Color.BLACK);
			setBackground(Color.WHITE);
			
			/// Save rectangles for glass
			arrRect = new Rectangle[points.length];
			int radius = plotPoint;
			
			/// Plot the points and save them to array for glass
			for (int i = 0; i < points.length; i++) {
				double x = points[i][0]*widthScale+delta;
				double y = heightScale-points[i][1]*heightScale+delta;
				arrRect[i] = new Rectangle((int)x-radius, (int)y-radius, 2*radius, 2*radius);
				g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
			}
			
			/// Mark the selected point in the table
			for (int i = 0; i < points.length; i++) {
				if (plotMarkRow > -1 && plotMarkRow > -1 &&
						((points[i][2] == plotMarkRow && points[i][3] == plotMarkCol) ||
						(points[i][3] == plotMarkRow && points[i][2] == plotMarkCol))) {
					double x = points[i][0]*widthScale+delta;
					double y = heightScale-points[i][1]*heightScale+delta;
					g2.setColor(Color.RED);
					g2.fill(new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius));
				}
			}
			
		}
		
		/// Set tool-tip text
		public String getToolTipText(MouseEvent evt) {
			
			for (int i = 0; i < arrRect.length; i++) {
				if (arrRect[i].contains(evt.getX(), evt.getY())) {
					return "<html>Row: " + critNames[(int)Math.round(points[i][2])] +
							"<br/>Column: " + critNames[(int)Math.round(points[i][3])] + 
							"<br/>" + MU + ": " + numFormat.format(points[i][0]) +
							"<br/>" + NU + ": " + numFormat.format(points[i][1]) +
							"<br/>Distance: " + numFormat.format(points[i][4]) +
							"</html>";
				}
			}
			
			return null;
		}
		
		/// Show the glass when the mouse button is pressed and held
		public void mousePressed(MouseEvent evt) {
			
			/// Set point for dragging
			pp.setLocation(evt.getPoint());
			
			/// Change cursor to crosshair
			//((JPanel)evt.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			//this.getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			glass.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			
			/// Clear all points from memory
			glassA.clear();
			glassB.clear();
			
			for (int i = 0; i < arrRect.length; i++) {
				if (arrRect[i].contains(evt.getX(), evt.getY())) {
					MyTable tableA = (MyTable)scrollA.getViewport().getView();
					MyTable tableB = (MyTable)scrollB.getViewport().getView();
					
					/// Rows and columns have index from 0, 1, 2, 3, etc
					/// Rows: the column table header is not counted towards the rows, therefore it is correct
					/// Columns: We have row table header, therefore the column is +1
					/// Wrong cell: (int)Math.round(points[i][2]), (int)Math.round(points[i][3])
					/// Correct cell: (int)Math.round(points[i][2]), (int)Math.round(points[i][3] + 1)
					Rectangle cellA = tableA.getCellRect((int)Math.round(points[i][2]), (int)Math.round(points[i][3] + 1), false);
					Rectangle cellB = tableB.getCellRect((int)Math.round(points[i][2]), (int)Math.round(points[i][3] + 1), false);
					
					/// View the cell
					scrollA.getHorizontalScrollBar().setValue((int)cellA.getX());
					scrollA.getVerticalScrollBar().setValue((int)cellA.getY());
					scrollB.getHorizontalScrollBar().setValue((int)cellB.getX());
					scrollB.getVerticalScrollBar().setValue((int)cellB.getY());
					
					/// Add the points to the glass
					glassA.add(new double[] {cellA.getX(), cellA.getY(), cellA.getWidth(), cellA.getHeight()});
					glassB.add(new double[] {cellB.getX(), cellB.getY(), cellB.getWidth(), cellB.getHeight()});
					
					/*System.out.println("Cell: " + cellA.getX() + " " + cellA.getY() + " " + cellA.getWidth() + " " + cellA.getHeight());
					System.out.println("Coordinates: " + points[i][0] + " " + points[i][1] +
							"   Row: " + Math.round(points[i][2]) + " Column: " + Math.round(points[i][3]) +
							"   Point: " + evt.getX() + " " + evt.getY());
					*/
				}
			}
			
			/// Show the rectangles on the glass
			if (glassA.size() > 0) {
				glass.setVisible(true);
				glass.repaint();
			} else
				glass.setVisible(false);
		}
		
		/// Hide the glass when the mouse button is released
		public void mouseReleased(MouseEvent evt) {
			//((JPanel)evt.getSource()).setCursor(Cursor.getDefaultCursor());
			//this.getRootPane().setCursor(Cursor.getDefaultCursor());
			glass.setCursor(Cursor.getDefaultCursor());
			glass.setVisible(false);
		}
		
		public void mouseClicked(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		public void mouseMoved(MouseEvent evt) {}
		
		/// Move the picture when dragging the mouse
		public void mouseDragged(MouseEvent evt) {
			JPanel thePanel = (JPanel) evt.getSource();
			JViewport theView = (JViewport) thePanel.getParent();
			Point cp = evt.getPoint();
			Point vp = theView.getViewPosition();
			//System.out.println(cp.getX() + " " + cp.getY() + "    " + vp.getX() + " " + vp.getY());
			vp.translate(pp.x-cp.x, pp.y-cp.y);
			thePanel.scrollRectToVisible(new Rectangle(vp, theView.getSize()));
			pp.setLocation(cp);
		}
		
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}
		
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 50;
		}
		
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 50;
		}
		
		public boolean getScrollableTracksViewportHeight() {
			return false; /// default is true, need false
		}
		
		public boolean getScrollableTracksViewportWidth() {
			return false; /// default is true, need false
		}
		
	}
	
	/// Glass pane for drawing
	private class MyGlass extends JComponent {
		
		private Container cont;
		
		private MyGlass(Container contentPane) {
			cont = contentPane;
		}
		
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			for (int i = 0; i < glassA.size(); i++) {
				double[] pointA = glassA.get(i);
				double[] pointB = glassB.get(i);
				Point locF = cont.getParent().getLocationOnScreen();
				
				Point locSA = scrollA.getLocationOnScreen();
				Point locSB = scrollB.getLocationOnScreen();
				Rectangle visSA = scrollA.getViewportBorderBounds();
				Rectangle visSB = scrollB.getViewportBorderBounds();
				
				MyTable tableA = (MyTable)scrollA.getViewport().getView();
				MyTable tableB = (MyTable)scrollB.getViewport().getView();
				
				Point locA = tableA.getLocationOnScreen();
				Point locB = tableB.getLocationOnScreen();
				//Rectangle visibleA = tableA.getVisibleRect();
				//Rectangle visibleB = tableB.getVisibleRect();
				
				int headerA = tableA.getTableHeader().getHeight();
				int headerB = tableB.getTableHeader().getHeight();
				int eps = 2;
				//System.out.println(locA.getX() + " " + locA.getY() + "     " + locF.getX() + " " + locF.getY());
				
				/// Draw cells
				g2.setStroke(new BasicStroke(2));
				g2.setColor(Color.BLUE);
				
				if (locA.getX() - locF.getX() + pointA[0] >= locSA.getX() - locF.getX() &&
					locA.getY() - locF.getY() + pointA[1] >= locSA.getY() - locF.getY() + headerA &&
					locA.getX() - locF.getX() + pointA[0] + pointA[2] <= locSA.getX() - locF.getX() + visSA.width + eps &&
					locA.getY() - locF.getY() + pointA[1] + pointA[3] <= locSA.getY() - locF.getY() + visSA.height + headerA + eps)
					g2.draw(new Rectangle2D.Double(
						locA.getX() - locF.getX() + pointA[0], locA.getY() - locF.getY() + pointA[1], pointA[2], pointA[3]));
				
				if (locB.getX() - locF.getX() + pointB[0] >= locSB.getX() - locF.getX() &&
					locB.getY() - locF.getY() + pointB[1] >= locSB.getY() - locF.getY() + headerB &&
					locB.getX() - locF.getX() + pointB[0] + pointB[2] <= locSB.getX() - locF.getX() + visSB.width + eps &&
					locB.getY() - locF.getY() + pointB[1] + pointB[3] <= locSB.getY() - locF.getY() + visSB.height + headerB + eps)
					g2.draw(new Rectangle2D.Double(
						locB.getX() - locF.getX() + pointB[0], locB.getY() - locF.getY() + pointB[1], pointB[2], pointB[3]));
				
				/// Draw table
				//g2.setColor(Color.RED);
				//g2.draw(new Rectangle2D.Double(locA.getX()-locF.getX(), locA.getY()-locF.getY(), visibleA.width, visibleA.height));
				//g2.draw(new Rectangle2D.Double(locB.getX()-locF.getX(), locB.getY()-locF.getY(), visibleB.width, visibleB.height));
				
				/// Draw scrollpane
				//g2.setColor(Color.GREEN);
				//g2.draw(new Rectangle2D.Double(locSA.getX()-locF.getX(), locSA.getY()-locF.getY()+headerA, visSA.width, visSA.height));
				//g2.draw(new Rectangle2D.Double(locSB.getX()-locF.getX(), locSB.getY()-locF.getY()+headerB, visSB.width, visSB.height));
				
			}
			
		}
	}
	
	/// Table for data view
	private class MyTable extends JTable implements MouseListener {
		
		private double[][] data;
		private String[][] data2;
		private String[] rows;
		private String[] cols;
		private int tableType = 0;
		private boolean hasColors = true;
		
		private MyTable(double[][] arrData, String[] headers, String name, boolean colors) {
			if (arrData != null) {
				data = arrData;
				rows = headers;
				cols = new String[headers.length + 1];
				cols[0] = name;
				for (int i = 0; i < headers.length; i++)
					cols[i+1] = headers[i];
				
				tableType = 0;
				hasColors = colors;
				addMouseListener(this);
				
				setModel(new MyTableModel());
				setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				setAutoCreateRowSorter(true);
				
				getTableHeader().setFont(monoFont);
				//getTableHeader().setResizingAllowed(false);
				getTableHeader().setReorderingAllowed(false);
				
				setDefaultRenderer(Double.class, new MyCellRenderer());
				//setDefaultRenderer(String.class, new ExCellRenderer());
			}
		}
		
		private MyTable(double[][] arrA, double[][] arrB, String[] headers, String name) {
			if (arrA != null && arrB != null) {
				data2 = new String[arrA.length][arrA[0].length];
				for (int i = 0; i < arrA.length; i++)
					for (int j = 0; j < arrA[0].length; j++)
						data2[i][j] = "(" + numFormat.format(arrA[i][j]) + "," + numFormat.format(arrB[i][j]) + ")";
				
				tableType = 1;
				rows = headers;
				cols = new String[headers.length + 1];
				cols[0] = name;
				for (int i = 0; i < headers.length; i++)
					cols[i+1] = headers[i];
				
				addMouseListener(this);
				
				setModel(new MyTableModel());
				setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				setAutoCreateRowSorter(true);
				
				getTableHeader().setFont(monoFont);
				//getTableHeader().setResizingAllowed(false);
				getTableHeader().setReorderingAllowed(false);
				
				setDefaultRenderer(String.class, new MyCellRenderer());
				//setDefaultRenderer(String.class, new ExCellRenderer());
			}
		}
		
		private class MyTableModel extends AbstractTableModel {
			
			public int getRowCount() {
				if (tableType == 0)
					return data.length;
				else
					return data2.length;
			}
			
			public int getColumnCount() {
				return cols.length;
			}
			
			public Object getValueAt(int row, int col) {
				if (col == 0)
					return rows[row];
				else if (tableType == 0)
					return numFormat.format(data[row][col-1]);
				else
					return data2[row][col-1];
			}
			
			public Class<?> getColumnClass(int col) {
				if (col == 0)
					return String.class;
				else if (tableType == 0)
					return Double.class;
				else
					return String.class;
			}
			
			public String getColumnName(int col) {
				return cols[col];
			}
			
			/*public boolean isCellEditable(int row, int col) {
				return true;
			}
			
			public void setValueAt(Object value, int row, int col) {
				if ( dblParse(value.toString()) ) {
					data[row][col] = Double.parseDouble(value.toString());
					fireTableCellUpdated(row, col);
				}
			}*/
		}
		
		private class MyCellRenderer extends DefaultTableCellRenderer {
			
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				
				setFont(monoFont);
				setHorizontalAlignment(SwingConstants.RIGHT);
				setForeground(Color.BLACK);
				
				if (checkDouble((String)value)) {
					double val = Double.parseDouble((String)value);
					int ind = (int)Math.round(val*10);
					if (ind >= 0 && ind <= 10 && hasColors)
						setForeground(htmlColor(colors[ind]));
						//setForeground(gradColor(ind, 11));
					
					//setToolTipText("ind " + Math.round(val*10));
				}
				
				if (hasFocus)
					setBackground(backClr);
				else if (isSelected)
					setBackground(Color.LIGHT_GRAY);
				else
					setBackground(Color.WHITE);
				
				return this;
			}
		}
		
		/*private class ExCellRenderer extends DefaultTableCellRenderer {
			
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus, int row, int col) {
				
				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
				
				setFont(monoFont);
				setHorizontalAlignment(SwingConstants.CENTER);
				
				return this;
			}
		}*/
		
		public Component prepareRenderer(TableCellRenderer rend, int row, int col) {
			
			Component comp = super.prepareRenderer(rend, row, col);
			
			/// Make first column appear as header
			if (col == 0)
				comp = this.getTableHeader().getDefaultRenderer()
						.getTableCellRendererComponent(this, this.getValueAt(row, col), false, false, row, col);
			
			/// Dynamic size of the columns
			TableColumn tableCol = this.getColumnModel().getColumn(col);
			tableCol.setPreferredWidth(Math.max(tableCol.getPreferredWidth(),
					comp.getPreferredSize().width + this.getIntercellSpacing().width + 10));
			
			return comp;
		}
		
		public void mouseClicked(MouseEvent evt) {}
		public void mouseEntered(MouseEvent evt) {}
		public void mouseExited(MouseEvent evt) {}
		
		public void mousePressed(MouseEvent evt) {
			//int valR = this.rowAtPoint(evt.getPoint());
			//int valC = this.columnAtPoint(evt.getPoint());
			//System.out.println("mouse " + this.getValueAt(valR, valC));
			
			//int valR = this.getSelectedRow();
			//int valC = this.getSelectedColumn();
			//System.out.println("mouse " + valR + " " + valC);
			
			if (result != null) {
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				resPanel.setMyMarker(this.getSelectedRow(), this.getSelectedColumn()-1);
				resPanel.repaint();
				resPanel.revalidate();
			}
		}
		
		public void mouseReleased(MouseEvent evt) {
			if (result != null) {
				MyPanel resPanel = (MyPanel)resScroll.getViewport().getView();
				resPanel.setMyMarker(-1, -1);
				resPanel.repaint();
				resPanel.revalidate();
			}
		}
		
	}
	
	/// Check if string is integer
	private boolean checkInt(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	/// Check if string is double
	private boolean checkDouble(String str) {
		try {
			Double.parseDouble(str);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
	}
	
	/// Color gradient from red to green
	/*private Color gradColor(int ind, int total) {
		
		Color c1 = Color.RED;
		Color c2 = Color.GREEN;
		
		float ratio = (float) ind/total;
		int vRed = (int)(c2.getRed()*ratio + c1.getRed()*(1-ratio));
		int vGreen = (int)(c2.getGreen()*ratio + c1.getGreen()*(1-ratio));
		int vBlue = (int)(c2.getBlue()*ratio + c1.getBlue()*(1-ratio));
		
		return new Color(vRed, vGreen, vBlue);
	}*/
	
	/// Colors for table
	private Color htmlColor(String hex) {
		
		Color res = new Color(0, 0, 0);
		
		if (hex.startsWith("#"))
			hex = hex.substring(1);
		
		if (hex.length() == 6) {
			int red = Integer.parseInt(hex.substring(0, 2), 16);
			int green = Integer.parseInt(hex.substring(2, 4), 16);
			int blue = Integer.parseInt(hex.substring(4, 6), 16);
			
			res = new Color(red, green, blue);
		}
		
		return res;
	}
	
	/// Find separator
	private String findSep(String line) {
		
		String separator = "";
		if (line.contains("\t"))
			separator = "\t";
		else if (line.contains(";"))
			separator = ";";
		else if (line.contains(","))
			separator = ",";
		
		return separator;
	}
	
	/// Find column
	private int findCol(Vector<String> vec, String separator) {
		
		String[] arr = vec.get(0).split(separator);
		int col = arr.length;
		
		for (int i = 1; i < vec.size(); i++) {
			arr = vec.get(i).split(separator);
			if (col != arr.length)
				return 0;
		}
		
		return col;
	}
	
	/// Transpose matrix
	private double[][] transposeMatrix(double[][] arr) {
		
		double[][] res = new double[arr[0].length][arr.length];
		for (int i = 0; i < arr[0].length; i++)
			for (int j = 0; j < arr.length; j++)
				res[i][j] = arr[j][i];
		
		return res;
	}
	
	/// Read matrix
	private double[][] readMatrix(Vector<String> vec, boolean vTranspose) {
		
		String separator = "";
		if ((separator = findSep(vec.get(0))) == "") {
			showMessage("warn", "Could not determine column separator");
			return null;
		}
		
		int col = 0;
		if ((col = findCol(vec, separator)) == 0) {
			showMessage("warn", "Columns are inconsistent");
			return null;
		}
		
		if (col < 3 || vec.size() < 3) {
			showMessage("warn", "Minimum matrix size is 3x3");
			return null;
		}
		
		String[] arr = vec.get(0).split(separator);
		double[][] res = new double[vec.size()][arr.length];
		
		for (int i = 0; i < vec.size(); i++) {
			arr = vec.get(i).split(separator);
			for (int j = 0; j < arr.length; j++) {
				String val = arr[j].replace(",", ".");
				if (checkDouble(val))
					res[i][j] = Double.parseDouble(val);
			}
		}
		
		if (vTranspose)
			res = transposeMatrix(res);
		
		return res;
	}
	
	/// Read vector (can contain more than one matrix)
	private Vector<double[][]> readVector(Vector<String> vec, int vMatCount, boolean vTranspose) {
		
		if (vec.size()%vMatCount != 0) {
			showMessage("warn", "Input data length must be fully divisible by matrix count");
			return null;
		}
		
		Vector<double[][]> res = new Vector<double[][]>();
		int vRows = vec.size()/vMatCount;
		for (int i = 0; i < vec.size(); i=i+vRows) {
			Vector<String> partData = new Vector<String>();
			for (int j = i; j < i+vRows; j++)
				partData.add(vec.get(j));
			
			double[][] matData = readMatrix(partData, vTranspose);
			if (matData != null)
				res.add(matData);
			else
				return null;
		}
		
		return res;
	}
	
	/// Load data from the user input
	private void btnAnalysisListener(ActionEvent evt) {
		
		/// Read global variables
		int vType = comboType.getSelectedIndex();
		int vAggr = comboAggr.getSelectedIndex();
		int vMatCount = (int)spinMatCount.getValue();
		boolean vOrdPair = chkOrdPair.isSelected();
		boolean vTranspose = chkTranspose.isSelected();
		int vMethod = comboMethod.getSelectedIndex();
		
		/// Local variables
		Vector<String> vec1 = new Vector<String>();
		Vector<String> vec2 = new Vector<String>();
		String strObj = "";
		String strCrit = "";
		
		/// Read user input
		try {
			String data = textA.getText();
			if (data.length() == 0) {
				showMessage("info", "Nothing to load");
				return;
			}
			
			boolean mark1 = false;
			boolean mark2 = false;
			
			String[] lines = data.split("\n");
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i].trim();
				
				if (line.startsWith("#objectnames:") && !vTranspose)
					strObj = line.substring(13);
				if (line.startsWith("#criternames:") && !vTranspose)
					strCrit = line.substring(13);
				
				if (line.startsWith("#objectnames:") && vTranspose)
					strCrit = line.substring(13);
				if (line.startsWith("#criternames:") && vTranspose)
					strObj = line.substring(13);
				
				if (mark1 && !mark2 && !line.startsWith("#") && line.length() != 0)
					vec1.add(line);
				if (!mark1 && mark2 && !line.startsWith("#") && line.length() != 0)
					vec2.add(line);
				
				if (line.startsWith("#input1")) {
					mark1 = true;
					mark2 = false;
				}
				if (line.startsWith("#input2")) {
					mark1 = false;
					mark2 = true;
				}
			}
			
		} catch (Exception ex) {
			showMessage("error", "Could not load the data");
			return;
		}
		
		
		/// Make result
		try {
			if (vec1.size() > 0 && vec2.size() > 0 && vOrdPair) {
				double[][] res1 = readMatrix(vec1, vTranspose);
				double[][] res2 = readMatrix(vec2, vTranspose);
				if (res1 == null || res2 == null) {
					showMessage("warn", "Could not read matrix");
					return;
				}
				
				if (res1.length != res2.length || res1[0].length != res2[0].length) {
					showMessage("warn", "Matrices must be of the same size");
					return;
				}
				
				if (vType == 0) {
					/// Result
					result = makeCalc(res1, res2, vMethod, vOrdPair);
					
					/// Headers
					objNames = makeHeader(strObj, res1[0].length, vTranspose, false);
					critNames = makeHeader(strCrit, res1.length, vTranspose, true);
					objNames2 = objNames;
					critNames2 = critNames;
					
					showMessage("info", "Ordered pair - " + typeNames[vType] + " - " + methodNames[vMethod]);
					
				} else if (vType == 1) {
					
					Vector<double[][]> vector1 = readVector(vec1, vMatCount, vTranspose);
					Vector<double[][]> vector2 = readVector(vec2, vMatCount, vTranspose);
					if (vector1 != null && vector2 != null) {
						/// Result
						result = makeSecondOrder(vector1, vector2, vMethod, vOrdPair);
						
						/// Headers
						int vsize = vector1.get(0).length;
						objNames = makeHeader(strObj, (vsize*vsize-vsize)/2, vTranspose, false);
						critNames = makeHeader(strCrit, vector1.size(), vTranspose, true);
						objNames2 = makeHeader(strObj, vector1.get(0)[0].length, vTranspose, false);
						critNames2 = makeHeader(strCrit, vector1.get(0).length, vTranspose, true);
						
						showMessage("info", "Ordered pair - " + typeNames[vType] + " - " + methodNames[vMethod]);
					} else
						showMessage("warn", "Ordered pair - " + typeNames[vType] + " - failed to read data");
					
				} else {
					Vector<double[][]> vector1 = readVector(vec1, vMatCount, vTranspose);
					Vector<double[][]> vector2 = readVector(vec2, vMatCount, vTranspose);
					if (vector1 != null && vector2 != null) {
						/// Result
						result = makeAggregated(vector1, vector2, vAggr, vMethod, vOrdPair);
						
						/// Headers
						objNames = makeHeader(strObj, vector1.get(0)[0].length, vTranspose, false);
						critNames = makeHeader(strCrit, vector1.get(0).length, vTranspose, true);
						objNames2 = objNames;
						critNames2 = critNames;
						
						showMessage("info", "Ordered pair - " + typeNames[vType] + " - " + aggrNames[vAggr] + " - " + methodNames[vMethod]);
					} else
						showMessage("warn", "Ordered pair - " + typeNames[vType] + " - failed to read data");
				}
				
			} else if (vec1.size() > 0 && !vOrdPair) {
				double[][] res1 = readMatrix(vec1, vTranspose);
				if (res1 == null) {
					showMessage("warn", "Could not read matrix");
					return;
				}
				
				if (vType == 0) {
					/// Result
					result = makeCalc(res1, null, vMethod, vOrdPair);
					
					/// Headers
					objNames = makeHeader(strObj, res1[0].length, vTranspose, false);
					critNames = makeHeader(strCrit, res1.length, vTranspose, true);
					objNames2 = objNames;
					critNames2 = critNames;
					
					showMessage("info", typeNames[vType] + " - " + methodNames[vMethod]);
					
				} else if (vType == 1) {
					
					Vector<double[][]> vector1 = readVector(vec1, vMatCount, vTranspose);
					if (vector1 != null) {
						/// Result
						result = makeSecondOrder(vector1, null, vMethod, vOrdPair);
						
						/// Headers
						int vsize = vector1.get(0).length;
						objNames = makeHeader(strObj, (vsize*vsize-vsize)/2, vTranspose, false);
						critNames = makeHeader(strCrit, vector1.size(), vTranspose, true);
						objNames2 = makeHeader(strObj, vector1.get(0)[0].length, vTranspose, false);
						critNames2 = makeHeader(strCrit, vector1.get(0).length, vTranspose, true);
						
						showMessage("info", typeNames[vType] + " - " + methodNames[vMethod]);
					} else
						showMessage("warn", typeNames[vType] + " - failed to read data");
					
				} else {
					Vector<double[][]> vector1 = readVector(vec1, vMatCount, vTranspose);
					if (vector1 != null) {
						/// Result
						result = makeAggregated(vector1, null, vAggr, vMethod, vOrdPair);
						
						/// Headers
						objNames = makeHeader(strObj, vector1.get(0)[0].length, vTranspose, false);
						critNames = makeHeader(strCrit, vector1.get(0).length, vTranspose, true);
						objNames2 = objNames;
						critNames2 = critNames;
						
						showMessage("info", typeNames[vType] + " - " + aggrNames[vAggr] + " - " + methodNames[vMethod]);
					} else
						showMessage("warn", typeNames[vType] + " - failed to read data");
				}
				
			} else if (vec1.size() > 0 && vOrdPair)
				showMessage("warn", "Ordered pair requires two data sets");
				
			else
				showMessage("warn", "No data to read");
			
		} catch (Exception ex) {
			showMessage("error", "Could not make the calculations");
			return;
		} catch (Error err) {
			showMessage("error", "Not enough memory. Start the application with: " + ls +
					"  java -Xmx10240m -jar ICrAData.jar");
			return;
		}
		
		
		/// Display the result
		try {
			if (result != null) {
				
				/// Create the tables
				MyTable tableA = new MyTable(result.get("MatrixMU"), critNames, MU, true);
				MyTable tableB = new MyTable(result.get("MatrixNU"), critNames, NU, true);
				scrollA.setViewportView(tableA);
				scrollB.setViewportView(tableB);
				
				/// Create the graphic
				double[][] arrPoints = result.get("PlotPoints");
				MyPanel resPanel = new MyPanel(arrPoints);
				resPanel.setPreferredSize(new Dimension(valSize, valSize));
				resPanel.setMyPoint(pointSize);
				resScroll.setViewportView(resPanel);
				
				/// Reset combo boxes - these events must be fired after resPanel is created, otherwise NullPointerException
				comboTable.setSelectedIndex(0); /// event fired
				comboSize.setSelectedItem(String.valueOf(valSize)); /// event fired
				comboGrid.setSelectedIndex(0); /// event fired
			}
			
		} catch (Exception ex) {
			showMessage("error", "Could not display the result");
		}
	}
	
	/// Make matrix header
	private String[] makeHeader(String vStr, int vSize, boolean vTranspose, boolean vCrit) {
		
		/// Align digits formatting
		/// Value of 30 is 2 digits, so it is 2 zeros for R01, C12 
		/// value of 250 is 3 digits, so it is 3 zeros for R003, C111
		String fAlign = "";
		for (int i = 0; i < String.valueOf(vSize).length(); i++)
			fAlign += "0";
		
		/// Symbols
		DecimalFormat fSymb = new DecimalFormat(fAlign, new DecimalFormatSymbols(Locale.US));
		
		/// Automatic headers
		String[] arrNames = new String[vSize];
		for (int i = 0; i < arrNames.length; i++)
			arrNames[i] = (vCrit ? (vTranspose ? "O" : "C") : (vTranspose ? "C" : "O")) +
				fSymb.format(Integer.valueOf(i + 1));
		
		/// Manual headers
		String separator = "";
		if (vStr.length() > 0 && (separator = findSep(vStr)) != "") {
			String[] arrStr = vStr.split(separator);
			for (int i = 0; i < arrStr.length && i < arrNames.length; i++)
				if (arrStr[i].trim().length() > 0)
					arrNames[i] = arrStr[i].trim();
		}
		
		return arrNames;
	}
	
	/// Make Second Order ICrA
	private HashMap<String, double[][]> makeSecondOrder(
			Vector<double[][]> vecU, Vector<double[][]> vecU2,
			int method, boolean isPair) {
		
		try {
			int vecsize = vecU.size();
			int matsize = vecU.get(0).length;
			int secsize = (matsize*matsize-matsize)/2;
			
			HashMap<String, double[][]> res = new HashMap<String, double[][]>();
			HashMap<String, double[][]> calc = null;
			double[][] calcA = new double[vecsize][secsize];
			double[][] calcB = new double[vecsize][secsize];
			
			/// Save calculations in calcA and calcB
			for (int k = 0; k < vecsize; k++) {
				calc = (isPair ?
					makeCalc(vecU.get(k), vecU2.get(k), method, isPair) :
					makeCalc(vecU.get(k), null, method, isPair));
				
				double[][] mat = calc.get("MatrixMU");
				int cc = 0;
				for (int i = 0; i < matsize; i++)
					for (int j = 0; j < matsize; j++)
						if (i < j)
							calcA[k][cc++] = mat[i][j];
				
				mat = calc.get("MatrixNU");
				cc = 0;
				for (int i = 0; i < matsize; i++)
					for (int j = 0; j < matsize; j++)
						if (i < j)
							calcB[k][cc++] = mat[i][j];
				
				Iterator<String> it = calc.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					res.put(Integer.valueOf(k + 1) + key, calc.get(key));
				}
			}
			
			/// Second Order ICrA
			calc = makeCalc(calcA, calcB, method, true);
			Iterator<String> it = calc.keySet().iterator();
			while (it.hasNext()) {
				String key = it.next();
				res.put(key, calc.get(key));
			}
			
			return res;
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	/// Make Aggregated ICrA
	private HashMap<String, double[][]> makeAggregated(
			Vector<double[][]> vecU, Vector<double[][]> vecU2,
			int aggr, int method, boolean isPair) {
		
		try {
			int vecsize = vecU.size();
			int matsize = vecU.get(0).length;
			
			HashMap<String, double[][]> res = new HashMap<String, double[][]>();
			double[][] arrA = new double[matsize][matsize];
			double[][] arrB = new double[matsize][matsize];
			
			HashMap<String, double[][]> calc = null;
			double[][][] calcA = new double[vecsize][matsize][matsize];
			double[][][] calcB = new double[vecsize][matsize][matsize];
			
			/// Save calculations in calcA and calcB
			for (int k = 0; k < vecsize; k++) {
				calc = (isPair ?
					makeCalc(vecU.get(k), vecU2.get(k), method, isPair) :
					makeCalc(vecU.get(k), null, method, isPair));
				
				double[][] mat = calc.get("MatrixMU");
				for (int i = 0; i < matsize; i++)
					for (int j = 0; j < matsize; j++)
						calcA[k][i][j] = mat[i][j];
				
				mat = calc.get("MatrixNU");
				for (int i = 0; i < matsize; i++)
					for (int j = 0; j < matsize; j++)
						calcB[k][i][j] = mat[i][j];
				
				Iterator<String> it = calc.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					res.put(Integer.valueOf(k + 1) + key, calc.get(key));
				}
			}
			
			/// Save the aggregation in arrA and arrB
			/// Average 0  MaxMin 1  MinMax 2
			for (int i = 0; i < matsize; i++) {
				for (int j = 0; j < matsize; j++) {
					if (aggr == 0) {
						double valA = 0;
						double valB = 0;
						for (int k = 0; k < vecsize; k++) {
							valA += calcA[k][i][j];
							valB += calcB[k][i][j];
						}
						
						arrA[i][j] = (double)valA/vecsize;
						arrB[i][j] = (double)valB/vecsize;
						
					} else if (aggr == 1) {
						double valA = 0;
						double valB = 1000000;
						for (int k = 0; k < vecsize; k++) {
							if (valA < calcA[k][i][j])
								valA = calcA[k][i][j];
							if (valB > calcB[k][i][j])
								valB = calcB[k][i][j];
						}
						
						arrA[i][j] = valA;
						arrB[i][j] = valB;
						
					} else {
						double valA = 1000000;
						double valB = 0;
						for (int k = 0; k < vecsize; k++) {
							if (valA > calcA[k][i][j])
								valA = calcA[k][i][j];
							if (valB < calcB[k][i][j])
								valB = calcB[k][i][j];
						}
						
						arrA[i][j] = valA;
						arrB[i][j] = valB;
					}
				}
			}
			
			/// Make distance
			double[][] arrDist = makeDistance(arrA, arrB);
			
			/// Plot points
			double[][] arrPoints = makePlotData(arrA, arrB, arrDist);
			
			/// Vector data for export
			double[][] vectorA = makeVectorData(arrA);
			double[][] vectorA2 = makeVectorData2(arrA);
			double[][] vectorB = makeVectorData(arrB);
			double[][] vectorB2 = makeVectorData2(arrB);
			double[][] vectorDist = makeVectorData(arrDist);
			double[][] vectorDist2 = makeVectorData2(arrDist);
			
			/// Save the result
			res.put("MatrixMU", arrA);
			res.put("MatrixNU", arrB);
			res.put("MatrixDist", arrDist);
			res.put("PlotPoints", arrPoints);
			
			res.put("VectorMU", vectorA);
			res.put("VectorMUalt", vectorA2);
			res.put("VectorNU", vectorB);
			res.put("VectorNUalt", vectorB2);
			res.put("VectorDist", vectorDist);
			res.put("VectorDistalt", vectorDist2);
			
			return res;
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	/// Make the calculations
	private HashMap<String, double[][]> makeCalc(double[][] arrU, double[][] arrU2, int method, boolean isPair) {
		
		try {
			/// Criteria matrix
			double[][] arrV = new double[1][1];
			/// Sign matrix
			double[][] arrS = new double[1][1];
			
			if (isPair)
				arrS = makeCritSign(arrU, arrU2);
			else {
				arrV = makeCrit(arrU);
				arrS = makeSign(arrV);
			}
			
			/// Matrix MU and NU
			double[][] arrA = new double[1][1];
			double[][] arrB = new double[1][1];
			
			/// MU-biased 0  Unbiased 1  NU-biased 2  Balanced 3  Weighted 4
			if (method == 3) {
				/// Make MU-biased 0
				double[][] arrA0 = makeA(arrS, 0);
				double[][] arrB0 = makeB(arrS, 0);
				/// Make NU-biased 2
				double[][] arrA2 = makeA(arrS, 2);
				double[][] arrB2 = makeB(arrS, 2);
				/// Balanced 3
				arrA = matrixMeanValue(arrA0, arrA2);
				arrB = matrixMeanValue(arrB0, arrB2);
				
			} else if (method == 4) {
				/// Make Unbiased 1
				double[][] arrA1 = makeA(arrS, 1);
				double[][] arrB1 = makeB(arrS, 1);
				/// Make P matrix = MU+NU
				double[][] arrP = matrixAddition(arrA1, arrB1);
				//System.out.println(showArray(arrP, ";", true));
				/// Weighted 4
				arrA = matrixWeighted(arrA1, arrP);
				arrB = matrixWeighted(arrB1, arrP);
				
			} else {
				arrA = makeA(arrS, method);
				arrB = makeB(arrS, method);
			}
			
			/// Make distance
			double[][] arrDist = makeDistance(arrA, arrB);
			
			/// Plot points
			double[][] arrPoints = makePlotData(arrA, arrB, arrDist);
			
			/// Vector data for export
			double[][] vectorA = makeVectorData(arrA);
			double[][] vectorA2 = makeVectorData2(arrA);
			double[][] vectorB = makeVectorData(arrB);
			double[][] vectorB2 = makeVectorData2(arrB);
			double[][] vectorDist = makeVectorData(arrDist);
			double[][] vectorDist2 = makeVectorData2(arrDist);
			
			/// Result as hash map
			HashMap<String, double[][]> res = new HashMap<String, double[][]>();
			res.put("Input1", arrU);
			if (isPair)
				res.put("Input2", arrU2);
			else
				res.put("CriteriaMatrix", arrV);
			res.put("SignMatrix", arrS);
			
			res.put("MatrixMU", arrA);
			res.put("MatrixNU", arrB);
			res.put("MatrixDist", arrDist);
			res.put("PlotPoints", arrPoints);
			
			res.put("VectorMU", vectorA);
			res.put("VectorMUalt", vectorA2);
			res.put("VectorNU", vectorB);
			res.put("VectorNUalt", vectorB2);
			res.put("VectorDist", vectorDist);
			res.put("VectorDistalt", vectorDist2);
			
			return res;
			
		} catch (Exception ex) {
			return null;
		}
	}
	
	/// Make criteria matrix
	private double[][] makeCrit(double[][] arr) {
		
		int rows = arr.length;
		int cols = arr[0].length;
		double[][] res = new double[rows][(cols*(cols-1))/2];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int k = 0; k < cols-1; k++) {
				for (int j = k; j < cols-1; j++) {
					res[i][cc] = arr[i][k] - arr[i][j+1];
					cc++;
				}
			}
			cc = 0;
		}
		
		return res;
	}
	
	/// Make sign matrix
	private double[][] makeSign(double[][] arr) {
		
		int rows = arr.length;
		int cols = arr[0].length;
		double[][] res = new double[rows][cols];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				res[i][j] = Math.signum(arr[i][j]);
			}
		}
		
		return res;
	}
	
	/// Make criteria/sign matrix for ordered pair
	private double[][] makeCritSign(double[][] arr, double[][] arr2) {
		
		int rows = arr.length;
		int cols = arr[0].length;
		double[][] res = new double[rows][(cols*(cols-1))/2];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int k = 0; k < cols-1; k++) {
				for (int j = k; j < cols-1; j++) {
					if (arr[i][k] >= arr[i][j+1] && arr2[i][k] < arr2[i][j+1])
						res[i][cc] = 1;
					else if (arr[i][k] > arr[i][j+1] && arr2[i][k] <= arr2[i][j+1])
						res[i][cc] = 1;
					else if (arr[i][k] <= arr[i][j+1] && arr2[i][k] > arr2[i][j+1])
						res[i][cc] = -1;
					else if (arr[i][k] < arr[i][j+1] && arr2[i][k] >= arr2[i][j+1])
						res[i][cc] = -1;
					else if (arr[i][k] == arr[i][j+1] && arr2[i][k] == arr2[i][j+1])
						res[i][cc] = 0;
					else
						res[i][cc] = 9;
					cc++;
				}
			}
			cc = 0;
		}
		
		return res;
	}
	
	/// Make MU-A
	private double[][] makeA(double[][] arr, int method) {
		
		int rows = arr.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (method == 0)
					res[i][j] = sameElem(arr[i], arr[j]);
				else if (method == 1)
					res[i][j] = sameElemNoZero(arr[i], arr[j]);
				else
					res[i][j] = sameElemNoZero(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Make NU-B
	private double[][] makeB(double[][] arr, int method) {
		
		int rows = arr.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (method == 0)
					res[i][j] = diffElem(arr[i], arr[j]);
				else if (method == 1)
					res[i][j] = diffElem(arr[i], arr[j]);
				else
					res[i][j] = diffElemNoZero(arr[i], arr[j]);
			}
		}
		
		return res;
	}
	
	/// Same elements for MU-A:  0=0  -1=-1  1=1
	private double sameElem(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == 1) && (arrB[i] == 1)) ||
				((arrA[i] == -1) && (arrB[i] == -1)) ||
				((arrA[i] == 0) && (arrB[i] == 0)) )
				cc++;
			if (chkCount.isSelected() && (arrA[i] == 9) && (arrB[i] == 9))
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Same elements for MU-A:  0=/=0  -1=-1  1=1
	private double sameElemNoZero(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == 1) && (arrB[i] == 1)) ||
				((arrA[i] == -1) && (arrB[i] == -1)) )
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Different elements for NU-B:  0=0  -1=/=1  1=/=-1
	private double diffElem(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == -1) && (arrB[i] == 1)) ||
				((arrA[i] == 1) && (arrB[i] == -1)) )
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Different elements for NU-B:  0=/=0  -1=/=1  1=/=-1
	private double diffElemNoZero(double[] arrA, double[] arrB) {
		
		int cc = 0;
		for (int i = 0; i < arrA.length; i++) {
			if( ((arrA[i] == -1) && (arrB[i] == 1)) ||
				((arrA[i] == 1) && (arrB[i] == -1)) ||
				((arrA[i] == 0) && (arrB[i] == 0)) )
				cc++;
			if (chkCount.isSelected() && (arrA[i] == 9) && (arrB[i] == 9))
				cc++;
		}
		
		return (double) cc/arrA.length;
	}
	
	/// Mean value for two matrices - (A1{1,1} + A2{1,1})/2
	private double[][] matrixMeanValue(double[][] arrA, double[][] arrB) {
		
		int rows = arrA.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < rows; j++)
				res[i][j] = (arrA[i][j] + arrB[i][j])/2;
		
		return res;
	}
	
	/// Add two matrices - A1{1,1} + A2{1,1}
	private double[][] matrixAddition(double[][] arrA, double[][] arrB) {
		
		int rows = arrA.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < rows; j++)
				res[i][j] = arrA[i][j] + arrB[i][j];
		
		return res;
	}
	
	/// Matrix for method Weighted
	private double[][] matrixWeighted(double[][] arr, double[][] arrP) {
		
		int rows = arr.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				if (arrP[i][j] == 0)
					res[i][j] = 0.5;
				else
					res[i][j] = arr[i][j]/arrP[i][j];
			}
		}
		
		return res;
	}
	
	/// Make distance
	private double[][] makeDistance(double[][] arrA, double[][] arrB) {
		
		int rows = arrA.length;
		double[][] res = new double[rows][rows];
		
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < rows; j++)
				res[i][j] = Math.sqrt((1-arrA[i][j])*(1-arrA[i][j]) + arrB[i][j]*arrB[i][j]);
				//res[i][j] = Math.sqrt(Math.pow(1-arrA[i][j], 2) + Math.pow(arrB[i][j], 2));
				//res[i][j] = Math.pow(Math.pow(1-arrA[i][j], 2) + Math.pow(arrB[i][j], 2), 0.5);
		
		return res;
	}
	
	/// Make plot data points
	private double[][] makePlotData(double[][] arrA, double[][] arrB, double[][] arrDist) {
		
		int rows = arrA.length;
		/// Upper triangular matrix - size of square matrix minus the diagonal elements divided by two
		double[][] res = new double[(rows*rows-rows)/2][5];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get upper triangular matrix and diagonal elements
				if ( i < j ) {
					res[cc][0] = arrA[i][j]; // coordinate x
					res[cc][1] = arrB[i][j]; // coordinate y
					res[cc][2] = i; // index row
					res[cc][3] = j; // index column
					res[cc][4] = arrDist[i][j]; // distance
					cc++;
				}
			}
		}
		
		return res;
	}
	
	/// Make vector data
	private double[][] makeVectorData(double[][] arr) {
		
		int rows = arr.length;
		/// Upper triangular matrix - size of square matrix minus the diagonal elements divided by two
		double[][] res = new double[(rows*rows-rows)/2][1];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get upper triangular matrix
				if ( i < j ) {
					res[cc][0] = arr[i][j];
					cc++;
				}
			}
		}
		
		return res;
	}
	
	/// Make vector data 2
	private double[][] makeVectorData2(double[][] arr) {
		
		int rows = arr.length;
		/// Lower triangular matrix - size of square matrix minus the diagonal elements divided by two
		double[][] res = new double[(rows*rows-rows)/2][1];
		
		int cc = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < rows; j++) {
				/// Get lower triangular matrix
				if ( i > j ) {
					res[cc][0] = arr[i][j];
					cc++;
				}
			}
		}
		
		return res;
	}
	
	/// Display array
	private String showArray(double[][] arr, String separator, Locale locale, int digits, String[] header, boolean showHeader) {
		
		/// Make 0.0000, must be with dot, not with comma
		String strDig = "0.";
		for (int i = 0; i < digits; i++)
			strDig += "0";
		
		/// DecimalFormat usFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));
		/// DecimalFormat bgFormat = new DecimalFormat("0.0000", new DecimalFormatSymbols(new Locale("bg", "BG")));
		DecimalFormat decFormat = new DecimalFormat(strDig, new DecimalFormatSymbols(locale));
		
		String endLine = ls;
		if (separator == " & ")
			endLine = " \\\\" + ls;
		
		/// StringBuilder is created much faster than String
		StringBuilder res = new StringBuilder();
		
		if (showHeader && header != null) {
			for (int i = 0; i < header.length; i++) {
				res.append(header[i]);
				if (i != header.length-1)
					res.append(separator);
			}
			res.append(endLine);
		}
		
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				res.append(decFormat.format(arr[i][j]));
				
				if (j != arr[i].length-1)
					res.append(separator);
			}
			
			res.append(endLine);
		}
		
		return res.toString();
	}
	
	
	/// LINKS
	
	/// http://docs.oracle.com/javase/tutorial/2d/
	/// http://www.w3schools.com/colors/colors_groups.asp
	
	/// http://stackoverflow.com/questions/340209/generate-colors-between-red-and-green-for-a-power-meter
	/// http://www.javamex.com/tutorials/conversion/decimal_hexadecimal.shtml
	/// http://stackoverflow.com/questions/2303305/window-resize-event
	/// http://stackoverflow.com/questions/2106367/listen-to-jframe-resize-events-as-the-user-drags-their-mouse  super.validate()
	/// http://stackoverflow.com/questions/11375250/set-tooltip-text-at-a-particular-location
	
	/// http://stackoverflow.com/questions/5960799/java-glass-pane
	/// http://stackoverflow.com/questions/2561690/placing-component-on-glass-pane
	
	/// http://stackoverflow.com/questions/1155137/how-to-keep-a-single-column-from-being-reordered-in-a-jtable
	/// http://stackoverflow.com/questions/8776540/painting-over-the-top-of-components-in-swing
	
	/// http://stackoverflow.com/questions/5764467/get-component-from-a-jscrollpane
	/// http://stackoverflow.com/questions/8385856/how-to-get-the-component-inside-a-jscrollpane
	
	/// http://stackoverflow.com/questions/6575578/convert-a-graphics2d-to-an-image-or-bufferedimage#6600346
	/// http://stackoverflow.com/questions/8202253/saving-a-java-2d-graphics-image-as-png-file
	
	/// http://stackoverflow.com/questions/16707397/whats-wrong-with-this-simple-double-calculation
	/// http://stackoverflow.com/questions/179427/how-to-resolve-a-java-rounding-double-issue
	/// https://blogs.oracle.com/CoreJavaTechTips/entry/the_need_for_bigdecimal
	
	/// Undo-redo manager
	/// http://stackoverflow.com/questions/12772821/how-to-implement-undo-redo-in-java-for-mvc-model
	/// http://www.javaworld.com/article/2076698/core-java/add-an-undo-redo-function-to-your-java-apps-with-swing.html
	/// http://www.javadocexamples.com/javax/swing/Action/Action.ACCELERATOR_KEY.html
	/// http://www.javadocexamples.com/java_source/org/embl/ebi/escience/scuflworkers/rserv/RservConfigPanel.java.html
	
	/// Change cursor on glass - http://www.javaspecialists.co.za/archive/Issue065.html
	/// Unicode symbols - https://stackoverflow.com/questions/16616162/how-to-write-unicode-cross-symbol-in-java
	
	/// Mouse dragging
	/// http://stackoverflow.com/questions/10243257/java-scroll-image-by-mouse-dragging
	/// http://docs.oracle.com/javase/tutorial/uiswing/components/scrollpane.html
	
	/// Scroll
	/// https://stackoverflow.com/questions/6561246/scroll-event-of-a-jscrollpane
	/// https://stackoverflow.com/questions/13213030/jscrollpane-set-scroll-position
	
	/// Locale - new Locale("bg", "BG")
	/// http://www.oracle.com/technetwork/java/javase/javase7locales-334809.html
	
	/// Gradient
	/// https://stackoverflow.com/questions/27532/generating-gradients-programmatically
	
	/// Mouse click on table
	/// https://stackoverflow.com/questions/7350893/click-event-on-jtable-java?noredirect=1
	/// https://stackoverflow.com/questions/1378096/actionlistener-on-jlabel-or-jtable-cell
	
	/// Auto-resize columns in table
	/// https://stackoverflow.com/questions/17627431/auto-resizing-the-jtable-column-widths
	/// https://stackoverflow.com/questions/17858132/automatically-adjust-jtable-column-to-fit-content
	
	/// This rotates the coordinate system
	/// https://stackoverflow.com/questions/10083913/how-to-rotate-text-with-graphics2d-in-java#10084078
	
	/// Width of string
	/// https://stackoverflow.com/questions/258486/calculate-the-display-width-of-a-string-in-java
	
	/// List single selection
	/// https://stackoverflow.com/questions/23557000/how-to-disable-multiselect-of-listbox-jlist-in-java
	
}
