package com.tool.automata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.tool.components.Clock;
import com.tool.components.derived.EdgeOfAutomaton;

public class Automaton {

	Set<String> states;						// set of states
	Set<String> startStates;				// set of start states
	Map<String,Integer> clocks;				// clock and zone index mapping
	Map<Integer,Clock> clockMapping;			// clock to clock data type mapping   //clock to index mapping
	Map<String,ArrayList<EdgeOfAutomaton>> adjacencyList;		// state and adjacency list
	Set<String> finalStates;				// set of final states
	
	public Set<String> getFinalStates() {
		return finalStates;
	}

	public void setFinalStates(Set<String> finalStates) {
		this.finalStates = finalStates;
	}

	public Map<Integer, Clock> getClockMapping() {
		return clockMapping;
	}

	public void setClockMapping(Map<Integer, Clock> clockMapping) {
		this.clockMapping = clockMapping;
	}

	public Set<String> getStates() {
		return states;
	}
	
	public void setStates(Set<String> states) {
		this.states = states;
	}
	
	public Set<String> getStartStates() {
		return startStates;
	}
	
	public void setStartStates(Set<String> startStates) {
		this.startStates = startStates;
	}
	
	public Map<String, ArrayList<EdgeOfAutomaton>> getAdjacencyList() {
		return adjacencyList;
	}

	public void setAdjacencyList(
			Map<String, ArrayList<EdgeOfAutomaton>> adjacencyList) {
		this.adjacencyList = adjacencyList;
	}

	public Map<String, Integer> getClocks() {
		return clocks;
	}
	
	public void setClocks(Map<String, Integer> clocks) {
		this.clocks = clocks;
	}
	
	public int getClockCount(){
		return clocks.size();
	}
	
	public void addEdge(String startState, String endState, String action, String guardString, Set<String> clockToReset){
		EdgeOfAutomaton edge = new EdgeOfAutomaton();
		edge.setStartState(startState);
		edge.setEndState(endState);
		edge.setAction(action);
		edge.setGuardString(guardString);
		edge.setClocksToReset(clockToReset);
		if(adjacencyList == null)					// if adjacency list is empty
			adjacencyList = new HashMap<String,ArrayList<EdgeOfAutomaton>>();
		if(adjacencyList.get(startState) == null){			// if there are no edges for particular state
			ArrayList<EdgeOfAutomaton> edgeList = new ArrayList<EdgeOfAutomaton>();
			edgeList.add(edge);
			adjacencyList.put(startState, edgeList);
		}
		else{
			ArrayList<EdgeOfAutomaton> edgeList = adjacencyList.get(startState);
			edgeList.add(edge);
		}
	}
	
	@Override
	public String toString() {
		String str = "";

		//Set<State> states;
		Iterator<String> itr2 = states.iterator();
		str += "States:\n";
		while(itr2.hasNext())
		{
			String element = itr2.next();
			str += element +"\n";
		}
		
		//State startState;
		str += "Start state: ";
		itr2 = startStates.iterator();
		while(itr2.hasNext())
		{
			String element = itr2.next();
			str += element +"\n";
		}
		
		//Clock clock;
		str += "clocks:\n";
		
		
		Iterator<?> iter = adjacencyList.entrySet().iterator();
		
		str += "Edges:\n";
		while (iter.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry mEntry = (Map.Entry) iter.next();
			str += "for node: " + mEntry.getKey() + "\n";
			@SuppressWarnings("unchecked")
			ArrayList<EdgeOfAutomaton> arrayList= (ArrayList<EdgeOfAutomaton>) mEntry.getValue();
			Iterator<EdgeOfAutomaton> iter1 = arrayList.iterator();
			while(iter1.hasNext()){
				str += (EdgeOfAutomaton)iter1.next() + "\n";
			}
		}
		return str;
	}
}
