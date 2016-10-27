package com.ywangwang.service.process;

import java.util.Arrays;

import org.json.JSONObject;

import com.ywangwang.service.client.Client;
import com.ywangwang.service.client.ClientPool;
import com.ywangwang.service.client.User;
import com.ywangwang.service.connector.MoConnector;
import com.ywangwang.service.main.Startup;
import com.ywangwang.service.modata.MoData;
import com.ywangwang.service.pool.MoConnectorPool;
import com.ywangwang.service.pool.MoDataQueuePool;

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
				if (MoDataQueuePool.isOnlineById(connector.id) == false) {
					connector.close();
					continue;
				} else if (connector.loginKey != MoDataQueuePool.getLoginKeyById(connector.id)) {
					MoData moData = new MoData();
					moData.setCmd(MoData.LOGIN_KEY_ERR);
					moData.setInfo("用户已在其他设备登录");
					connector.sendMSG(moData.toString());
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
						findData(connector);
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
		MoData moData = null;
		User user = null;
		Client client = null;
		if (msg.trim().equals("ACK")) {
			connector.sendMSG("ACK");
		} else {
			moData = MoData.analyzeJsonData(msg);
		}
		if (moData != null) {
			if (moData.getCmd() == MoData.LOGIN) {
				user = User.analyzeJsonData(moData.getJsonData());
				client = ClientPool.getInstance().getClient(user.username);
				if (client != null) {
					if (client.username.equals(user.username) && client.password.equals(user.password)) {
						moData.setCmd(MoData.LOGIN_SUCCESS);
						if (moData.getType() == MoData.TYPE_GXJ) {
							boolean have = false;
							for (int i = 0; i < client.gxjIds.length; i++) {
								if (client.gxjIds[i] == moData.getId()) {
									have = true;
									break;
								}
							}
							if (have == false) {
								long[] newGxjIds = new long[client.gxjIds.length + 1];
								System.arraycopy(client.gxjIds, 0, newGxjIds, 0, client.gxjIds.length);
								newGxjIds[client.gxjIds.length] = moData.getId();
								client.gxjIds = newGxjIds;
							}
						} else {
							moData.setId(client.id);
						}
						// MoDataQueuePool.setLoginKeyById(moData.id, moData.loginKey);
						// 设置setOnlineById时，loginKey被同时设置。保证设置在线时，loginKey同时设置正确
						MoDataQueuePool.setOnlineById(moData.getId(), moData.getLoginKey(), true);
						connector.loginKey = moData.getLoginKey();
					} else {
						moData.setCmd(MoData.LOGIN_FAIL);
						moData.setInfo("密码错误");
						moData.setId(0);
					}
				} else {
					moData.setCmd(MoData.LOGIN_FAIL);
					moData.setInfo("无此用户");
					moData.setId(0);
				}
				connector.id = moData.getId();
				connector.sendMSG(moData.toString());
			} else if (moData.getCmd() == MoData.LOGOUT) {
				connector.close();
				return;
			} else if (connector.id < 1) {
				moData.setCmd(MoData.Not_LOGGED);
				connector.sendMSG(moData.toString());
			} else if (MoDataQueuePool.getLoginKeyById(connector.id) != moData.getLoginKey()) {
				moData.setCmd(MoData.LOGIN_KEY_ERR);
				moData.setInfo("用户已在其他设备登录");
				connector.sendMSG(moData.toString());
				connector.close();
				return;
			} else if (moData.getCmd() == MoData.SEND_MESSAGE) {
				System.out.println(TAG + "新message " + connector.id + " To " + Arrays.toString(moData.getToId()));
				for (int i = 0; i < moData.getToId().length; i++) {
					MoDataQueuePool.addMoDataById(moData.getToId()[i], moData);
				}
			} else if (moData.getCmd() == MoData.CONTROL_GXJ) {
			} else if (moData.getCmd() == MoData.GET_WATER_INFO) {
			} else if (moData.getCmd() == MoData.GET_GXJ) {
				user = User.analyzeJsonData(moData.getJsonData());
				client = ClientPool.getInstance().getClient(user.username);
				if (client != null) {
					if (client.password.equals(user.password)) {
						moData.setCmd(MoData.GET_GXJ_SUCCESS);
						moData.setJsonData(new JSONObject(client.toString()));
					} else {
						moData.setCmd(MoData.GET_GXJ_FAIL);
						moData.setInfo("密码错误");
					}
				} else {
					moData.setCmd(MoData.GET_GXJ_FAIL);
					moData.setInfo("失败");
				}
				connector.sendMSG(moData.toString());
			}
		}
		findData(connector);
		MoConnectorPool.getInstance().addMoConnector(connector);
	}

	private void findData(MoConnector connector) {
		if (connector.id > 0) {
			MoData moData = MoDataQueuePool.getMoDataById(connector.id);
			if (moData != null) {
				if (connector.sendMSG(moData.toString()) == false) {
					System.out.println(TAG + connector.id + " data发送失败");
					MoDataQueuePool.addMoDataById(connector.id, moData);
					connector.close();
					return;
				} else {
					System.out.println(TAG + connector.id + " data发送成功");
				}
			}
		}
	}
}
