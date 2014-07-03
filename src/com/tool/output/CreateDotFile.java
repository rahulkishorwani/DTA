package com.tool.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.tool.automata.ZoneGraph;
import com.tool.components.State;
import com.tool.components.derived.EdgeOfZoneGraph;

public class CreateDotFile {

	File directory;
	File file;
	
	public CreateDotFile(){}
	
	public CreateDotFile(ZoneGraph zoneGraph) {
		try {
			 
			String content = "";
 
			directory = new File("/home/harshada/ToolOutput");
			
			if (!directory.exists()) {
				if (directory.mkdir()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
					return;
				}
			}
			
			DateFormat dateFormat = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
			Date date = new Date();

			file = new File(directory.getAbsolutePath()+"/"+dateFormat.format(date)+".dot");
			
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			int i = 0;
			HashMap<ArrayList<State>,Integer> hm = new HashMap<ArrayList<State>,Integer>();
			for (ArrayList<State> state : zoneGraph.getStates())
				hm.put(state, i++);
			
			content += "digraph ZoneGraph {\n";
			content += "\"\" [shape=plaintext];\n";
		
			for (ArrayList<State> startState : zoneGraph.getStartStates())
				content += "\"\" -> "+hm.get(startState)+";\n";
			
			if(zoneGraph.getEdges() != null){
				for (EdgeOfZoneGraph edge : zoneGraph.getEdges())
					content += hm.get(edge.getStartState()) + " -> " + hm.get(edge.getEndState()) + " [label="+ edge.getAction()+"];\n";
			}
			for (ArrayList<State> state : zoneGraph.getStates())
				content += hm.get(state) + " " + "[shape=polygon,sides=4,label=\""+ state +"\"];\n";
			
			content += "}";
			
			bw.write(content);
			bw.close();
 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generatePdf() {
		String command = "dot -Tpdf " + file.getAbsolutePath() + " -o " + file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-3) + "pdf";
		try {
			Runtime.getRuntime().exec(command);
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

}
