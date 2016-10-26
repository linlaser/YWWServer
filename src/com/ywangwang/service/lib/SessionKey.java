package com.ywangwang.service.lib;

import java.util.Random;

public class SessionKey {
	private int sessionKey = 0;

	public SessionKey() {
	}

	public synchronized void cleanSessionKey() {
		sessionKey = 0;
	}

	public synchronized int getSessionKey() {
		return sessionKey;
	}

	public int generateNewSessionKey() {
		return generateNewSessionKey(100000);
	}

	public synchronized int generateNewSessionKey(int scope) {
		sessionKey = new Random().nextInt(scope) + 1;
		return sessionKey;
	}

	public synchronized int setNewSessionKey(int sessionKey) {
		this.sessionKey = sessionKey;
		return this.sessionKey;
	}
}
