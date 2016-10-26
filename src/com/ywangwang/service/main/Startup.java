package com.ywangwang.service.main;

import com.ywangwang.service.connector.MoConnectorServer;
import com.ywangwang.service.pool.MoProcessPool;

public class Startup {
	static final String TAG = "Startup->";
	public static boolean isRun = true;
	static MoConnectorServer connectorServer = null;

	public static void stop() {
		connectorServer.stopListen();
		isRun = false;
		try {
			Thread.sleep(3000L);
			System.out.println(TAG + "·þÎñ¹Ø±Õ");
			System.exit(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public static void start() {
		connectorServer = new MoConnectorServer(6600);
		connectorServer.start();
		MoProcessPool.getInstance(3).initProcessPoll();
	}

	public static void main(String[] args) {
		if (args.length == 0 || args[0].equalsIgnoreCase("start")) {
			start();
		} else {
			stop();
		}
	}
}
