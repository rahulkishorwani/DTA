/* Copyright (c) 2006, Carl Burch. License information is located in the
 * com.cburch.autosim.Main source code and at www.cburch.com/proj/autosim/. */

package com.cburch.autosim;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import com.tool.app.AutoApplication;

abstract class Automaton {
    public static final int NUM_FRAMES = 15;
    int stateCount = 0;

    protected LinkedList<AutomatonComponent> states = new LinkedList<AutomatonComponent>();
    protected LinkedList<AutomatonComponent> transitions = new LinkedList<AutomatonComponent>();
    private LinkedList<AutomatonComponent> components = new LinkedList<AutomatonComponent>();
    //private Alphabet alphabet = new Alphabet("abcd" + Alphabet.ELSE);
    protected Set<String> actions = new HashSet<String>();

    private char autoName;
    public char getAutoName() {
		return autoName;
	}

	private StateSet current = new StateSet();
    private StateSet current_draw = current;
    private Canvas canvas = null;
    private Rectangle bounding = null;

   // private LinkedList<?> history = new LinkedList<Object>();
        // for storing StateSets previously stepped through

    public Automaton() {
        
    }

    //
    // ABSTRACT METHODS
    //
    public abstract State createState();
    public abstract Transition createTransition(State src, State dst);

    //
    // ACCESS METHODS
    //
    //public Alphabet getAlphabet() { return alphabet; }
    public Canvas getCanvas() { 
    	if(canvas == null)
    		return new Canvas();
    	return canvas;
    }
    //public List<?> getHistory() { return history; }

    public Iterator<AutomatonComponent> getStates() {
        return states.iterator();
    }
    public StateSet getInitialStates() {
        StateSet ret = new StateSet();
        for(Iterator<AutomatonComponent> it = getStates(); it.hasNext(); ) {
            State state = (State) it.next();
            if(state.isInitial()) ret.add(state);
        }
        return ret;
    }
    public Iterator<AutomatonComponent> getTransitions() {
        return transitions.iterator();
    }
    public Iterator<AutomatonComponent> getComponents() {
        return components.iterator();
    }
    public Iterator<AutomatonComponent> getAllComponents() {
        return Iterators.join(getTransitions(),
            Iterators.join(getStates(), getComponents()));
    }
    public Iterator<AutomatonComponent> getAllComponentsReverse() {
        return Iterators.join(Iterators.reverse(components),
            Iterators.join(Iterators.reverse(states),
                Iterators.reverse(transitions)));
    }

    //
    // CONFIGURATION METHODS
    //
    public void setCanvas(Canvas canvas) { this.canvas = canvas; }

    public void exposeConnections(Graphics g, State what) {
        for(Iterator<AutomatonComponent> it = getTransitions(); it.hasNext(); ) {
            Transition transition = (Transition) it.next();
            if(transition.getSource() == what
                    || transition.getDest() == what) {
                transition.expose(g);
            }
        }
    }

    public AutomatonComponent addComponent(AutomatonComponent what) {
        components.add(what);
        invalidateBounds();
        return what;
    }
    public void removeComponent(AutomatonComponent what) {
        components.remove(what);
    }

    public State addState() {
        State q = createState();
        if(q != null) {
            states.add(q);
            invalidateBounds();
        }
        return q;
    }
    public void removeState(State what) {
    	
        current.remove(what);
        current_draw.remove(what);
        states.remove(what);

        Graphics g = null;
        if(canvas != null) g = canvas.getGraphics();

        LinkedList<Transition> to_remove = new LinkedList<Transition>();
        for(Iterator<AutomatonComponent> it = getTransitions(); it.hasNext(); ) {
            Transition transition = (Transition) it.next();
            if(transition.getSource() == what || transition.getDest() == what) {
                to_remove.add(transition);
            }
        }
        for(Iterator<Transition> it = to_remove.iterator(); it.hasNext(); ) {
            Transition transition = (Transition) it.next();
            if(g != null) transition.expose(g);
            transitions.remove(transition);
        }
    }

    public Transition addTransition(State src, State dst) {
        Transition delta = createTransition(src, dst);
        if(delta != null) {
            transitions.add(delta);
            invalidateBounds();
        }
        return delta;
    }
    public void removeTransition(Transition what) {
        transitions.remove(what);
    }

