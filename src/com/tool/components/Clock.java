package com.tool.components;

public class Clock {
	
	String name;     //name of the clock
	int upperBound;  //upper bound of clock
	int lowerBound;  //lower bound of clock

	Clock(){}
	
	public Clock(String name){
		this.name = name;
		upperBound = Integer.MIN_VALUE;
		lowerBound = Integer.MIN_VALUE;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUpperBound() {
		return upperBound;
	}
	public void setUpperBound(int upperBound) {
		this.upperBound = upperBound;
	}
	public int getLowerBound() {
		return lowerBound;
	}
	public void setLowerBound(int lowerBound) {
		this.lowerBound = lowerBound;
	}
	
	public int getMaxBound(){
		if(lowerBound>=upperBound)
			return lowerBound;
		else
			return upperBound;
	}

	@Override
	public String toString() {
		String str = "[ "+getName()+", "+getUpperBound()+", "+getLowerBound()+" ]";
		return str;
	}
	
	
}
