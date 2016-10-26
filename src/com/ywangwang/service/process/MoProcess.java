package com.ywangwang.service.process;

import java.util.Arrays;

import org.json.JSONObject;

import com.ywangwang.service.client.Client;
import com.ywangwang.service.client.ClientPool;
import com.ywangwang.service.client.User;
import com.ywangwang.service.connector.MoConnector;
import com.ywangwang.service.main.Startup;
import com.ywangwang.service.message.MoMessage;
import com.ywangwang.service.pool.MoConnectorPool;
import com.ywangwang.service.pool.MoMessageQueuePool;

public class MoProcess extends Thread {
	private final String TAG = "MoProcess->";
	String msg = null;
	boolean isRun = true;

	@Override
	public void run() {

		System.out.println(TAG + "init Process Sucess...");

		while (true) {
			MoConnector connector = MoConnectorPool.getInstance().getMoConnector();

			if (connector == null) {
				if (Startup.isRun == false) {
					System.out.println(TAG + "Process stop");
					return;
				}
				try {
					Thread.sleep(10);
					continue;
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				}
			} else if (connector.isConnect() == false) {
				connector.close();
				continue;
			} else if (connector.id > 0) {
				if (MoMessageQueuePool.isOnlineById(connector.id) == false) {
					connector.close();
					continue;
				} else if (connector.loginKey != MoMessageQueuePool.getLoginKeyById(connector.id)) {
					MoMessage message = new MoMessage();
					message.cmd = MoMessage.LOGIN_KEY_ERR;
					message.info = "用户已在其他设备登录";
					connector.sendMSG(message.toString());
					connector.close();
					continue;
				}
			}

			try {
				msg = connector.getMSG();

				if (msg == null || msg.length() < 2) {
					if (connector.isTimeout() == true) {
						connector.close();
					} else {
						findMessage(connector);
						MoConnectorPool.getInstance().addMoConnector(connector);
					}
				} else {
					System.out.println(TAG + "id=" + connector.id + ",loginKey=" + connector.loginKey + ",Receive=" + msg);
					process(connector);
				}
				Thread.sleep(1);// 不加延时CPU占用率会很高
			} catch (Exception e) {
				e.printStackTrace();
				connector.close();
			} finally {
				msg = null;
			}

		}
	}

	public void process(MoConnector connector) {
		MoMessage message = null;
		User user = null;
		Client client = null;
		if (msg.trim().equals("ACK")) {
			connector.sendMSG("ACK");
		} else {
			message = MoMessage.analyzeJsonData(msg);
		}
		if (message != null) {
			if (message.cmd == MoMessage.LOGIN) {
				user = User.analyzeJsonData(message.jsonData);
				client = ClientPool.getInstance().getClient(user.username);
				if (client != null) {
					if (client.username.equals(user.username) && client.password.equals(user.password)) {
						message.cmd = MoMessage.LOGIN_SUCCESS;
						if (message.type == MoMessage.TYPE_GXJ) {
							boolean have = false;
							for (int i = 0; i < client.gxjIds.length; i++) {
								if (client.gxjIds[i] == message.id) {
									have = true;
									break;
								}
							}
							if (have == false) {
								long[] newGxjIds = new long[client.gxjIds.length + 1];
								System.arraycopy(client.gxjIds, 0, newGxjIds, 0, client.gxjIds.length);
								newGxjIds[client.gxjIds.length] = message.id;
								client.gxjIds = newGxjIds;
							}
						} else {
							message.id = client.id;
						}
						// MoMessageQueuePool.setLoginKeyById(message.id, message.loginKey);
						// 设置setOnlineById时，loginKey被同时设置。保证设置在线时，loginKey同时设置正确
						MoMessageQueuePool.setOnlineById(message.id, message.loginKey, true);
						connector.loginKey = message.loginKey;
					} else {
						message.cmd = MoMessage.LOGIN_FAIL;
						message.info = "密码错误";
						message.id = 0;
					}
				} else {
					message.cmd = MoMessage.LOGIN_FAIL;
					message.info = "无此用户";
					message.id = 0;
				}
				connector.id = message.id;
				connector.sendMSG(message.toString());
			} else if (message.cmd == MoMessage.LOGOUT) {
				connector.close();
				return;
			} else if (connector.id < 1) {
				message.cmd = MoMessage.Not_LOGGED;
				connector.sendMSG(message.toString());
			} else if (MoMessageQueuePool.getLoginKeyById(connector.id) != message.loginKey) {
				message.cmd = MoMessage.LOGIN_KEY_ERR;
				message.info = "用户已在其他设备登录";
				connector.sendMSG(message.toString());
				connector.close();
				return;
			} else if (message.cmd == MoMessage.SEND_MESSAGE) {
				System.out.println(TAG + "新message " + connector.id + " To " + Arrays.toString(message.toId));
				for (int i = 0; i < message.toId.length; i++) {
					MoMessageQueuePool.addMoMessageById(message.toId[i], message);
				}
			} else if (message.cmd == MoMessage.CONTROL_GXJ) {
			} else if (message.cmd == MoMessage.GET_WATER_INFO) {
			} else if (message.cmd == MoMessage.GET_GXJ) {
				user = User.analyzeJsonData(message.jsonData);
				client = ClientPool.getInstance().getClient(user.username);
				if (client != null) {
					if (client.password.equals(user.password)) {
						message.cmd = MoMessage.GET_GXJ_SUCCESS;
						message.jsonData = new JSONObject(client.toString());
					} else {
						message.cmd = MoMessage.GET_GXJ_FAIL;
						message.info = "密码错误";
					}
				} else {
					message.cmd = MoMessage.GET_GXJ_FAIL;
					message.info = "失败";
				}
				connector.sendMSG(message.toString());
			}
		}
		findMessage(connector);
		MoConnectorPool.getInstance().addMoConnector(connector);
	}

	private void findMessage(MoConnector connector) {
		if (connector.id > 0) {
			MoMessage moMessage = MoMessageQueuePool.getMoMessageById(connector.id);
			if (moMessage != null) {
				if (connector.sendMSG(moMessage.toString()) == false) {
					System.out.println(TAG + connector.id + " message发送失败");
					MoMessageQueuePool.addMoMessageById(connector.id, moMessage);
					connector.close();
					return;
				} else {
					System.out.println(TAG + connector.id + " message发送成功");
				}
			}
		}
	}
}
