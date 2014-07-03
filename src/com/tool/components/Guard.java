package com.tool.components;

public class Guard {
	
	Integer clock1;   //1st clock
	Integer clock2;   //2nd clock
	int value;       //1st - 2nd </<= value
	boolean isStrictLT;   // < -- true     <=------false
	
	Guard(){}
	
	public Guard(Integer clock1, Integer clock2, int value, boolean isStrictLT){
		this.clock1 = clock1;
		this.clock2 = clock2;
		this.value = value;
		this.isStrictLT = isStrictLT;
	}
	
	public Integer getClock1() {
		return clock1;
	}
	public void setClock1(Integer clock1) {
		this.clock1 = clock1;
	}
	public Integer getClock2() {
		return clock2;
	}
	public void setClock2(Integer clock2) {
		this.clock2 = clock2;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public boolean isStrictLT() {
		return isStrictLT;
	}
	public void setStrictLT(boolean isStrictLT) {
		this.isStrictLT = isStrictLT;
	}
	
	@Override
	public String toString() {
		String str = "";
		str = clock1+", "+clock2+", "+value+", "+isStrictLT;
		return str;
	}
}
