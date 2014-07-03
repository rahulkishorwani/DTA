package com.tool.components.derived;

import java.util.HashMap;
import java.util.Set;

import com.tool.components.Edge;
import com.tool.components.Guard;

public class EdgeOfAutomaton extends Edge{
	
	

	String startState;    //starting point of edge
	String endState;     //end point of the edge
	Set<String> clocksToReset;   //set of clocks(in string set) to reset 
	String guardString;     //conjunction of guards in the form of string
	HashMap<Integer,Set<Guard>> guard;    
	
	public String getGuardString() {
		return guardString;
	}

	public void setGuardString(String guardString) {
		this.guardString = guardString;
	}
	
	public HashMap<Integer, Set<Guard>> getGuard() {
		return guard;
	}

	public void setGuard(HashMap<Integer, Set<Guard>> guard) {
		this.guard = guard;
	}

	public String getStartState() {
		return startState;
	}
	
	public void setStartState(String startState) {
		this.startState = startState;
	}
	
	public String getEndState() {
		return endState;
	}
	
	public void setEndState(String endState) {
		this.endState = endState;
	}
	
	public Set<String> getClocksToReset() {
		return clocksToReset;
	}
	
	public void setClocksToReset(Set<String> clocksToReset) {
		this.clocksToReset = clocksToReset;
	}

	@Override
	public String toString() {
		
		String str = "";
		str += "Edge: ";
		//State startState;
		str += startState + " ";
		//State endState;
		str += endState + " ";
		str += getAction() + " ";
		str += getGuard() + " ";
		//Clock clockToReset;
		str += clocksToReset;
		//BinOpNode guard;
		return str;
	}
	
}
