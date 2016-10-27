package com.ywangwang.service.modata;

import java.util.LinkedList;
import java.util.Queue;

public class MoDataQueue {
	private Queue<MoData> dataQueue = new LinkedList<MoData>();

	private boolean online = false;
	private int loginKey = 0;

	public MoDataQueue() {
	}

	public synchronized MoData getMoData() {
		return dataQueue.poll();
	}

	public synchronized boolean addMoData(MoData moData) {
		return dataQueue.add(moData);
	}

	public boolean isOnline() {
		return online;
	}

	public synchronized boolean setOnline(int loginKey, boolean online) {
		if (this.loginKey == loginKey) {
			this.online = online;
		} else if (online == true) {
			this.loginKey = loginKey;
			this.online = online;
		}
		return this.online;
	}

	public int getLoginKey() {
		return loginKey;
	}

	public void setLoginKey(int loginKey) {
		this.loginKey = loginKey;
	}
}
