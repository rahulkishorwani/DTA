package com.tool.zones;

import java.util.Map;
import java.util.Set;

import com.tool.components.Clock;
import com.tool.components.Guard;

/**
 * 
 * @author harshada
 * 
 */
public class Zone {
	/**
	 * Constraints 	0 for <
	 * 				1 for <=
	 */
	private int zoneDataStruct[][][];
	private int clockCount;
	
	public Zone(){}
	
	public Zone(int clockCount){
		this.clockCount = clockCount;
		zoneDataStruct = new int[clockCount+1][clockCount+1][2];
		getInitialZone();
	}
	
	public int[][][] getZoneDataStruct() {
		return zoneDataStruct;
	}

	public void setZoneDataStruct(int[][][] zoneDataStruct) {
		this.zoneDataStruct = zoneDataStruct;
	}

	public int getClockCount() {
		return clockCount;
	}

	public void setClockCount(int clockCount) {
		this.clockCount = clockCount;
	}
	
	public void getInitialZone(){
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				if(i==0 || i==j){
					zoneDataStruct[i][j][0]=0;
					zoneDataStruct[i][j][1]=1;
				}
				else{
					zoneDataStruct[i][j][0]=Integer.MAX_VALUE;
					zoneDataStruct[i][j][1]=0;
				}
			}
		}
	}
	
	/**
	 * converts a current zone to canonical zone using floyd-warshall algorithm for
	 * shortest path
	 * @return canonical zone
	 */
	public Zone canonicalZone(){
		Zone zone = new Zone();
		int zoneDataStruct1[][][] = new int[clockCount+1][clockCount+1][2];
		int zoneDataStruct2[][][] = new int[clockCount+1][clockCount+1][2];
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				zoneDataStruct1[i][j][0]=this.zoneDataStruct[i][j][0];
				zoneDataStruct1[i][j][1]=this.zoneDataStruct[i][j][1];
				zoneDataStruct2[i][j][0]=this.zoneDataStruct[i][j][0];
				zoneDataStruct2[i][j][1]=this.zoneDataStruct[i][j][1];
			}
		}
		
		boolean flagOne =false;
		for(int k=0;k<=clockCount;k++){
			for(int i=0;i<=clockCount;i++){
				for(int j=0;j<=clockCount;j++){
					if(i != j){
						if(flagOne){
							if(zoneDataStruct2[i][k][0] == Integer.MAX_VALUE || zoneDataStruct2[k][j][0] == Integer.MAX_VALUE)
								zoneDataStruct1[i][j][0] = zoneDataStruct2[i][j][0];
							else if(zoneDataStruct2[i][k][0] == Integer.MIN_VALUE || zoneDataStruct2[k][j][0] == Integer.MIN_VALUE)
								zoneDataStruct1[i][j][0] = zoneDataStruct2[i][j][0];
							else{
								int sum = zoneDataStruct2[i][k][0]+zoneDataStruct2[k][j][0]; 
								int isLT = zoneDataStruct2[i][k][1]+zoneDataStruct2[k][j][1] == 2 ? 1 : 0;
								if(zoneDataStruct2[i][j][0] == sum){
									if(isLT == 0 || zoneDataStruct2[i][j][1] == 0)
										zoneDataStruct1[i][j][1] = 0;
									else
										zoneDataStruct1[i][j][1] = 1;
									zoneDataStruct1[i][j][0] = sum;
								}
								else if(zoneDataStruct2[i][j][0] < sum){
									if(zoneDataStruct2[i][j][1] == 0)
										zoneDataStruct1[i][j][1] = 0;
									else
										zoneDataStruct1[i][j][1] = 1;
									zoneDataStruct1[i][j][0] = zoneDataStruct2[i][j][0];
								}
								else{
									if(isLT == 0)
										zoneDataStruct1[i][j][1] = 0;
									else
										zoneDataStruct1[i][j][1] = 1;
									zoneDataStruct1[i][j][0] = sum;
								}
							}
						}
						else{
							if(zoneDataStruct1[i][k][0] == Integer.MAX_VALUE || zoneDataStruct1[k][j][0] == Integer.MAX_VALUE)
								zoneDataStruct2[i][j][0] = zoneDataStruct1[i][j][0];
							else if(zoneDataStruct1[i][k][0] == Integer.MIN_VALUE || zoneDataStruct1[k][j][0] == Integer.MIN_VALUE)
								zoneDataStruct2[i][j][0] = zoneDataStruct1[i][j][0];
							else{
								int sum = zoneDataStruct1[i][k][0]+zoneDataStruct1[k][j][0]; 
								int isLT = zoneDataStruct1[i][k][1]+zoneDataStruct1[k][j][1] == 2 ? 1 : 0;
								if(zoneDataStruct1[i][j][0] == sum){
									if(isLT == 0 || zoneDataStruct1[i][j][1] == 0)
										zoneDataStruct2[i][j][1] = 0;
									else
										zoneDataStruct2[i][j][1] = 1;
									zoneDataStruct2[i][j][0] = sum;
								}
								else if(zoneDataStruct1[i][j][0] < sum){
									if(zoneDataStruct1[i][j][1] == 0)
										zoneDataStruct2[i][j][1] = 0;
									else
										zoneDataStruct2[i][j][1] = 1;
									zoneDataStruct2[i][j][0] = zoneDataStruct1[i][j][0];
								}
								else{
									if(isLT == 0)
										zoneDataStruct2[i][j][1] = 0;
									else
										zoneDataStruct2[i][j][1] = 1;
									zoneDataStruct2[i][j][0] = sum;
								}
							}
						}
					}
				}
			}
			if(flagOne)
				flagOne = false;
			else
				flagOne = true;
		}
		if(flagOne){
			zone.setZoneDataStruct(zoneDataStruct2);	
			zone.setClockCount(clockCount);
		}
		else{
			zone.setZoneDataStruct(zoneDataStruct1);
			zone.setClockCount(clockCount);
		}
		return zone;
	}
	/**
	 * Find outs and returns upward closure of zone
	 * @return upward closure of zone
	 */
	public Zone up(){
		Zone zone = new Zone();
		int zoneDataStruct[][][] = new int[clockCount+1][clockCount+1][2];
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				zoneDataStruct[i][j][0]=this.zoneDataStruct[i][j][0];
				zoneDataStruct[i][j][1]=this.zoneDataStruct[i][j][1];
			}
		}
		for(int i=1;i<=clockCount;i++){
			zoneDataStruct[i][0][0] = Integer.MAX_VALUE;
			zoneDataStruct[i][0][1] = 0;
		}
		zone.setZoneDataStruct(zoneDataStruct);
		zone.setClockCount(clockCount);
		return zone;
	}
	/**
	 * 
	 * @param guard
	 * @param clocks 
	 * @return
	 */
	public Zone and(Set<Guard> guard, Map<String, Integer> clocks){
		Zone zone = new Zone();
		int zoneDataStruct[][][] = new int[clockCount+1][clockCount+1][2];
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				zoneDataStruct[i][j][0]=this.zoneDataStruct[i][j][0];
				zoneDataStruct[i][j][1]=this.zoneDataStruct[i][j][1];
			}
		}
		for (Guard constraint : guard) {
			int clock1 = constraint.getClock1();
			int clock2 = constraint.getClock2();
			int val = constraint.getValue();
			boolean isStrictLT = constraint.isStrictLT();
			if(val<zoneDataStruct[clock1][clock2][0]||(val==zoneDataStruct[clock1][clock2][0]) && isStrictLT && zoneDataStruct[clock1][clock2][1]==1){
				zoneDataStruct[clock1][clock2][0] = val;
				zoneDataStruct[clock1][clock2][1] = (isStrictLT ? 0 : 1);
			}
		}
		zone.setZoneDataStruct(zoneDataStruct);
		zone.setClockCount(clockCount);
		zone  = zone.canonicalZone();
		zoneDataStruct = zone.getZoneDataStruct();
		for(int i=0;i<=clockCount;i++){
			for(int j=0; j<=clockCount;j++){
				if(i!=j){
					if(add(zoneDataStruct[j][i][0], zoneDataStruct[i][j][0]) < 0 || (add(zoneDataStruct[j][i][0], zoneDataStruct[i][j][0])  == 0 && zoneDataStruct[j][i][1] + zoneDataStruct[j][i][1] < 2)){
						zoneDataStruct[0][0][0] = -1;
						zoneDataStruct[0][0][1] = 1;
					}
				}
			}
		}
		zone.setZoneDataStruct(zoneDataStruct);
		return zone;
	}
	
	private int add(int i, int j) {
		if((i == Integer.MAX_VALUE) || (j == Integer.MAX_VALUE))
			return Integer.MAX_VALUE;
		return i + j;
	}

	public Zone reset(Set<String> set, Map<String, Integer> clocks){
		Zone zone = new Zone();
		int zoneDataStruct[][][] = new int[clockCount+1][clockCount+1][2];
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				zoneDataStruct[i][j][0]=this.zoneDataStruct[i][j][0];
				zoneDataStruct[i][j][1]=this.zoneDataStruct[i][j][1];
			}
		} 
		for(String v : set){
			int val = clocks.get(v.trim());
			zoneDataStruct[val][0][0]=0;
			zoneDataStruct[0][val][0]=0;
			zoneDataStruct[val][0][1]=1;
			zoneDataStruct[0][val][1]=1;
		}
		
		for(String v : set){
			for(int i=0;i<=clockCount;i++){
				int val = clocks.get(v.trim());
				zoneDataStruct[val][i][0]=zoneDataStruct[0][i][0];
				zoneDataStruct[i][val][0]=zoneDataStruct[i][0][0];
			}
		}
		zone.setZoneDataStruct(zoneDataStruct);
		zone.setClockCount(clockCount);
		return zone;
	}
	/**
	 * Classical extrapolation using M bound
	 * @param clockMapping
	 * @return
	 */
	public Zone extraM(Map<String,Clock> clockMapping){
		Zone zone = new Zone();
		int zoneDataStruct[][][] = new int[clockCount+1][clockCount+1][2];
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				if(this.zoneDataStruct[i][j][0]>clockMapping.get(i).getMaxBound()){
					zoneDataStruct[i][j][0] = Integer.MAX_VALUE;
					zoneDataStruct[i][j][1] = 1;
				}
				else if((-1 * this.zoneDataStruct[i][j][0])>clockMapping.get(j).getMaxBound()){
					zoneDataStruct[i][j][0] = -1 * clockMapping.get(j).getMaxBound();
					zoneDataStruct[i][j][1] = 0;
				}
				else{
					zoneDataStruct[i][j][0]=this.zoneDataStruct[i][j][0];
					zoneDataStruct[i][j][1]=this.zoneDataStruct[i][j][1];
				}
			}
		}
		zone.setZoneDataStruct(zoneDataStruct);
		zone.setClockCount(clockCount);
		return zone;
	}
	/**
	 * Diagonal extrapolation using M bound
	 * @param clockMapping
	 * @return
	 */
	public Zone extraMPlus(Map<String,Clock> clockMapping){
		Zone zone = new Zone();
		int zoneDataStruct[][][] = new int[clockCount+1][clockCount+1][2];
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				if(this.zoneDataStruct[i][j][0]>clockMapping.get(i).getMaxBound()){
					zoneDataStruct[i][j][0] = Integer.MAX_VALUE;
					zoneDataStruct[i][j][1] = 1;
				}
				else if((-1 * this.zoneDataStruct[0][i][0])>clockMapping.get(i).getMaxBound()){
					zoneDataStruct[i][j][0] = Integer.MAX_VALUE;
					zoneDataStruct[i][j][1] = 1;
				}
				else if((-1 * this.zoneDataStruct[0][j][0])>clockMapping.get(j).getMaxBound() && (i != 0)){
					zoneDataStruct[i][j][0] = Integer.MAX_VALUE;
					zoneDataStruct[i][j][1] = 1;
				}
				else if((-1 * this.zoneDataStruct[i][j][0])>clockMapping.get(j).getMaxBound() && (i == 0)){
					zoneDataStruct[i][j][0] = -1 * clockMapping.get(j).getMaxBound();
					zoneDataStruct[i][j][1] = 0;
				}
				else{
					zoneDataStruct[i][j][0]=this.zoneDataStruct[i][j][0];
					zoneDataStruct[i][j][1]=this.zoneDataStruct[i][j][1];
				}
			}
		}
		zone.setZoneDataStruct(zoneDataStruct);
		zone.setClockCount(clockCount);
		return zone;
	}
	/**
	 * Classical extrapolation using LU bounds
	 * @param clockMapping
	 * @return
	 */
	public Zone extraLU(Map<String,Clock> clockMapping){
		Zone zone = new Zone();
		int zoneDataStruct[][][] = new int[clockCount+1][clockCount+1][2];
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				if(this.zoneDataStruct[i][j][0]>clockMapping.get(i).getLowerBound()){
					zoneDataStruct[i][j][0] = Integer.MAX_VALUE;
					zoneDataStruct[i][j][1] = 1;
				}
				else if((-1 * this.zoneDataStruct[i][j][0])>clockMapping.get(j).getUpperBound()){
					zoneDataStruct[i][j][0] = -1 * clockMapping.get(j).getUpperBound();
					zoneDataStruct[i][j][1] = 0;
				}
				else{
					zoneDataStruct[i][j][0]=this.zoneDataStruct[i][j][0];
					zoneDataStruct[i][j][1]=this.zoneDataStruct[i][j][1];
				}
			}
		}
		zone.setZoneDataStruct(zoneDataStruct);
		zone.setClockCount(clockCount);
		return zone;
	}
	/**
	 * Diagonal extrapolation using LU bounds
	 * @param map
	 * @return
	 */
	public Zone extraLUPlus(Map<Integer, Clock> map){
		
		Zone zone = new Zone();
		int zoneDataStruct[][][] = new int[clockCount+1][clockCount+1][2];
		/*for (int j = 1; j <= clockCount; ++j)
	    {
	        if ((-1 * this.zoneDataStruct[0][j][0])>map.get(j).getUpperBound())
	        {
	        	if(map.get(j).getUpperBound()>=0){
	        		zoneDataStruct[0][j][0] = -1 * map.get(j).getUpperBound();
					zoneDataStruct[0][j][1] = 0;
	        	}
	        	else{
	        		zoneDataStruct[0][j][0] = 0;
					zoneDataStruct[0][j][1] = 1;
	        	}
	        }
	    }*/
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				if(i != j){
					if(this.zoneDataStruct[i][j][0]>map.get(i).getLowerBound()){
						if(i != 0){
							zoneDataStruct[i][j][0] = Integer.MAX_VALUE;
							zoneDataStruct[i][j][1] = 0;
						}
						else{
							zoneDataStruct[i][j][0] = 0;
							zoneDataStruct[i][j][1] = 0;
						}
					}
					else if((-1 * this.zoneDataStruct[0][i][0])>map.get(i).getLowerBound()){
						if(i != 0){
							zoneDataStruct[i][j][0] = Integer.MAX_VALUE;
							zoneDataStruct[i][j][1] = 0;
						}
						else{
							zoneDataStruct[i][j][0] = 0;
							zoneDataStruct[i][j][1] = 0;
						}
					}
					else if((-1 * this.zoneDataStruct[0][j][0])>map.get(j).getUpperBound() && (i != 0)){
						if(i != 0){
							zoneDataStruct[i][j][0] = Integer.MAX_VALUE;
							zoneDataStruct[i][j][1] = 0;
						}
						else{
							zoneDataStruct[i][j][0] = 0;
							zoneDataStruct[i][j][1] = 0;
						}
					}
					else if((-1 * this.zoneDataStruct[0][j][0])>map.get(j).getUpperBound() && (i == 0)){
						if(map.get(j).getUpperBound() == Integer.MIN_VALUE){
							zoneDataStruct[i][j][0] = 0;
							zoneDataStruct[i][j][1] = 0;
						}
						else if(map.get(j).getUpperBound() == Integer.MAX_VALUE){
							zoneDataStruct[i][j][0] = Integer.MIN_VALUE;
						}
						else{
							zoneDataStruct[i][j][0] = -1 * map.get(j).getUpperBound();
							zoneDataStruct[i][j][1] = 0;
						}
					}
					else{
						zoneDataStruct[i][j][0]=this.zoneDataStruct[i][j][0];
						zoneDataStruct[i][j][1]=this.zoneDataStruct[i][j][1];
					}
				}
				else{
					zoneDataStruct[i][j][0]=0;
					zoneDataStruct[i][j][1]=1;
				}
			}
		}
		zone.setZoneDataStruct(zoneDataStruct);
		zone.setClockCount(clockCount);
		return zone;
	}
	
	/**
	 * Inclusion test of zones
	 * @param z : A current zone is included in zone z is checked
	 * @return
	 */
	public boolean inclusion(Zone z){
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				if(zoneDataStruct[i][j][0]>z.getZoneDataStruct()[i][j][0])			// clock difference is greater
					return false;
				if(zoneDataStruct[i][j][0]==z.getZoneDataStruct()[i][j][0]){			// clock difference is equal but bound is non stricter
					if(zoneDataStruct[i][j][1]>z.getZoneDataStruct()[i][j][1])
						return false;
				}
					
			}
		}
		return true;			// otherwise
	}

	@Override
	public String toString() {
		String string = "";
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				if(zoneDataStruct[i][j][0]==Integer.MAX_VALUE)
					string += "(inf,";
				else if(zoneDataStruct[i][j][0]==Integer.MIN_VALUE)
					string += "(-inf,";
				else
					string += "("+zoneDataStruct[i][j][0]+",";
				if(zoneDataStruct[i][j][1] == 0)
					string += "<) ";
				else
					string += "<=) ";
			}
			string += "\n";
		}
		return string;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		for(int i=0; i<zoneDataStruct.length;i++){
			for(int j=0; j<zoneDataStruct.length;j++){
				hash = 31 * hash + (zoneDataStruct[i][j][0] == Integer.MAX_VALUE ? 0 : zoneDataStruct[i][j][0]);
				hash = 31 * hash + (zoneDataStruct[i][j][1] == Integer.MAX_VALUE ? 0 : zoneDataStruct[i][j][1]); 
			}
		}
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		Zone zone = (Zone)obj;
		//if(this.inclusion(zone)||zone.inclusion(this))
			//return true;
		for(int i=0; i<zoneDataStruct.length;i++){
			for(int j=0; j<zoneDataStruct.length;j++){
				if(zoneDataStruct[i][j][0]!=zone.getZoneDataStruct()[i][j][0])
					return false;
				if(zoneDataStruct[i][j][1]!=zone.getZoneDataStruct()[i][j][1])
					return false;
			}
		}
		return true;
	}

	public void getStartingZone() {
		for(int i=0;i<=clockCount;i++){
			for(int j=0;j<=clockCount;j++){
				if((j != i) && (i != 0) && (j != 0)){
					zoneDataStruct[i][j][0] = 0;
					zoneDataStruct[i][j][1] = 1;
				}
			}
		}
	}
}