package com.ywangwang.service.pool;

import java.util.LinkedList;
import java.util.Queue;

import com.ywangwang.service.connector.MoConnector;
import com.ywangwang.service.main.Startup;

public class MoConnectorPool {
	private static MoConnectorPool connPool;
	private Queue<MoConnector> connQueue = new LinkedList<MoConnector>();

	private MoConnectorPool() {
	}

	public static MoConnectorPool getInstance() {
		if (null == connPool) {
			connPool = new MoConnectorPool();
		}
		return connPool;
	}

	public synchronized MoConnector getMoConnector() {
		// System.out.println("get connector...");
		return connQueue.poll();
	}

	public synchronized boolean addMoConnector(MoConnector connector) {
		if (Startup.isRun) {
			return connQueue.add(connector);
		} else {
			connector.close();
			return true;
		}
	}
}
