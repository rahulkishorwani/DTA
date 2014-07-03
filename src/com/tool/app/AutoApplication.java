package com.tool.app;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import com.cburch.autosim.Main;
import com.tool.automata.Automaton;
import com.tool.automata.ZoneGraph;
import com.tool.components.Clock;
import com.tool.components.Guard;
import com.tool.components.State;
import com.tool.components.derived.EdgeOfAutomaton;
import com.tool.output.CreateDotFile;

public class AutoApplication {

	/**
	 * Parameters should be passed in following order:(comma separated list)
	 * automaton count,
	 * Details of each automaton:
	 * states, start states, final states, clocks, count of edges, 
	 * edges (<start_state, end_state, action, guard, clocks_to_reset>)
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		ArrayList<Automaton> disTA = new ArrayList<Automaton>();
		HashMap<String,Integer> clockNAutomaton = new HashMap<String,Integer>();
		Map<String, HashSet<Integer>> actionProcMap = new HashMap<String,HashSet<Integer>>();	// action to process mapping
		
		int track = 0;
		int count = Integer.parseInt(args[track++]);			// reading automaton count
		
		// for loop runs for each automaton to parse automaton details
		for(int c=0; c<count; c++){
			Automaton automaton = new Automaton();				// creating instance of Automaton
			
			String stateList[] = args[track++].split(",");		// splitting state list
			
			Set<String> states = new HashSet<String>();	
			
			for(int i=0; i<stateList.length; i++){
				states.add(stateList[i].trim());
			}
			automaton.setStates(states);						// setting automaton states
			
			String startStateList[] = args[track++].split(",");  // splitting start states list
			
			Set<String> startStates = new HashSet<String>();	
			
			for(int i=0; i<startStateList.length; i++){
				startStates.add(startStateList[i].trim());
			}
			automaton.setStartStates(startStates);				// setting automaton start states
			
			String finalStateList[] = args[track++].split(",");  // splitting final states list
						
			Set<String> finalStates = new HashSet<String>();	
						
			for(int i=0; i<finalStateList.length; i++){
				finalStates.add(finalStateList[i].trim());
			}
			automaton.setFinalStates(finalStates);				// setting automaton final states list
			
			String clockList[] = args[track++].split(",");		// splitting clock list
			
			Map<String, Integer> clocks = new HashMap<String,Integer>();	 // clock to zone index mapping
			Map<Integer, Clock> clockMapping = new HashMap<Integer,Clock>();	 // clock to clock data type mapping
			
			Clock clock = new Clock("zero");								// extra clock "Zero" for Zone
			clock.setLowerBound(0);											// setting lower bound for clock "Zero"
			clock.setUpperBound(0);											// setting upper bound for clock "Zero"
			clockMapping.put(0, clock);
			
			for(int i=0; i<clockList.length; i++){
				clocks.put(clockList[i].trim(),i+1);
				clockMapping.put(i+1, new Clock(clockList[i].trim()));		
				clockNAutomaton.put(clockList[i].trim(), c);				// clock and corresponding automaton no.
			}
			
			int edgeCount = Integer.parseInt(args[track++]);				// reading edge count
			
			// for loop is executed for each edge of automaton
			// details of edge: start_state,end_state,action,guard,comma_seprated_list_of clocks_to_reset
			for(int i = 0; i < edgeCount; i++){
				String edgeDetails[] = args[track++].split(",");		// splitting edge details
				String guardString = null;
				if(3 < edgeDetails.length){
					if(edgeDetails[3].trim().length() != 0)
						guardString = edgeDetails[3].trim();			// reading guard
				}
				
				Set<String> clocksToReset = new HashSet<String>();
				for(int j = 4; j < edgeDetails.length; j++)
					clocksToReset.add(edgeDetails[j]);					// adding clocks to reset
				
				String action = edgeDetails[2];
				if(action.endsWith("?") || action.endsWith("!"))
					action = action.substring(0, action.length()-1);
				
				Set<Integer> proc = actionProcMap.get(action);		
				if(proc == null)
					proc = new HashSet<Integer>();
				proc.add(c);
				
				actionProcMap.put(action, (HashSet<Integer>) proc);			// adding action and corresponding processes
				
				if(clocksToReset.size() == 0)
					automaton.addEdge(edgeDetails[0],edgeDetails[1],edgeDetails[2],guardString,null);
				else
					automaton.addEdge(edgeDetails[0],edgeDetails[1],edgeDetails[2],guardString,clocksToReset);
			}
			
			// setting clocks
			automaton.setClocks(clocks);
			automaton.setClockMapping(clockMapping);
			disTA.add(automaton);						// adding automaton/process to distributed process array
		}
		
		processGuard(disTA,clockNAutomaton);			// processing guard
		
		// constructing region automaton from automaton
		ZoneGraph zoneGraph = new ZoneGraph();				
		zoneGraph.construct(disTA,actionProcMap);
		
		// creating dot file for storing zone graph in pdf
		CreateDotFile createDotFile = new CreateDotFile(zoneGraph);
		createDotFile.generatePdf();

		boolean done = false;
		
		if(zoneGraph.isEmpty()){								// if zone graph is empty, show dialog with message
			JOptionPane.showMessageDialog(Main.getMainFrame(),
                    "Zone graph is empty.");
		}
		else{													// if zone graph is not empty, ask for trace of a string
			do{
				String s = (String)JOptionPane.showInputDialog(
		                Main.getMainFrame(),
		                "Zone graph is not empty.\nEnter String: \n",
		                "Get Trace",
		                JOptionPane.PLAIN_MESSAGE,
		                null,
		                null,
		                "");
				if(s == null)
					done = true;

				else if (s.length() > 0) {
					ArrayList<ArrayList<State>> a = zoneGraph.printTrace(s.trim(),disTA);
					if(a == null){
						JOptionPane.showMessageDialog(Main.getMainFrame(),
			                    "Not accepted under existential semantics or\nincorrect input string");
					}
					else{
						String msg = "";
						int n = a.size();
					    for(int i = 0; i < n ; i++)
					      msg += a.get( i ) +" --> ";
						JOptionPane.showMessageDialog(Main.getMainFrame(),			// print trace
			                    msg.substring(0, msg.length()-5));
					}
				}
				else{
					JOptionPane.showMessageDialog(Main.getMainFrame(),
		                    "Please enter string.");
				}
			}while(!done);
		}
	}

	private static void processGuard(ArrayList<Automaton> disTA, HashMap<String, Integer> clockNAutomaton) {
		for (Automaton automaton : disTA) {
			Map<String,ArrayList<EdgeOfAutomaton>> adjacencyListNew = new HashMap<String,ArrayList<EdgeOfAutomaton>>();
			Map<String,ArrayList<EdgeOfAutomaton>> adjacencyList = automaton.getAdjacencyList();			// <state, adjacency list>
			if(adjacencyList == null)			// if adjacency list is empty do nothing
				;
			else{								// if adjacency list is not empty
				Iterator<Entry<String, ArrayList<EdgeOfAutomaton>>> adjacencyListIter = adjacencyList.entrySet().iterator();
				while (adjacencyListIter.hasNext()) {
					Map.Entry<String, ArrayList<EdgeOfAutomaton>> mEntry = (Map.Entry<String, ArrayList<EdgeOfAutomaton>>) adjacencyListIter.next();
					ArrayList<EdgeOfAutomaton> edges= (ArrayList<EdgeOfAutomaton>) mEntry.getValue();
					ArrayList<EdgeOfAutomaton> edgesNewList = new ArrayList<EdgeOfAutomaton>();
					Iterator<EdgeOfAutomaton> edgeIter = edges.iterator();
					while(edgeIter.hasNext()){
						EdgeOfAutomaton edge = (EdgeOfAutomaton)edgeIter.next();
						if(edge.getGuardString()!=null){
							if(edge.getGuard()==null){
								HashMap<Integer, Set<Guard>> guard = new HashMap<Integer, Set<Guard>>();
								edge.setGuard(guard);
							}
							HashMap<Integer, Set<Guard>> guard = edge.getGuard();
							constructGuard(edge.getGuardString(), disTA, guard, clockNAutomaton);     	// construct guard
							edge.setGuard(guard);
						}
						edgesNewList.add(edge);
					}
					adjacencyListNew.put(mEntry.getKey(), edgesNewList);
				}
				automaton.setAdjacencyList(adjacencyListNew);
			}
		}
	}

	private static void constructGuard(String guardString,
			ArrayList<Automaton> disTA, HashMap<Integer, Set<Guard>> edgeGuard, HashMap<String, Integer> clockNAutomaton) {
		
		guardString = guardString.replaceAll(" ", "");
		
		if(guardString.equals("")){
			return;
		}
		
		if(guardString.matches("[a-z]+<\\d+")){								// x < c --->  x-0 < 0
			final Pattern p = Pattern.compile("([a-z]+)<(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock = null;
		    if(m.find()){
		    	clock = m.group(1);							// extract clock name
		    	val = Integer.parseInt(m.group(2));			// extract constant
		    }
		    
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock));	
		    if(guard == null)						// if edge guard is null
		    	guard = new HashSet<Guard>();		// create new instance of hashset of guard
		    
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock)).getClocks();
		    Map<Integer, Clock> clockMapping = disTA.get(clockNAutomaton.get(clock)).getClockMapping();
		    guard.add(new Guard(clocks.get(clock), 0, val, true));			// adding new guard(clock id, clock "zero", constant, is strict less than?)
		   
		    edgeGuard.put(clockNAutomaton.get(clock), guard);
		    
		    if(clockMapping.get(clocks.get(clock)).getUpperBound()<val){		// if current upper bound is less than current value
		    	Clock clockVar = clockMapping.get(clocks.get(clock));
		    	clockVar.setUpperBound(val);
		    	clockMapping.put(clocks.get(clock), clockVar);
		    }
		    disTA.get(clockNAutomaton.get(clock)).setClockMapping(clockMapping);
		    return;
		}
		
		else if(guardString.matches("[a-z]+>\\d+")){								// x > c --->  0-x < -c
			final Pattern p = Pattern.compile("([a-z]+)>(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock = null;
		    if(m.find()){
		    	clock = m.group(1);
		    	val = Integer.parseInt(m.group(2));
		    }
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock)).getClocks();
		    Map<Integer, Clock> clockMapping = disTA.get(clockNAutomaton.get(clock)).getClockMapping();
		    guard.add(new Guard(0, clocks .get(clock), -1 * val, true));
		    edgeGuard.put(clockNAutomaton.get(clock), guard);
		    
		    if(clockMapping.get(clocks.get(clock)).getLowerBound()<val){
		    	Clock clockVar = clockMapping.get(clocks.get(clock));
		    	clockVar.setLowerBound(val);
		    	clockMapping.put(clocks.get(clock), clockVar);
		    }
		    disTA.get(clockNAutomaton.get(clock)).setClockMapping(clockMapping);
		    return;
		}
		
		else if(guardString.matches("[a-z]+=\\d+")){								// x = c ---> x-0 <= c and 0-x <= c
			final Pattern p = Pattern.compile("([a-z]+)=(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock = null;
		    if(m.find()){
		    	clock = m.group(1);
		    	val = Integer.parseInt(m.group(2));
		    }
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock)).getClocks();
		    Map<Integer, Clock> clockMapping = disTA.get(clockNAutomaton.get(clock)).getClockMapping();
		    guard.add(new Guard(0, clocks.get(clock), -1 * val, false));
		    guard.add(new Guard(clocks.get(clock), 0, val, false));
		    edgeGuard.put(clockNAutomaton.get(clock), guard);
		    if(clockMapping.get(clocks.get(clock)).getLowerBound()<val){
		    	Clock clockVar = clockMapping.get(clocks.get(clock));
		    	clockVar.setLowerBound(val);
		    	clockMapping.put(clocks.get(clock), clockVar);
		    }
		    if(clockMapping.get(clocks.get(clock)).getUpperBound()<val){
		    	Clock clockVar = clockMapping.get(clocks.get(clock));
		    	clockVar.setUpperBound(val);
		    	clockMapping.put(clocks.get(clock), clockVar);
		    }
		    disTA.get(clockNAutomaton.get(clock)).setClockMapping(clockMapping);
		    return;
		}
			
		else if(guardString.matches("[a-z]+<=\\d+")){								// x <= c ---> x-0 <= c
			final Pattern p = Pattern.compile("([a-z]+)<=(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock = null;
		    if(m.find()){
		    	clock = m.group(1);
		    	val = Integer.parseInt(m.group(2));
		    }
		    
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock)).getClocks();
		    Map<Integer, Clock> clockMapping = disTA.get(clockNAutomaton.get(clock)).getClockMapping();
		    guard.add(new Guard(clocks .get(clock), 0, val, false));
		    edgeGuard.put(clockNAutomaton.get(clock), guard);
		    if(clockMapping.get(clocks.get(clock)).getUpperBound()<val){
		    	Clock clockVar = clockMapping.get(clocks.get(clock));
		    	clockVar.setUpperBound(val);
		    	clockMapping.put(clocks.get(clock), clockVar);
		    }
		    disTA.get(clockNAutomaton.get(clock)).setClockMapping(clockMapping);
		    return;
		}
		
		else if(guardString.matches("[a-z]+>=\\d+")){								// x >= c ---> 0-x <= -c
			final Pattern p = Pattern.compile("([a-z]+)>=(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock = null;
		    if(m.find()){
		    	clock = m.group(1);
		    	val = Integer.parseInt(m.group(2));
		    }
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock)).getClocks();
		    Map<Integer, Clock> clockMapping = disTA.get(clockNAutomaton.get(clock)).getClockMapping();
		    guard.add(new Guard(0, clocks .get(clock), -1 * val, false));
		    edgeGuard.put(clockNAutomaton.get(clock), guard);
		    if(clockMapping.get(clocks.get(clock)).getLowerBound()<val){
		    	Clock clockVar = clockMapping.get(clocks.get(clock));
		    	clockVar.setLowerBound(val);
		    	clockMapping.put(clocks.get(clock), clockVar);
		    }
		    disTA.get(clockNAutomaton.get(clock)).setClockMapping(clockMapping);
		    return;
		}
		// new..............
		else if(guardString.matches("[a-z]+-[a-z]+<\\d+")){								// x-y < c
			final Pattern p = Pattern.compile("([a-z]+)-([a-z]+)<(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock1 = null;
		    String clock2 = null;
		    if(m.find()){
		    	clock1 = m.group(1);
		    	clock2 = m.group(2);
		    	val = Integer.parseInt(m.group(3));
		    }  
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock1));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock1)).getClocks();
		    guard.add(new Guard(clocks .get(clock1), clocks .get(clock2), val, true));
		    edgeGuard.put(clockNAutomaton.get(clock1), guard);
		    return;
		}
		
		else if(guardString.matches("[a-z]+-[a-z]+<=\\d+")){								// x-y <= c
			final Pattern p = Pattern.compile("([a-z]+)-([a-z]+)>(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock1 = null;
		    String clock2 = null;
		    if(m.find()){
		    	clock1 = m.group(1);
		    	clock2 = m.group(2);
		    	val = Integer.parseInt(m.group(3));
		    }
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock1));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock1)).getClocks();
		    guard.add(new Guard(clocks .get(clock1), clocks .get(clock2), val, false));
		    edgeGuard.put(clockNAutomaton.get(clock1), guard);
		    return;
		}

		else if(guardString.matches("[a-z]+-[a-z]+>\\d+")){								// x-y > c ---> y-x < -c
			final Pattern p = Pattern.compile("([a-z]+)-([a-z]+)>(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock1 = null;
		    String clock2 = null;
		    if(m.find()){
		    	clock1 = m.group(1);
		    	clock2 = m.group(2);
		    	val = Integer.parseInt(m.group(3));
		    }
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock1));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock1)).getClocks();
		    guard.add(new Guard(clocks .get(clock2), clocks .get(clock1), -1 * val, true));
		    edgeGuard.put(clockNAutomaton.get(clock1), guard);
		    return;
		}
		
		else if(guardString.matches("[a-z]+-[a-z]+>=\\d+")){								// x-y >= c ---> y-x <= -c
			final Pattern p = Pattern.compile("([a-z]+)-([a-z]+)>=(\\d+)");
		    final Matcher m = p.matcher(guardString);
		    int val = 0;
		    String clock1 = null;
		    String clock2 = null;
		    if(m.find()){
		    	clock1 = m.group(1);
		    	clock2 = m.group(2);
		    	val = Integer.parseInt(m.group(3));
		    }
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock1));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock1)).getClocks();
		    guard.add(new Guard(clocks .get(clock2), clocks .get(clock1), -1 * val, false));
		    edgeGuard.put(clockNAutomaton.get(clock1), guard);
		    return;
		}
			
		else if(guardString.matches("\\(.+\\)AND\\(.+\\)")){
			Stack<Character> stack = new Stack<Character>();
			for(int i = 0; i < guardString.length(); i++){
				if((stack.size() == 0) && (i != 0)){
					if(guardString.substring(i).startsWith("AND")){
						String guardPart1 = guardString.substring(1, i-1);
						String guardPart2 = guardString.substring(i+4, guardString.length()-1);
						constructGuard(guardPart1, disTA, edgeGuard, clockNAutomaton);
						constructGuard(guardPart2, disTA, edgeGuard, clockNAutomaton);
						return;
					}
				}
				if(guardString.substring(i).startsWith("(")){
					stack.push('(');
				}
				if(guardString.substring(i).startsWith(")")){
					stack.pop();
				}
			}
			if(stack.size() != 0){
	    		JOptionPane.showMessageDialog(Main.getMainFrame(),
	                    "Guard is written Incorrectly",
	                    "Incorrect Input",
	                    JOptionPane.ERROR_MESSAGE);
	    	}
		}
		else{
    		JOptionPane.showMessageDialog(Main.getMainFrame(),
                    "Guard is written Incorrectly",
                    "Incorrect Input",
                    JOptionPane.ERROR_MESSAGE);
    	}
		return;
	}
}