    public void remove(AutomatonComponent comp) {
        if(comp instanceof State) {
            removeState((State) comp);
        } else if(comp instanceof Transition) {
            removeTransition((Transition) comp);
        } else {
            removeComponent(comp);
        }
    }

    //
    // SIMULATION METHODS
    //
    public void doPlay() {											// important
    	
    	boolean validArgs = true;
    	
    	CustomDialog1 customDialog = new CustomDialog1(Main.getMainFrame(),MainFrame.getDisTA());
        customDialog.pack();
        
        customDialog.setLocationRelativeTo(Main.getMainFrame());
        customDialog.setVisible(true);
        
        ArrayList<String> clocks = customDialog.getValidatedText();
        if(MainFrame.getDisTA().size() > clocks.size())
        	return;
        ArrayList<String> args = new ArrayList<String>();
    	args.add(Integer.toString(MainFrame.getDisTA().size()));
    	
    	for(int i=0; i<MainFrame.getDisTA().size();i++){
    		String stateString = "";
        	String startStates = "";
        	String endStates = "";
        	for(Iterator<AutomatonComponent> it = MainFrame.getDisTA().get(i).getStates(); it.hasNext(); ) {
                State state = (State) it.next();
                stateString += ((State)state).getName()+"," ;
                if(state.isInitial())
                	startStates += ((State)state).getName()+"," ;
                if(state.isFinal())
                	endStates += ((State)state).getName()+"," ;
        	}
        	if(startStates == ""){
        		validArgs = false;
        		break;
        	}
        	if(endStates == ""){
        		validArgs = false;
        		break;
        	}
        	args.add(stateString.substring(0, stateString.length()-1));
        	args.add(startStates.substring(0, startStates.length()-1));
        	if(endStates.length()>0)
        		args.add(endStates.substring(0, endStates.length()-1));
        	
        	args.add(clocks.get(i));
        	
        	int j = 0;
        	
        	Iterator<AutomatonComponent> it = MainFrame.getDisTA().get(i).getTransitions();
        	for ( ; it.hasNext() ; ++j )
        		it.next();
        	
        	args.add(Integer.toString(j));
        	
        	for(it = MainFrame.getDisTA().get(i).getTransitions(); it.hasNext(); ) {
        		Transition tran = (Transition) it.next(); 
        		String edge = tran.getSource().getName()+","+tran.getDest().getName()+",";
        		if(tran.getAlphabet() == ""){
            		validArgs = false;
            		break;
            	}
        		edge += tran.getAlphabet()+",";
        		if(tran.getGuard()!=null)
        			edge += tran.getGuard();
        		edge += ",";
        		if(tran.getClocksToReset()!=null)
        			edge += Arrays.toString(tran.getClocksToReset()).substring(1, Arrays.toString(tran.getClocksToReset()).length()-1);
        		args.add(edge);
        	}
    	}
    	if(!validArgs){
    		JOptionPane.showMessageDialog(Main.getMainFrame(),
                    "Automaton is constructed incorrectly",
                    "Incorrect Input",
                    JOptionPane.ERROR_MESSAGE);
    	}else{
        	try {
    			AutoApplication.main(args.toArray(new String[args.size()]));
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    public void doStop() {
        setCurrent(new StateSet());
    }
    public void doPause() { }
    public void doStep() { }
    public void doResetSimulation() { }

    public StateSet getCurrent() {
        return current;
    }
    public StateSet getCurrentDraw() {
        return current_draw;
    }
    public void setCurrent(StateSet data) {
        if(data == null) data = new StateSet();
        StateSet old_draw = current_draw;
        current = data;
        current_draw = data;

        Graphics g = canvas.getGraphics();
        current_draw.expose(g);
        old_draw.expose(g);
    }

    //
    // GUI METHODS
    //
    public AutomatonComponent find(int x, int y, Graphics g) {
        for(Iterator<AutomatonComponent> it = getAllComponentsReverse(); it.hasNext(); ) {
            AutomatonComponent comp = (AutomatonComponent) it.next();
            if(comp.isIn(x, y, g)) return comp;
        }
        return null;
    }
    public State findState(int x, int y, Graphics g) {
        for(Iterator<AutomatonComponent> it = Iterators.reverse(states); it.hasNext(); ) {
            State state = (State) it.next();
            if(state.isIn(x, y, g)) return state;
        }
        return null;
    }
    public void draw(Graphics g) {
        for(Iterator<AutomatonComponent> it = getAllComponents(); it.hasNext(); ) {
            ((AutomatonComponent) it.next()).draw(g);
        }
    }

    //
    // BOUNDING BOX METHODS
    //
    public Dimension getDimensions(Graphics g) {
        if(bounding == null) computeBoundingBox(g);
        int width = bounding.width;
        if(bounding.x > 0) width = bounding.x + bounding.width;
        int height = bounding.height;
        if(bounding.y > 0) height = bounding.y + bounding.height;
        return new Dimension(width, height);
    }
    public Rectangle getBounds(Graphics g) {
        if(bounding == null) computeBoundingBox(g);
        return new Rectangle(bounding);
    }
    public void invalidateBounds() { bounding = null; }
    private void computeBoundingBox(Graphics g) {
        bounding = null;
        Rectangle box = new Rectangle();
        for(Iterator<AutomatonComponent> it = getAllComponents(); it.hasNext(); ) {
            AutomatonComponent comp = (AutomatonComponent) it.next();
            comp.getBounds(box, g);
            if(bounding == null) {
                bounding = new Rectangle(box);
            } else {
                bounding.add(box);
            }
        }
        if(bounding == null) bounding = new Rectangle();
        bounding.grow(5, 5);
    }

    //
    // FILE METHODS
    //
    public void print(GroupedWriter fout) {
        if(this instanceof DisTA) fout.print("DisTA: "+Character.toString(autoName).toUpperCase());
        else fout.print("??");
        fout.print(" "); fout.beginGroup(); fout.println();
        printAutomaton(fout);
        fout.endGroup(); fout.println();
    }
    private void printAutomaton(GroupedWriter fout) {
      //  fout.print("alphabet ");
      // fout.printlnGroup(alphabet.toString());

        for(Iterator<AutomatonComponent> it = getStates(); it.hasNext(); ) {
            State state = (State) it.next();
            fout.print("state "); fout.beginGroup(); fout.println();
            state.print(fout);
            fout.endGroup(); fout.println();
        }

        for(Iterator<AutomatonComponent> it = getTransitions(); it.hasNext(); ) {
            Transition transition = (Transition) it.next();
            int i = states.indexOf(transition.getSource());
            int j = states.indexOf(transition.getDest());
            fout.print("edge " + i + " " + j + " ");
            fout.beginGroup(); fout.println();
            transition.print(fout);
            fout.endGroup(); fout.println();
        }

        /*for(Iterator<AutomatonComponent> it = getComponents(); it.hasNext(); ) {
            AutomatonComponent comp = (AutomatonComponent) it.next();
            if(comp instanceof AutomatonLabel) {
                fout.print("label "); fout.beginGroup(); fout.println();
                comp.print(fout);
                fout.endGroup(); fout.println();
            }
        }*/

    }
    public static void read(Automaton ret, GroupedReader fin) throws IOException {
    	fin.beginGroup();
        while(!fin.atGroupEnd()) {
            String key = fin.readLine().trim();
            if(key != null && key.length() > 0) {
                if(!ret.setKey(key, fin)) {
                    fin.readGroup();
                }
            }
        }
        fin.endGroup();
    }
    public boolean setKey(String key, GroupedReader fin) throws IOException {

    	if(key.equals("state")) {
            fin.beginGroup();
            State state = addState();
            state.read(fin);
            fin.endGroup();
            return true;
        } else if(key.startsWith("edge ")) {
            StringTokenizer tokens = new StringTokenizer(key);
            try {
                tokens.nextToken();
                int src_i = Integer.parseInt(tokens.nextToken());
                State src = (State) states.get(src_i);
                if(src == null) {
                    throw new IOException("source " + src_i + " not defined");
                }

                int dst_i = Integer.parseInt(tokens.nextToken());
                State dst = (State) states.get(dst_i);
                if(dst == null) {
                    throw new IOException("dest " + dst_i + " not defined");
                }

                Transition transition = addTransition(src, dst);
                fin.beginGroup();
                transition.read(fin);
                fin.endGroup();
                return true;
            } catch(NumberFormatException e) {
                throw new IOException("ill-formatted edge (" + key + ")");
            } catch(IndexOutOfBoundsException e) {
                throw new IOException("ill-formatted edge ("
                    + key + ")");
            }
        } else {
            return false;
        }
    }

	public void setAutoName(char c) {
		autoName = c;
	}
}
