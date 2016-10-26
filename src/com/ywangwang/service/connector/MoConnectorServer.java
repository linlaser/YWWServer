package com.ywangwang.service.connector;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.ywangwang.service.pool.MoConnectorPool;

public class MoConnectorServer extends Thread {
	private final String TAG = "MoConnectorServer->";
	ServerSocket serverSocket;
	int port = 6600;
	boolean isRun = true;

	public MoConnectorServer() {
	}

	public MoConnectorServer(int port) {
		this.port = port;
	}

	public void stopListen() {
		isRun = false;
	}

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("start ConnectorServer sucess....");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		while (isRun) {
			Socket socket = null;
			MoConnector moConnector = null;
			try {
				serverSocket.setSoTimeout(2900);
				socket = serverSocket.accept();
				if (isRun && socket != null) {
					moConnector = new MoConnector(socket, 2048);
					moConnector.setEnableTimeout(true);
					MoConnectorPool.getInstance().addMoConnector(moConnector);
				}
			} catch (SocketTimeoutException e) {
				// System.out.println(TAG + "等待连接超时");
			} catch (IOException e) {
				e.printStackTrace();
				// continue;
			}
			if (isRun == false) {
				System.out.println(TAG + "ConnectorServer stop");
			}
		}
	}
}
