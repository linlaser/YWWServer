package com.ywangwang.service.connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.ywangwang.service.pool.MoMessageQueuePool;

public class MoConnector {
	private final static String TAG = "MoConnector->";
	boolean isTimeout = false;
	boolean enableTimeout = false;
	int timeoutTime = 50 * 1000;

	public long id = 0;
	public int loginKey = 0;

	Socket socket;
	long lastTime = 0;
	BufferedReader input;
	BufferedWriter output;
	int buffSize;

	public MoConnector(Socket socket, int buffSize) {
		this.socket = socket;
		this.buffSize = buffSize;
		try {
			socket.setSoTimeout(timeoutTime);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
			output = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
			lastTime = System.currentTimeMillis();
			isTimeout = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(TAG + "Connect Success,IP=" + socket.getInetAddress() + " PORT=" + socket.getPort());
	}

	public String getMSG() {
		boolean ready = false;
		String msg = null;
		try {
			ready = input.ready();
			if (ready) {
				msg = input.readLine();
			}

			if (ready) {
				lastTime = System.currentTimeMillis();
			} else if (enableTimeout) {
				if ((System.currentTimeMillis() - lastTime) > timeoutTime) {
					System.out.println(TAG + "Timeout");
					isTimeout = true;
				}
			}
		} catch (SocketTimeoutException e) {
			isTimeout = true;
			System.out.println(TAG + "SocketTimeoutException");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(TAG + "IOException");
		}
		return msg;
	}

	public boolean sendMSG(String msg) {
		try {
			System.out.println(TAG + "Send=" + msg);
			output.write(msg);
			output.write(10);
			output.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void close() {
		if (id > 0) {
			MoMessageQueuePool.setOnlineById(id, loginKey, false);
			System.out.println(TAG + "setOffLine->id=" + id);
		}
		System.out.println(TAG + "id=" + id + ",close,loginKey=" + loginKey);
		try {
			if (output != null) {
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			// 如果服务器端网络断线，先停input流，线程会卡死在这里
			if (input != null) {
				input.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		input = null;
		output = null;
		socket = null;
	}

	public boolean isConnect() {
		if (socket == null) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isTimeout() {
		return isTimeout;
	}

	public void setEnableTimeout(Boolean enable) {
		enableTimeout = enable;
	}

	public void setEnableTimeout(Boolean enable, int timeoutTime) {
		enableTimeout = enable;
		this.timeoutTime = timeoutTime;
	}

	public long getTimeoutTime() {
		return timeoutTime;
	}

	public void setTimeoutTime(int timeoutTime) {
		this.timeoutTime = timeoutTime;
	}
}
