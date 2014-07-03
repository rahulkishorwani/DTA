package com.tool.components;

import com.tool.zones.Zone;

public class State{
	
	String automatonState;  //name of state
	Zone zone;             //zone
	
	public State() {
	}
	
	public State(String state, Zone zone) {
		this.automatonState = state;
		this.zone = zone;
	}
	
	public String getAutomatonState() {
		return automatonState;
	}
	
	public void setAutomatonState(String automatonState) {
		this.automatonState = automatonState;
	}
	
	public Zone getZone() {
		return zone;
	}
	
	public void setZone(Zone zone) {
		this.zone = zone;
	}

	@Override
	public String toString() {
		String str;
		str = automatonState;//+": "+zone;
		return str;
	}

	@Override
	public boolean equals(Object obj) {
		
		State state1 = (State)obj;
		
		if(state1.getAutomatonState().equals(getAutomatonState()))
			if(state1.getZone().equals(getZone()))
				return true;
		return false;
		
	}

	@Override
	public int hashCode() {
		
		int hash = 7;
        hash = 31 * hash + (null == zone ? 0 : zone.hashCode()); 
        hash = 31 * hash + (null == automatonState ? 0 : automatonState.hashCode());
        return hash; 
	
	}

}
