/* Copyright (c) 2006, Carl Burch. License information is located in the
 * com.cburch.autosim.Main source code and at www.cburch.com/proj/autosim/. */

package com.cburch.autosim;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
    private File curFile = null;
    private JMenuBar menubar = null;
    private JTabbedPane tabbedPane=null;
    private static Canvas currentCanvas;// = new Canvas();
    private static ArrayList<Automaton> disTA = new ArrayList<Automaton>();
    ToolBox toolbox = new ToolBox();
    private char autoName = 'a';
    
    public MainFrame(Automaton initial) {
       
    	setSize(700, 600);
        setTitle("Model Checker");
        setBackground(Color.white);
        
        Listener l = new Listener();
        addWindowListener(l);
        addComponentListener(l);

        //canvas.setToolBox(toolbox);

        menubar = new JMenuBar();
        menubar.add(new MenuFile());
        menubar.add(new MenuHelp());
        setJMenuBar(menubar);

        getContentPane().add(toolbox, BorderLayout.NORTH);

        Panel jPanel1 = new Panel();
        jPanel1.getCanvas().setToolBox(toolbox);
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Process "+ Character.toString(autoName).toUpperCase(), jPanel1 );
        
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
              JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
              int index = sourceTabbedPane.getSelectedIndex();
              if(index != -1)
            	  setCurrentCanvas(((Panel)sourceTabbedPane.getComponentAt(index)).getCanvas());
            }
          };
        tabbedPane.addChangeListener(changeListener);
        
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // setting state tool as default tool, not setting will not set
        // any tool in default way
        setCurrentCanvas(jPanel1.getCanvas());
        toolbox.selectButton(toolbox.getStateTool());
        
        if(initial != null){ 
        	jPanel1.getCanvas().setAutomaton(initial);
        	disTA.add(initial);
        }
        else{
        	Automaton a;
            try {
                a = (Automaton) DisTA.class.newInstance();
            } catch(Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                return;
            }
            a.setAutoName(autoName++);
            disTA.add(a);
            jPanel1.getCanvas().setAutomaton(a);
        }
        
        jPanel1.getCanvas().exposeAll();
        jPanel1.getCanvas().commitTransaction(true);
        computeTitle();
    }

    private void computeTitle() {
        String new_title = "Model Checker";
        if(curFile != null) {
            new_title += ": " + curFile.getName();
        }
        setTitle(new_title);
    }

    public void openAutomaton(File f) throws IOException {
        FileReader fread = new FileReader(f);
        GroupedReader fin = new GroupedReader(fread);
        
        String what = fin.readLine();
        if(!what.equals("Model Checker, " + Main.FILE_VERSION_NAME)) {
            throw new IOException("unrecognized file version");
        }

        String type = fin.readLine();
        disTA = new ArrayList<Automaton>();

        while(type != null){
        	type = type.trim();
        	
            DisTA automaton;
			if(type.startsWith("DisTA")) automaton = new DisTA();
            else throw new IOException("unknown automaton type");
            
        	Automaton.read(automaton,fin);
        	
        	disTA.add(automaton);
        	type = fin.readLine();
        }
        autoName = 'a';
        tabbedPane.removeAll();
        for (Automaton automaton : disTA) {

        	Panel jPanel1 = new Panel();
            tabbedPane.addTab("Process "+ Character.toString(autoName).toUpperCase(), jPanel1 );
            getContentPane().add(tabbedPane, BorderLayout.CENTER);
    		
    		int count = tabbedPane.getTabCount();
    		tabbedPane.setSelectedIndex(count-1);
            automaton.setAutoName(autoName++);
        	
            // setting state tool as default tool, not setting will not set
            // any tool in default way
            toolbox.selectButton(toolbox.getStateTool());
            jPanel1.getCanvas().setToolBox(toolbox);
            jPanel1.getCanvas().setAutomaton(automaton);
            jPanel1.getCanvas().exposeAll();
            jPanel1.getCanvas().commitTransaction(true );
            jPanel1.getCanvas().computeSize();
        
		}
        computeTitle();
    }

    public void doQuit() {
        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public static Canvas getCurrentCanvas() {
		return currentCanvas;
	}

	public void setCurrentCanvas(Canvas currentCanvas) {
		MainFrame.currentCanvas = currentCanvas;
	}

	private class PrintItem implements Pageable {
        public int getNumberOfPages() {
            return 1;
        }
        public PageFormat getPageFormat(int which)
                throws IndexOutOfBoundsException {
            if(which < 0 || which >= getNumberOfPages()) {
                throw new IndexOutOfBoundsException();
            }
            PageFormat format;
            format = new PageFormat();
            format.setOrientation(PageFormat.LANDSCAPE);
            return format;
        }
        public Printable getPrintable(int which)
                throws IndexOutOfBoundsException {
            if(which < 0 || which >= getNumberOfPages()) {
                throw new IndexOutOfBoundsException();
            }
            return (Printable) getCurrentCanvas();
        }
    }

    private class MenuFile extends JMenu implements ActionListener {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		private JFileChooser chooser
            = new JFileChooser(System.getProperty("user.dir"));

        private JMenuItem newProcess;
        private JMenuItem newProject;
        private JMenuItem open;
        private JMenuItem save;
        private JMenuItem print;
        private JMenuItem quit;

        public MenuFile() {
            super("File");
            setPopupMenuVisible(true);

            JMenu newMenu = new JMenu("New");
            newProcess = create(newMenu, "Process");
            newProject = create(newMenu, "Project");

            this.add(newMenu);
            open = create(this, "Open", KeyEvent.VK_O);
            save = create(this, "Save", KeyEvent.VK_S);
            print = create(this, "Print", KeyEvent.VK_P);
            quit = create(this, "Quit", KeyEvent.VK_Q);
        }

        private JMenuItem create(JMenu dest, String title) {
            JMenuItem ret = new JMenuItem(title);
            ret.addActionListener(this);
            dest.add(ret);
            return ret;
        }

        private JMenuItem create(JMenu dest, String title, int accel) {
            JMenuItem ret = create(dest, title);
            int mask = ret.getToolkit().getMenuShortcutKeyMask();
            ret.setAccelerator(KeyStroke.getKeyStroke(accel, mask));
            return ret;
        }

        public void actionPerformed(ActionEvent event) {
            Object src = event.getSource();
            if(src == newProcess)       doNewTab(DisTA.class);
            else if(src == newProject)  doNew(DisTA.class);
            else if(src == open)    doOpen();
            else if(src == save)    doSave();
            else if(src == print)   doPrint();
            else if(src == quit)    doQuit();
        }

        private void doNewTab(Class<DisTA> source) {

        	Panel jPanel1 = new Panel();
            tabbedPane.addTab("Process "+ Character.toString(autoName).toUpperCase(), jPanel1 );
            getContentPane().add(tabbedPane, BorderLayout.CENTER);
    		
    		int count = tabbedPane.getTabCount();
    		tabbedPane.setSelectedIndex(count-1);
    		Automaton a;
            try {
                a = (Automaton) source.newInstance();
            } catch(Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                return;
            }
            a.setAutoName(autoName++);
            disTA.add(a);
           /* ToolBox toolbox = new ToolBox(canvas);
            canvas.setToolBox(toolbox);
            getContentPane().add(toolbox, BorderLayout.NORTH);
            toolbox.selectButton(toolbox.getStateTool());*/
        	
            // setting state tool as default tool, not setting will not set
            // any tool in default way
            toolbox.selectButton(toolbox.getStateTool());
            jPanel1.getCanvas().setToolBox(toolbox);
            jPanel1.getCanvas().setAutomaton(a);
            jPanel1.getCanvas().exposeAll();
            jPanel1.getCanvas().commitTransaction(true);
            jPanel1.getCanvas().computeSize();
        }

		private void doNew(Class<?> source) {

			int confirm = JOptionPane.showConfirmDialog(null,
                    "Are you sure you want to clear everything for a new project?",
                    "Confirm New",
                    JOptionPane.YES_NO_OPTION);
            if(confirm == JOptionPane.YES_OPTION) {
                curFile = null;
                Automaton a;
                try {
                    a = (Automaton) source.newInstance();
                } catch(Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    return;
                }
                disTA = new ArrayList<Automaton>();
                tabbedPane.removeAll();
                Panel jPanel1 = new Panel();
                autoName = 'a';
                disTA.add(a);
                tabbedPane.add("Process "+ Character.toString(autoName).toUpperCase(), jPanel1 );
                a.setAutoName(autoName++);
                /*ToolBox toolbox = new ToolBox(canvas);
                canvas.setToolBox(toolbox);
                getContentPane().add(toolbox, BorderLayout.NORTH);
                toolbox.selectButton(toolbox.getStateTool());*/
                
                // setting state tool as default tool, not setting will not set
                // any tool in default way
                toolbox.selectButton(toolbox.getStateTool());
                jPanel1.getCanvas().setToolBox(toolbox);
                jPanel1.getCanvas().setAutomaton(a);
                jPanel1.getCanvas().exposeAll();
                jPanel1.getCanvas().commitTransaction(true);
            }
        }

        private void doOpen() {
            int returnVal = chooser.showOpenDialog(null);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                try {
                    openAutomaton(f);
                    getCurrentCanvas().commitTransaction(true);
                } catch(IOException e) {
                    JOptionPane.showMessageDialog(null,
                        "Could not open file" + f.toString()
                        + ": " + e.getMessage());
                }
            }
        }

        private void doSave() {
            int returnVal = chooser.showSaveDialog(null);
            if(returnVal != JFileChooser.APPROVE_OPTION) return;

            File f = chooser.getSelectedFile();
            if(f.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "The file already exists. Do you want to overwrite it?",
                    "Confirm Overwrite",
                    JOptionPane.YES_NO_OPTION);
                if(confirm != JOptionPane.YES_OPTION) return;
            }
            saveFile(f);
            curFile = f;
            computeTitle();
            getCurrentCanvas().commitTransaction(true);
        }

        private void doPrint() {
            PrinterJob job = PrinterJob.getPrinterJob();
            if(job.printDialog() == false) return;
            job.setPageable(new PrintItem());
            try {
                job.print();
            } catch(PrinterException e) {
                JOptionPane.showMessageDialog(null,
                    "Error during printing: " + e.toString());
            }
        }

        public void saveFile(File f) {
            try {
                FileOutputStream fwrite = new FileOutputStream(f);
                GroupedWriter fout = new GroupedWriter(fwrite);
                fout.println("Model Checker, " + Main.FILE_VERSION_NAME);
                for (Automaton automaton : disTA) {
					automaton.print(fout);
				}
                //getCurrentCanvas().getAutomaton().print(fout);
                fout.close();
            } catch(IOException e) {
                JOptionPane.showMessageDialog(null,
                    "Could not open file.");
            }
        }
    }

    private class MenuHelp extends JMenu implements ActionListener {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JMenuItem help;
        private JMenuItem about;

        public MenuHelp() {
            super("Help");
            help = create(this, "Help");
            about = create(this, "About");
        }

        private JMenuItem create(JMenu dest, String title) {
            JMenuItem ret = new JMenuItem(title);
            ret.addActionListener(this);
            dest.add(ret);
            return ret;
        }

        public void actionPerformed(ActionEvent event) {
            Object src = event.getSource();
            if(src == help)       doHelp();
            else if(src == about) doAbout();
        }

        private void doHelp() {
            HelpFrame help = new HelpFrame("com/cburch/autosim/doc/index.html");
            URL index = help.getCurrent();
            help.setTitle("Help: Automaton Simulator");
            help.addContentsItem("Contents", index);
            try {
                help.addContentsItem("About", new URL(index, "about.html"));
            } catch(Exception e) { }
            help.setVisible(true);
        }

        private void doAbout() {
            JOptionPane.showMessageDialog(null,
                    "Model Checker " + Main.VERSION_NAME + ". "
                    + "(c) 2014, Harshada Patil.\n"
                    + "See Help for details.\n");
        }
    }

    private class Listener implements WindowListener, ComponentListener {
        public void windowActivated(WindowEvent e) { }
        public void windowClosed(WindowEvent e) {
            System.exit(0);
        }
        public void windowClosing(WindowEvent e) {
            doQuit();
        }
        public void windowDeactivated(WindowEvent e) { }
        public void windowDeiconified(WindowEvent e) { }
        public void windowIconified(WindowEvent e) { }
        public void windowOpened(WindowEvent e) { }

        public void componentHidden(ComponentEvent e) { }
        public void componentMoved(ComponentEvent e) { }
        public void componentResized(ComponentEvent e) {
        	getCurrentCanvas().computeSize();
        }
        public void componentShown(ComponentEvent e) {
        	getCurrentCanvas().computeSize();
        }
    }

	public static ArrayList<Automaton> getDisTA() {
		return disTA;
	}
}
