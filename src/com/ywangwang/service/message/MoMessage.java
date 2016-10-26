package com.ywangwang.service.message;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ywangwang.service.lib.SessionKey;

public class MoMessage implements Cloneable {
	private final static String TAG = "MoMessage->";

	public static final int ACK = 255;

	public static final int TYPE_APP = 0;
	public static final int TYPE_GXJ = 1;

	public static final int LOGIN = 1;
	public static final int LOGOUT = 2;
	public static final int SEND_MESSAGE = 3;
	public static final int CONTROL_GXJ = 4;
	public static final int GET_WATER_INFO = 5;
	public static final int GET_GXJ = 6;

	public static final int LOGIN_SUCCESS = 200;
	public static final int LOGIN_FAIL = 201;
	public static final int Not_LOGGED = 202;
	public static final int GET_GXJ_SUCCESS = 203;
	public static final int GET_GXJ_FAIL = 204;
	public static final int LOGIN_KEY_ERR = 205;

	public int cmd = 0;// 命令
	public int type = 0;// 0=APP,1=管线机
	public long id = 0;// 用户/设备ID
	public long[] toId = new long[0];// 接收用户/设备ID数组
	public int sessionKey = 0;// 会话KEY
	public int loginKey = 0;// 登录会话KEY
	public JSONObject jsonData = new JSONObject();// JSON数据
	public String info = "";

	public long time = 0;// 收到命令的时间
	public int sendFailCounter = 0;// 发送失败计数器

	public MoMessage() {
		sessionKey = new SessionKey().generateNewSessionKey();
	}

	public MoMessage(int cmd) {
		this.cmd = cmd;
		sessionKey = new SessionKey().generateNewSessionKey();
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
			// return new MoMessage();
		}
	}

	@Override
	public String toString() {
		return "{\"MoMessage\":{\"cmd\":" + cmd + ",\"type\":" + type + ",\"id\":" + id + ",\"toId\":" + Arrays.toString(toId) + ",\"info\":\"" + info + "\",\"sessionKey\":" + sessionKey + ",\"loginKey\":" + loginKey + ",\"jsonData\":" + jsonData + "}}";
	}

	public JSONObject generateJsonData() {
		try {
			return new JSONObject(toString());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static MoMessage analyzeJsonData(String jsonString) {
		try {
			JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("MoMessage");
			if (jsonObject.getInt("cmd") > 0) {
				MoMessage moMessage = new MoMessage();
				moMessage.cmd = jsonObject.getInt("cmd");
				moMessage.type = jsonObject.getInt("type");
				moMessage.id = jsonObject.getLong("id");
				JSONArray jsonArray = jsonObject.getJSONArray("toId");
				moMessage.toId = new long[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					moMessage.toId[i] = jsonArray.getLong(i);
				}
				moMessage.info = jsonObject.getString("info");
				moMessage.sessionKey = jsonObject.getInt("sessionKey");
				moMessage.loginKey = jsonObject.getInt("loginKey");
				moMessage.jsonData = jsonObject.getJSONObject("jsonData");
				return moMessage;
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.println(TAG + "JSON数据解析失败");
			return null;
		}
	}
}
