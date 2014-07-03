/* Copyright (c) 2006, Carl Burch. License information is located in the
 * com.cburch.autosim.Main source code and at www.cburch.com/proj/autosim/. */

package com.cburch.autosim;

import java.util.Iterator;

class NFA extends Automaton {
    private class NFAState extends State {
        public NFAState() {
            super(NFA.this,getAutoName()+Integer.toString(states.size(), 10));
        }
        public boolean canBeInitial() {
            return true;
        }
    }

    private class NFATransition extends Transition {
        public NFATransition(State src, State dst) {
            super(NFA.this, src, dst);
        }
        public boolean canBeTransit(String what) {
            return true;
        }
    }

    public NFA() {
       //getAlphabet().add(Alphabet.EPSILON);
    }

    public void doPlay() {
        super.doPlay();
    }

    public State createState() {
        return new NFAState();
    }
    public Transition createTransition(State src, State dst) {
        for(Iterator<?> it = getTransitions(); it.hasNext(); ) {
            Transition transition = (Transition) it.next();
            if(transition.getSource() == src
                    && transition.getDest() == dst) {
                return null;
            }
        }
        return new NFATransition(src, dst);
    }
}
