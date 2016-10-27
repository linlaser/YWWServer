package com.ywangwang.service.pool;

import java.util.concurrent.ConcurrentHashMap;

import com.ywangwang.service.client.Client;
import com.ywangwang.service.modata.MoData;
import com.ywangwang.service.modata.MoDataQueue;

public class MoDataQueuePool {
	private static MoDataQueuePool dataPool;
	private static ConcurrentHashMap<Long, MoDataQueue> map = new ConcurrentHashMap<Long, MoDataQueue>();

	private MoDataQueuePool() {
	}

	public static MoDataQueuePool getInstance() {
		if (null == dataPool) {
			dataPool = new MoDataQueuePool();
		}
		return dataPool;
	}

	public synchronized MoDataQueue getMoDataQueue(long userId) {
		return map.get(userId);
	}

	public synchronized void addMoDataQueue(long userId, MoDataQueue queue) {
		map.put(userId, queue);
	}

	public static MoData getMoDataById(long userId) {
		MoDataQueue moDataQueue = MoDataQueuePool.getInstance().getMoDataQueue(userId);
		if (moDataQueue == null) {
			return null;
		}
		return moDataQueue.getMoData();
	}

	public static void addMoDataById(long userId, MoData moData) {
		if (MoDataQueuePool.getInstance().getMoDataQueue(userId) == null) {
			MoDataQueue moDataQueue = new MoDataQueue();
			moDataQueue.addMoData((MoData) moData.clone());
			MoDataQueuePool.getInstance().addMoDataQueue(userId, moDataQueue);
		} else {
			MoDataQueuePool.getInstance().getMoDataQueue(userId).addMoData((MoData) moData.clone());
		}
	}

	public static boolean isOnlineById(long userId) {
		if (MoDataQueuePool.getInstance().getMoDataQueue(userId) == null) {
			return Client.OFFLINE;
		}
		if (MoDataQueuePool.getInstance().getMoDataQueue(userId).isOnline()) {
			return Client.ONLINE;
		}
		return Client.OFFLINE;
	}

	public static void setOnlineById(long userId, int loginKey, boolean online) {
		if (MoDataQueuePool.getInstance().getMoDataQueue(userId) == null) {
			MoDataQueue moDataQueue = new MoDataQueue();
			moDataQueue.setOnline(loginKey, online);
			MoDataQueuePool.getInstance().addMoDataQueue(userId, moDataQueue);
		} else {
			MoDataQueuePool.getInstance().getMoDataQueue(userId).setOnline(loginKey, online);
		}
	}

	public static int getLoginKeyById(long userId) {
		if (MoDataQueuePool.getInstance().getMoDataQueue(userId) == null) {
			return -1;
		}
		return MoDataQueuePool.getInstance().getMoDataQueue(userId).getLoginKey();
	}

	public static void setLoginKeyById(long userId, int loginKey) {
		if (MoDataQueuePool.getInstance().getMoDataQueue(userId) == null) {
			MoDataQueue moDataQueue = new MoDataQueue();
			moDataQueue.setLoginKey(loginKey);
			MoDataQueuePool.getInstance().addMoDataQueue(userId, moDataQueue);
		} else {
			MoDataQueuePool.getInstance().getMoDataQueue(userId).setLoginKey(loginKey);
		}
	}
}
