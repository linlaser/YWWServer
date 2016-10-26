package com.ywangwang.service.pool;

import java.util.ArrayList;
import java.util.List;

import com.ywangwang.service.process.MoProcess;

public class MoProcessPool {
	private static MoProcessPool processPool;
	private List<MoProcess> processList = new ArrayList<MoProcess>();
	private int maxNum = 0;
	private int currNum = 0;
	private MoProcessPool(){}
	public static MoProcessPool getInstance(int maxNum){
		if(null == processPool){
			processPool = new MoProcessPool();
			processPool.maxNum = maxNum;
		}
		return processPool;
	}
	
	
	public void initProcessPoll(){
		while(currNum < maxNum){
			MoProcess process = new MoProcess();
			process.start();
			processList.add(process);
			currNum ++;
		}
	}
	
	
	public int getMaxNum(){
		return maxNum;
	}
	public int getCurrNum(){
		return currNum;
	}
}
