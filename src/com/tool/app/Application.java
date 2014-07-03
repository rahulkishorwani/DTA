package com.tool.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.tool.automata.Automaton;
import com.tool.automata.ZoneGraph;
import com.tool.components.Clock;
import com.tool.components.Guard;
import com.tool.components.derived.EdgeOfAutomaton;

public class Application {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		Automaton automaton;
		ArrayList<Automaton> disTA = new ArrayList<Automaton>();
		HashMap<String,Integer> clockNAutomaton = new HashMap<String,Integer>();
		
		Map<String,HashSet<Integer>> actionProcMap = new HashMap<String,HashSet<Integer>>();

		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.println("Automaton count:");
		int count = Integer.parseInt(br.readLine());
		
		for(int c=0; c<count; c++){
			
			automaton = new Automaton();
			System.out.println("Enter the details of"+(c+1)+"th automaton:");
			
			// adding actions
			System.out.println("Enter comma seprated list of actions:");
			String actionList[] = br.readLine().split(",");
			
			Set<String> actions = new HashSet<String>();	
			
			for(int i=0; i<actionList.length; i++){
				actions.add(actionList[i].trim());
			}
			
			// adding states
			System.out.println("Enter comma seprated list of states:");
			String stateList[] = br.readLine().split(",");
			
			Set<String> states = new HashSet<String>();	
			
			for(int i=0; i<stateList.length; i++){
				states.add(stateList[i].trim());
			}
			automaton.setStates(states);
			
			// adding start state
			System.out.println("Enter comma seprated list of start states:");
			String startStateList[] = br.readLine().split(",");
			
			Set<String> startStates = new HashSet<String>();	
			
			for(int i=0; i<startStateList.length; i++){
				startStates.add(startStateList[i].trim());
			}
			automaton.setStartStates(startStates);
			
			// adding clocks
			System.out.println("Enter comma seprated list of clocks:");
			String clockList[] = br.readLine().split(",");
			
			Map<String, Integer> clocks = new HashMap<String,Integer>();	 // clock and zone index mapping
			Map<Integer, Clock> clockMapping = new HashMap<Integer,Clock>();	 // clock to clock data type mapping
			
			Clock clock = new Clock("zero");
			clock.setLowerBound(0);
			clock.setUpperBound(0);
			clockMapping.put(0, clock);
			for(int i=0; i<clockList.length; i++){
				clocks.put(clockList[i].trim(),i+1);
				clockMapping.put(i+1, new Clock(clockList[i].trim()));
				clockNAutomaton.put(clockList[i].trim(), c);
			}
			
			// adding edges
			System.out.println("Enter count of edges:");
			int edgeCount = Integer.parseInt(br.readLine());
			
			for(int i = 0; i < edgeCount; i++){
				System.out.println("Enter details of "+(i + 1)+"th edge(start_state,end_state,action,guard,comma_seprated_list_of clocks_to_reset)");
				String edgeDetails[] = br.readLine().split(",");
				//Zone guard = new Zone(clockList.length);
				String guardString = null;
				if(3 < edgeDetails.length){
					if(edgeDetails[3].trim().length() != 0)
						guardString = edgeDetails[3].trim();
						//constructGuard(edgeDetails[3].trim(),clocks,clockMapping,guard);
					//else
						//guard = null;
				}
				
				Set<String> clocksToReset = new HashSet<String>();
				for(int j = 4; j < edgeDetails.length; j++){
					clocksToReset.add(edgeDetails[j]);
				} 
				
				HashSet<Integer> proc = actionProcMap.get(edgeDetails[2]);
				if(proc == null)
					proc = new HashSet<Integer>();
				proc.add(c);
				actionProcMap.put(edgeDetails[2], proc);
				
				if(clocksToReset.size() == 0)
					automaton.addEdge(edgeDetails[0],edgeDetails[1],edgeDetails[2],guardString,null);
				else
					automaton.addEdge(edgeDetails[0],edgeDetails[1],edgeDetails[2],guardString,clocksToReset);
			}
			
			// setting clocks
			automaton.setClocks(clocks);
			automaton.setClockMapping(clockMapping);
			disTA.add(automaton);
		}
		
		processGuard(disTA,clockNAutomaton);
		// constructing region automaton from automaton
		ZoneGraph zoneGraph = new ZoneGraph();
		zoneGraph.construct(disTA, actionProcMap);
		
		System.out.println(zoneGraph);
		
	}

	private static void processGuard(ArrayList<Automaton> disTA, HashMap<String, Integer> clockNAutomaton) {
		for (Automaton automaton : disTA) {
			Map<String,ArrayList<EdgeOfAutomaton>> adjacencyList = automaton.getAdjacencyList();
			Iterator<Entry<String, ArrayList<EdgeOfAutomaton>>> iter = adjacencyList.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<String, ArrayList<EdgeOfAutomaton>> mEntry = (Map.Entry<String, ArrayList<EdgeOfAutomaton>>) iter.next();
				ArrayList<EdgeOfAutomaton> arrayList= (ArrayList<EdgeOfAutomaton>) mEntry.getValue();
				Iterator<EdgeOfAutomaton> iter1 = arrayList.iterator();
				while(iter1.hasNext()){
					EdgeOfAutomaton edge = (EdgeOfAutomaton)iter1.next();
					constructGuard(edge.getGuardString(), disTA, edge.getGuard(), clockNAutomaton);
				}
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
		    	clock = m.group(1);
		    	val = Integer.parseInt(m.group(2));
		    }
		    Set<Guard> guard = edgeGuard.get(clockNAutomaton.get(clock));
		    if(guard == null)
		    	guard = new HashSet<Guard>();
		    
		    Map<String, Integer> clocks = disTA.get(clockNAutomaton.get(clock)).getClocks();
		    Map<Integer, Clock> clockMapping = disTA.get(clockNAutomaton.get(clock)).getClockMapping();
		    guard.add(new Guard(clocks .get(clock), 0, val, true));
		   
		    edgeGuard.put(clockNAutomaton.get(clock), guard);
		    
		    if(clockMapping.get(clocks.get(clock)).getUpperBound()<val){
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
		    guard.add(new Guard(0, clocks .get(clock), -1 * val, false));
		    guard.add(new Guard(clocks .get(clock), 0, val, false));
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
				System.out.println("Guard "+guardString+" is written incorrectly");
				System.exit(-1);
			}
		}
		else{
			System.out.println("Guard "+guardString+" is written incorrectly");
			System.exit(-1);
		}
		return;
	}
}
