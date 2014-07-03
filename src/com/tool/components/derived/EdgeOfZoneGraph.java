package com.tool.components.derived;

import java.util.ArrayList;
import java.util.Iterator;

import com.tool.components.Edge;
import com.tool.components.State;

public class EdgeOfZoneGraph extends Edge{
	
	ArrayList<State> startState;
	ArrayList<State> endState;
	
	public ArrayList<State> getStartState() {
		return startState;
	}
	public void setStartState(ArrayList<State> startState) {
		this.startState = startState;
	}
	public ArrayList<State> getEndState() {
		return endState;
	}
	public void setEndState(ArrayList<State> endState) {
		this.endState = endState;
	}
	
	public EdgeOfZoneGraph(){
		
	}
	
    public EdgeOfZoneGraph(String action,ArrayList<State> state, ArrayList<State> endState2){
		super(action);
		this.startState = state;
		this.endState = endState2;
	}

	@Override
	public int hashCode() {	
		
		int hash = 7;
		
		Iterator<State> itr1 = startState.iterator();
		Iterator<State> itr2 = endState.iterator();

		while(itr1.hasNext()){
			State s1 = itr1.next();
			hash = 31 * hash + (null == s1 ? 0 : s1.hashCode()); 
		}
		
		while(itr2.hasNext()){
			State s1 = itr2.next();
			hash = 31 * hash + (null == s1 ? 0 : s1.hashCode()); 
		}
		
		hash = 31 * hash + (null == getAction() ? 0 : getAction().hashCode());
		return hash;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		EdgeOfZoneGraph state = (EdgeOfZoneGraph)obj;
		
		if(!state.getAction().equals(getAction()))
			return false;
		
		Iterator<State> itr1 = startState.iterator();
		Iterator<State> itr2 = state.getStartState().iterator();
		
		while(itr1.hasNext() && itr2.hasNext()){
			State s1 = itr1.next();
			State s2 = itr2.next();
			if(!s1.equals(s2))
				return false;
		}
		
		itr1 = endState.iterator();
		itr2 = state.getEndState().iterator();
		
		while(itr1.hasNext() && itr2.hasNext()){
			State s1 = itr1.next();
			State s2 = itr2.next();
			if(!s1.equals(s2))
				return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		String str = startState+", "+getAction()+", "+endState+"\n";
		return str;
	}
	
}
