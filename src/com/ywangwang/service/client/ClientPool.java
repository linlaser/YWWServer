package com.ywangwang.service.client;

import java.util.concurrent.ConcurrentHashMap;

import com.ywangwang.service.pool.MoMessageQueuePool;

public class ClientPool {

	public ClientPool() {
	}

	private static ClientPool clientPool;
	private static ConcurrentHashMap<String, Client> map = new ConcurrentHashMap<String, Client>();

	public static ClientPool getInstance() {
		if (null == clientPool) {
			clientPool = new ClientPool();
		}
		if (map.size() == 0) {
			Client client1 = new Client();
			client1.username = "1";
			client1.password = "1";
			client1.id = 1;
			long[] newGxjIds1 = { 119823800609559L, 36232808102709L };
			client1.gxjIds = newGxjIds1;

			Client client2 = new Client();
			client2.username = "2";
			client2.password = "2";
			client2.id = 2;
			long[] newGxjIds2 = { 36232808102709L, 119823800609559L };
			client2.gxjIds = newGxjIds2;

			Client client3 = new Client();
			client3.username = "3";
			client3.password = "3";
			client3.id = 3;

			Client client123 = new Client();
			client123.username = "123";
			client123.password = "123";
			client123.id = 123;
			long[] newGxjIds123 = { 36232808102709L, 119823800609559L };
			client123.gxjIds = newGxjIds123;

			map.put("1", client1);
			map.put("2", client2);
			map.put("3", client3);
			map.put("123", client123);
		}
		return clientPool;
	}

	public synchronized Client getClient(String username) {
		Client client = map.get(username);
		if (client == null) {
			return null;
		}
		client.gxjOnline = new boolean[client.gxjIds.length];
		for (int i = 0; i < client.gxjIds.length; i++) {
			client.gxjOnline[i] = Client.OFFLINE;
			if (MoMessageQueuePool.getInstance().getMoMessageQueue(client.gxjIds[i]) != null) {
				if (MoMessageQueuePool.getInstance().getMoMessageQueue(client.gxjIds[i]).isOnline()) {
					client.gxjOnline[i] = Client.ONLINE;
				}
			}
		}
		return client;
	}

	public synchronized void addClient(String username, Client client) {
		map.put(username, client);
	}
}
