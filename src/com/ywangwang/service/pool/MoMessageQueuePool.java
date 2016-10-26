package com.ywangwang.service.pool;

import java.util.concurrent.ConcurrentHashMap;

import com.ywangwang.service.client.Client;
import com.ywangwang.service.message.MoMessage;
import com.ywangwang.service.message.MoMessageQueue;

public class MoMessageQueuePool {
	private static MoMessageQueuePool messagePool;
	private static ConcurrentHashMap<Long, MoMessageQueue> map = new ConcurrentHashMap<Long, MoMessageQueue>();

	private MoMessageQueuePool() {
	}

	public static MoMessageQueuePool getInstance() {
		if (null == messagePool) {
			messagePool = new MoMessageQueuePool();
		}
		return messagePool;
	}

	public synchronized MoMessageQueue getMoMessageQueue(long userId) {
		return map.get(userId);
	}

	public synchronized void addMoMessageQueue(long userId, MoMessageQueue queue) {
		map.put(userId, queue);
	}

	public static MoMessage getMoMessageById(long userId) {
		MoMessageQueue moMessageQueue = MoMessageQueuePool.getInstance().getMoMessageQueue(userId);
		if (moMessageQueue == null) {
			return null;
		}
		return moMessageQueue.getMoMessage();
	}

	public static void addMoMessageById(long userId, MoMessage message) {
		if (MoMessageQueuePool.getInstance().getMoMessageQueue(userId) == null) {
			MoMessageQueue moMessageQueue = new MoMessageQueue();
			moMessageQueue.addMoMessage((MoMessage) message.clone());
			MoMessageQueuePool.getInstance().addMoMessageQueue(userId, moMessageQueue);
		} else {
			MoMessageQueuePool.getInstance().getMoMessageQueue(userId).addMoMessage((MoMessage) message.clone());
		}
	}

	public static boolean isOnlineById(long userId) {
		if (MoMessageQueuePool.getInstance().getMoMessageQueue(userId) == null) {
			return Client.OFFLINE;
		}
		if (MoMessageQueuePool.getInstance().getMoMessageQueue(userId).isOnline()) {
			return Client.ONLINE;
		}
		return Client.OFFLINE;
	}

	public static void setOnlineById(long userId, int loginKey, boolean online) {
		if (MoMessageQueuePool.getInstance().getMoMessageQueue(userId) == null) {
			MoMessageQueue moMessageQueue = new MoMessageQueue();
			moMessageQueue.setOnline(loginKey, online);
			MoMessageQueuePool.getInstance().addMoMessageQueue(userId, moMessageQueue);
		} else {
			MoMessageQueuePool.getInstance().getMoMessageQueue(userId).setOnline(loginKey, online);
		}
	}

	public static int getLoginKeyById(long userId) {
		if (MoMessageQueuePool.getInstance().getMoMessageQueue(userId) == null) {
			return -1;
		}
		return MoMessageQueuePool.getInstance().getMoMessageQueue(userId).getLoginKey();
	}

	public static void setLoginKeyById(long userId, int loginKey) {
		if (MoMessageQueuePool.getInstance().getMoMessageQueue(userId) == null) {
			MoMessageQueue moMessageQueue = new MoMessageQueue();
			moMessageQueue.setLoginKey(loginKey);
			MoMessageQueuePool.getInstance().addMoMessageQueue(userId, moMessageQueue);
		} else {
			MoMessageQueuePool.getInstance().getMoMessageQueue(userId).setLoginKey(loginKey);
		}
	}
}
