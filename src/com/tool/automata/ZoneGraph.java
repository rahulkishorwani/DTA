package com.tool.automata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import com.tool.components.Guard;
import com.tool.components.State;
import com.tool.components.derived.EdgeOfAutomaton;
import com.tool.components.derived.EdgeOfZoneGraph;
import com.tool.zones.Zone;

public class ZoneGraph {
	
	private ArrayList<ArrayList<State>> startStates; //start states of individual processes form a set
	private Set<EdgeOfZoneGraph> edges;
	private ArrayList<ArrayList<State>> states;
	private boolean isEmpty = true;
	private HashMap<ArrayList<State>,Set<EdgeOfZoneGraph>> adjList;

	public HashMap<ArrayList<State>,Set<EdgeOfZoneGraph>> getAdjList() {
		return adjList;
	}

	public void setAdjList(HashMap<ArrayList<State>,Set<EdgeOfZoneGraph>> adjList) {
		this.adjList = adjList;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	public ArrayList<ArrayList<State>> getStates() {
		return states;
	}

	public void setStates(ArrayList<ArrayList<State>> states) {
		this.states = states;
	}

	public ArrayList<ArrayList<State>> getStartStates() {
		return startStates;
	}

	public void setStartStates(ArrayList<ArrayList<State>> startStates) {
		this.startStates = startStates;
	}

	public Set<EdgeOfZoneGraph> getEdges() {
		return edges;
	}

	public void setEdges(Set<EdgeOfZoneGraph> edges) {
		this.edges = edges;
	}

	// input:
	// ar1: 1 2 3
	// ar2: a b c d
	// ar3: ! @
	// output: 1 a !, 1 a @, 1 b !,...., 3 d @
	private static ArrayList<ArrayList<State>> compute(
			ArrayList<ArrayList<State>> states, int index) {
		
		if(index == states.size()-1){
			ArrayList<ArrayList<State>> result = new ArrayList<ArrayList<State>>();
			for (State s : states.get(index)) {
				ArrayList<State> r = new ArrayList<State>();
				r.add(s);
				result.add(r);
			}
			return result;
		}
		else{
			ArrayList<ArrayList<State>> retOp = compute(states, index + 1);	
			ArrayList<ArrayList<State>> result = new ArrayList<ArrayList<State>>();
			for(State str: states.get(index)){
				for (ArrayList<State> ar : retOp) {
					ArrayList<State> a = new ArrayList<State>(ar);
					a.add(str);
					result.add(a);
				}
			}	
			return result;
		}
	}
		
	public void construct(ArrayList<Automaton> disTA, Map<String, HashSet<Integer>> actionProcMap) {
		
		// setting start states
		ArrayList<ArrayList<State>> startStatesList = new ArrayList<ArrayList<State>>();
		for (Automaton automaton : disTA) {
			Zone zone = new Zone(automaton.getClockCount());			
			zone.getStartingZone();										// initial zone for start state
			ArrayList<State> startStates = new ArrayList<State>();
			for (String startState : automaton.getStartStates()) {
				startStates.add(new State(startState, zone));
			}
			startStatesList.add(startStates);		// start states list of automaton added to startStatesList
		}
		startStates = compute(startStatesList, 0);		// combinations of start states of process
		for (ArrayList<State> startState : startStates) {
			Collections.reverse(startState);
		}
		
		// setting states
		states = new ArrayList<ArrayList<State>>();			// initialising states var.
		states.addAll(startStates);							// adding start states to states
		
		Queue<ArrayList<State>> passed = new LinkedList<ArrayList<State>>();		// states with processing done
		Queue<ArrayList<State>> waiting = new LinkedList<ArrayList<State>>();		// states need to be processed
		
		for (ArrayList<State> startState : startStates) {				// for each start state of a zone graph
			
			waiting.add(startState);									// add start state to waiting queue
			
			while(waiting.size()!=0){									// while waiting queue is not empty
				
				ArrayList<State> state = waiting.remove();
				if(isFinal(state, disTA)){							// if final state is visited, it means zone graph is non-empty
					isEmpty = false;
				}
				
				boolean isNew = true;
				
				for (ArrayList<State> passedState : passed) {		// check if state is process for the first time by comparing with every passed state
				
					Iterator<State> it1 = state.iterator();
					Iterator<State> it2 = passedState.iterator();
					int c = 0;
					
					while(it1.hasNext() && it2.hasNext()){
						State s1 = it1.next();
						State s2 = it2.next();
						if(s1.getAutomatonState().equals(s2.getAutomatonState())){
							if(s1.getZone().equals(s2.getZone()))				// equals used here because we want un-timed language accepted under existential semantics
								c++;
							else
								break;
						}
						else
							break;
					}
					
					if(c == disTA.size()){							// state is not new, do not process state
						isNew = false;
						break;
					}
				}
				if(isNew){						// for new state
					
					passed.add(state);			// add state to passed state
					
					for(int i=0; i<disTA.size(); i++){
						
						if(disTA.get(i).getAdjacencyList() != null){		// only if automaton contains edges

							ArrayList<EdgeOfAutomaton> adjList = disTA.get(i).getAdjacencyList().get(state.get(i).getAutomatonState()); // edges leaving from state of a process i'th index
							
							if(adjList != null){
								
								boolean isValid = true;
								
								for (EdgeOfAutomaton edgeOfAutomaton : adjList) {		// for every edge in adjList
									
									boolean doneAlready = false;
									boolean flag1 = false;
									boolean flag2 = false;
									
									ArrayList<State> endState = new ArrayList<State>();			// new instance as end state
									HashMap<Integer,Set<Guard>> guard = edgeOfAutomaton.getGuard();
									
									String alpha = edgeOfAutomaton.getAction();
									
									HashMap<Integer,EdgeOfAutomaton> moreEdges = new HashMap<Integer,EdgeOfAutomaton>();
									//Set<String> clocksToReset = edgeOfAutomaton.getClocksToReset();
									Map<Integer,Set<String>> clocksToReset = new HashMap<Integer,Set<String>>();
									
									String action = alpha;
									if(action.endsWith("?") || action.endsWith("!"))
										action = action.substring(0, action.length()-1);
									if(actionProcMap.get(action).size() > 1){
										HashSet<Integer> procSet = actionProcMap.get(action);
										Iterator<Integer> it = procSet.iterator();
										while (it.hasNext()) {
											Integer procId = (Integer) it.next();
											if(procId < i){
												ArrayList<EdgeOfAutomaton> adjList1 = disTA.get(procId).getAdjacencyList().get(state.get(procId).getAutomatonState());
												if(adjList1 != null){
													for (EdgeOfAutomaton edge : adjList1) {
														String action1 = edge.getAction();
														if(action1.endsWith("?") || action1.endsWith("!"))
															action1 = action1.substring(0, action1.length()-1);
														if(action1.equals(action)){
															doneAlready = true;
															break;
														}
													}
												}
											}
											if(doneAlready)
												break;
										}
										
										if(!doneAlready){
											it = procSet.iterator();
											while (it.hasNext()) {
												Integer procId = (Integer) it.next();
												ArrayList<EdgeOfAutomaton> adjList1 = disTA.get(procId).getAdjacencyList().get(state.get(procId).getAutomatonState());
												if(adjList1 != null){
													for (EdgeOfAutomaton edge : adjList1) {
														if(edge.getAction().endsWith("!"))
															flag1 = true;
														if(edge.getAction().endsWith("?"))
															flag2 = true;
														
														String action2 = edge.getAction();
														if(action2.endsWith("?") || action2.endsWith("!"))
															action2 = action2.substring(0, action2.length()-1);
														String action3 = alpha;
														if(action3.endsWith("?") || action3.endsWith("!"))
															action3 = action3.substring(0, action3.length()-1);
														
														if(action2.equals(action3)){
															guard = combineGuards(guard,edge.getGuard(),disTA.size());
															if(edge.getClocksToReset() != null)
																clocksToReset.put(procId,edge.getClocksToReset());
															moreEdges.put(procId,edge);
														}
													}
												}
											}
										}
									}
									else{
										clocksToReset.put(i,edgeOfAutomaton.getClocksToReset());
									}
									
									if(!doneAlready && (flag1 == flag2)){
										for(int j=0; j<state.size(); j++){
											if(i == j){
												Zone zone = state.get(j).getZone();
												if(guard != null && guard.size() != 0){
													if(guard.get(i)!=null){
														zone = zone.and(guard.get(i),disTA.get(j).getClocks());
														if(zone.getZoneDataStruct()[0][0][0]==-1){
															isValid = false;
															break;
														}
													}
												}
												if(clocksToReset.get(j) != null){
													zone = zone.reset(clocksToReset.get(j) , disTA.get(j).getClocks());
												}
												zone = zone.up();
												zone = zone.extraLUPlus(disTA.get(i).getClockMapping());
												State newEndState = new State(edgeOfAutomaton.getEndState(),zone);
												endState.add(newEndState);
											}
											else{
												Zone zone = state.get(j).getZone();
												if(guard!=null){
													if(guard.get(j)!=null){
														zone = zone.and(guard.get(j),disTA.get(j).getClocks());
														if(zone.getZoneDataStruct()[0][0][0]==-1){
															isValid = false;
															break;
														}
														if(clocksToReset.get(j) != null){
															zone = zone.reset(clocksToReset.get(j) , disTA.get(j).getClocks());
														}
														zone = zone.up();
														zone = zone.extraLUPlus(disTA.get(j).getClockMapping());
													}
												}
												State newEndState = null;
												if(moreEdges.containsKey(j))
													newEndState = new State(moreEdges.get(j).getEndState(),zone);
												else
													newEndState = new State(state.get(j).getAutomatonState(),zone);
												endState.add(newEndState);
											}
										}
										
										if(isValid){
											ArrayList<State> endStateNew = superEndState(endState, disTA);
											action = edgeOfAutomaton.getAction();
											if(action.endsWith("?") || action.endsWith("!"))
												action = action.substring(0, action.length()-1);
											EdgeOfZoneGraph e = new EdgeOfZoneGraph(action, state, endStateNew);
											if(edges == null)
												edges = new HashSet<EdgeOfZoneGraph>();
											if(endState.size()==disTA.size()){
												edges.add(e);
												states.add(endStateNew);
												waiting.add(endState);
											}
											if(this.adjList==null)
												this.adjList = new HashMap<ArrayList<State>,Set<EdgeOfZoneGraph>>();
											if(this.adjList.containsKey(e.getStartState())){
												Set<EdgeOfZoneGraph> edges = this.adjList.get(e.getStartState());
												edges.add(e);
												this.adjList.put(state, edges);
											}
											else{
												Set<EdgeOfZoneGraph> edges = new HashSet<EdgeOfZoneGraph>();
												edges.add(e);
												this.adjList.put(state, edges);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private boolean isTranEnabled(ArrayList<Automaton> disTA, HashMap<Integer, Set<Guard>> guard,
			ArrayList<State> state) {
		if(guard!=null){
			for(int i=0;i<disTA.size();i++){
				if(guard.get(i)!=null){
					Zone zone = state.get(i).getZone();
					zone = zone.and(guard.get(i),disTA.get(i).getClocks());
					if(zone.getZoneDataStruct()[0][0][0]==-1)
						return false;
				}
			}
		}
		return true;
	}

	private HashMap<Integer, Set<Guard>> combineGuards(
			HashMap<Integer, Set<Guard>> guard1,
			HashMap<Integer, Set<Guard>> guard2, int size) {
		HashMap<Integer, Set<Guard>> newGuard = new HashMap<Integer, Set<Guard>>();
		for(int i=0; i < size; i++){
			if(guard1 != null && guard1.containsKey(i)){
				if(guard2 != null && guard2.containsKey(i)){
					Set<Guard> g = guard1.get(i);
					g.addAll(guard2.get(i));
					newGuard.put(i, g);
				}
				else{
					newGuard.put(i, guard1.get(i));
				}
			}
			else if(guard2 != null && guard2.containsKey(i)){
				newGuard.put(i, guard2.get(i));
			}
		}
		return newGuard;
	}

	private ArrayList<State> superEndState(ArrayList<State> endState, ArrayList<Automaton> disTA) {
		for (ArrayList<State> state : states) {
			Iterator<State> it1 = state.iterator();
			Iterator<State> it2 = endState.iterator();
			int c = 0;
			while(it1.hasNext() && it2.hasNext()){
				State s1 = it1.next();
				State s2 = it2.next();
				if(s1.getAutomatonState().equals(s2.getAutomatonState())){
					if(s2.getZone().equals(s1.getZone()))
						c++;
					else
						break;
				}
				else
					break;
			}
			if(c == disTA.size()){
				return state;
			}
		}
		return endState;
	}

	private boolean isFinal(ArrayList<State> state, ArrayList<Automaton> disTA) {
		Iterator<State> it1 = state.iterator();
		Iterator<Automaton> it2 = disTA.iterator();
		while(it1.hasNext() && it2.hasNext()){
			if(!it2.next().getFinalStates().contains(it1.next().getAutomatonState()))
				return false;
		}
		return true;
	}

	@Override
	public String toString() {
		String str = "";
		str += "Start states: \n";
		for (ArrayList<State> startState : startStates)
			str += startState + "\n";
		for (EdgeOfZoneGraph edge : edges)
			str += edge + "\n";
		return str;
	}

	public ArrayList<ArrayList<State>> printTrace(String string, ArrayList<Automaton> disTA) {
		String actions[] = string.split(" ");
		ArrayList<ArrayList<State>> states;
		for (ArrayList<State> state : startStates) {
			states = new ArrayList<ArrayList<State>>();
			ArrayList<State> searchState = state;
			states.add(searchState);
			for (String action : actions) {
				boolean matchFound = false;
				if(adjList != null){
					Set<EdgeOfZoneGraph> edges = adjList.get(searchState);
					if(edges!=null){
						Iterator<EdgeOfZoneGraph> it = edges.iterator();
						while(it.hasNext()){
							EdgeOfZoneGraph edge = (EdgeOfZoneGraph)it.next();
							if(edge.getAction().equals(action)){
								states.add(edge.getEndState());
								searchState = edge.getEndState();
								matchFound = true;
								break;
							}
						}
					}
				}
				if(!matchFound){
					return null;
				}
			}
			if(isFinal(states.get(states.size()-1), disTA)){
				return states;
			}
		}
		return null;
	}
}
