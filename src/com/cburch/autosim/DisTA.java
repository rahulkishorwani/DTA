/* Copyright (c) 2006, Carl Burch. License information is located in the
 * com.cburch.autosim.Main source code and at www.cburch.com/proj/autosim/. */

package com.cburch.autosim;

import java.util.Iterator;

class DisTA extends Automaton {
    class DFAState extends State {
        public DFAState() {
            super(DisTA.this,getAutoName()+Integer.toString(stateCount));
            stateCount++;
        }
        
		public boolean canBeInitial() {
            return isInitial() || getInitialStates().size() == 0;
        }
    }

    class DFATransition extends Transition {
        public DFATransition(State src, State dst) {
            super(DisTA.this, src, dst);
        }
        public boolean canBeTransit(String what) {
            //if(what == Alphabet.EPSILON) return false;

            for(Iterator it = getTransitions(); it.hasNext(); ) {
                Transition transition = (Transition) it.next();
                if(this != transition
                        && transition.getSource() == this.getSource()
                        && transition.transitsOn(what)) {
                    return false;
                }
            }
            return true;
        }
    }

    public State createState() {
        return new DisTA.DFAState();
    }
    
	public Transition createTransition(State src, State dst) {
        for(Iterator it = getTransitions(); it.hasNext(); ) {
            Transition transition = (Transition) it.next();
            if(transition.getSource() == src
                    && transition.getDest() == dst) {
                return null;
            }
        }
        return new DisTA.DFATransition(src, dst);
    }
    
    public String toString() {
		String str = "Automaton:\nStates=";
		for (AutomatonComponent stateI : states) {
			State state = (State)stateI;
			str += state;
		}
		str += "Transitions=";
		for (AutomatonComponent tranI : transitions) {
			Transition tran = (Transition)tranI;
			str += tran;
		}
		return str;
	}
}
