/* Copyright (c) 2006, Carl Burch. License information is located in the
 * com.cburch.autosim.Main source code and at www.cburch.com/proj/autosim/. */

package com.cburch.autosim;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;

class ToolBox extends JToolBar {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static abstract class ToolButton extends JButton
            implements ActionListener, MouseListener, Icon {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private Color std_background;
        private ToolBox toolbox;
        private Tool tool;
        private JPopupMenu popup = null;

        public ToolButton(ToolBox toolbox, Tool tool) {
            super();

            this.toolbox = toolbox;
            this.tool = tool;
            std_background = getBackground();

            setIcon(this);
            addActionListener(this);
            addMouseListener(this);
        }
        public void setSelected(boolean what) {
            setBackground(what ? Color.gray : std_background);
        }

        public Tool getTool() {
            return tool;
        }
        public void actionPerformed(ActionEvent e) {
            toolbox.selectButton(this);
        }
        public void mouseClicked(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
        public void mousePressed(MouseEvent e) {
            if(popup == null) return;
            int mask = InputEvent.BUTTON2_MASK | InputEvent.BUTTON3_MASK;
            if(e.isPopupTrigger() || (e.getModifiers() & mask) != 0) {
                doClick();
                popup.show(this, e.getX(), e.getY());
            }
        }
        public void mouseReleased(MouseEvent e) { }

        public int getIconWidth() { return 20; }
        public int getIconHeight() { return 20; }
        public void paintIcon(Component c, Graphics g, int x, int y) { }
        public void paint(Graphics g) {
            super.paint(g);
            if(popup != null) {
                GraphicsUtil.switchToWidth(g, 1);
                int ht = getHeight();
                int wd = getWidth();
                int[] xp = { wd - 9, wd - 3, wd - 7 };
                int[] yp = { ht - 6, ht - 6, ht - 3 };
                g.setColor(Color.black);
                g.fillPolygon(xp, yp, xp.length);
            }
        }
    }

    private static class ButtonEdge extends ToolButton {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public ButtonEdge(ToolBox toolbox) {							// public ButtonEdge(Canvas canvas, ToolBox toolbox) {
            super(toolbox, new ToolTransition());						// super(toolbox, new ToolTransition(canvas));
            setToolTipText("Add state transitions to automaton");
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            g.setColor(Color.black);
            g.drawLine(x + 3, y + 13, x + 17, y + 7);
            g.setColor(Color.red);
            g.fillOval(x + 1, y + 11, 5, 5);
            g.fillOval(x + 15, y + 5, 5, 5);
            g.setColor(Color.black);
        }
    }

    private static class ButtonState extends ToolButton {
        
		private static final long serialVersionUID = 1L;
		public ButtonState(ToolBox toolbox) {					// public ButtonState(Canvas canvas, ToolBox toolbox) {
            super(toolbox, new ToolState());				// super(toolbox, new ToolState(canvas));
            setToolTipText("Add states to automaton");
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            super.paintIcon(c, g, x, y);
            g.setColor(Color.red);
            g.fillOval(x + 4, y + 4, 12, 12);
            g.setColor(Color.black);
            g.drawOval(x + 4, y + 4, 12, 12);
        }
    }


    private static abstract class SimButton extends JButton
            implements ActionListener, Icon {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		//private Canvas canvas;										uncomment

        public SimButton() {					// public SimButton(Canvas canvas) {
            super();
            //this.canvas = canvas;										uncomment
            setIcon(this);
            addActionListener(this);
        }

       /* public Canvas getCanvas() {									uncomment
            return canvas;
        }*/

        public abstract void select();
        public abstract void paintIcon(Component c, Graphics g, int x, int y);

        public void actionPerformed(ActionEvent e) { select(); }

        public int getIconWidth() { return 20; }
        public int getIconHeight() { return 20; }
    }

    private static class ButtonPlay extends SimButton {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public ButtonPlay() {							// public ButtonPlay(Canvas canvas) {
            //super(canvas);							uncomment
            setToolTipText("Simulate automaton");
        }

        public void select() {
            MainFrame.getCurrentCanvas().getAutomaton().doPlay();
        }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(Color.green);
            int[] xp = { x + 5, x +  5, x + 15 };
            int[] yp = { y + 4, y + 16, y + 10 };
            g.fillPolygon(xp, yp, 3);
            g.setColor(Color.black);
            g.drawPolygon(xp, yp, 3);
        }
    }

    //private Canvas canvas = null;															uncomment
    private ToolButton cur_button = null;

    private ButtonState b_state;

    public ToolBox() {                                                         //  public ToolBox(Canvas canvas) {
    	//this.canvas = canvas;																uncomment
        add(b_state = new ButtonState(this));						   //  add(b_state = new ButtonState(canvas, this));
        add(new ButtonEdge(this));										//  add(new ButtonEdge(canvas, this));
        addSeparator();
        add(new ButtonPlay());											// add(new ButtonPlay(canvas));
    }

    //public void setCanvas(Canvas canvas) { this.canvas = canvas; }                       uncomment

    public ButtonState getStateTool() {
        return b_state;
    }

    public void selectButton(ToolButton what) {
        if(what.getTool() == null) return;
        if(cur_button != null) cur_button.setSelected(false);
        cur_button = what;
        cur_button.setSelected(true);
        if(MainFrame.getCurrentCanvas() != null) {								// if(canvas != null) {
        	MainFrame.getCurrentCanvas().setTool(cur_button.getTool());			// canvas.setTool(cur_button.getTool());
        	MainFrame.getCurrentCanvas().commitTransaction(true);				// canvas.commitTransaction(true);
        }
    }
}
