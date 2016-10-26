package com.ywangwang.service.message;

import java.util.LinkedList;
import java.util.Queue;

import com.ywangwang.service.message.MoMessage;

public class MoMessageQueue {
	private Queue<MoMessage> messageQueue = new LinkedList<MoMessage>();

	private boolean online = false;
	private int loginKey = 0;

	public MoMessageQueue() {
	}

	public synchronized MoMessage getMoMessage() {
		return messageQueue.poll();
	}

	public synchronized boolean addMoMessage(MoMessage message) {
		return messageQueue.add(message);
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
