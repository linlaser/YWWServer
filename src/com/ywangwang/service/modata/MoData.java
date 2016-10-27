package com.ywangwang.service.modata;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ywangwang.service.lib.SessionKey;

public class MoData implements Cloneable {
	private final static String TAG = "MoData->";

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
	public static final int LOGIN_CONFLICT = 206;

	private int cmd = 0;// 命令
	private int type = 0;// 0=APP,1=管线机
	private long id = 0;// 用户/设备ID
	private long[] toId = new long[0];// 接收用户/设备ID数组
	private int sessionKey = 0;// 会话KEY
	private int loginKey = 0;// 登录会话KEY
	private JSONObject jsonData = new JSONObject();// JSON数据
	private String info = "";

	private long time = 0;// 收到命令的时间
	private int sendFailCount = 0;// 发送失败计数器

	public MoData() {
		sessionKey = new SessionKey().generateNewSessionKey();
	}

	public MoData(int cmd) {
		this.cmd = cmd;
		sessionKey = new SessionKey().generateNewSessionKey();
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
			// return new MoData();
		}
	}

	@Override
	public String toString() {
		return "{\"MoData\":{\"cmd\":" + cmd + ",\"type\":" + type + ",\"id\":" + id + ",\"toId\":" + Arrays.toString(toId) + ",\"info\":\"" + info + "\",\"sessionKey\":" + sessionKey + ",\"loginKey\":" + loginKey + ",\"jsonData\":" + jsonData + "}}";
	}

	public JSONObject generateJsonData() {
		try {
			return new JSONObject(toString());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static MoData analyzeJsonData(String jsonString) {
		try {
			JSONObject jsonObject = new JSONObject(jsonString).getJSONObject("MoData");
			if (jsonObject.getInt("cmd") > 0) {
				MoData moData = new MoData();
				moData.cmd = jsonObject.getInt("cmd");
				moData.type = jsonObject.getInt("type");
				moData.id = jsonObject.getLong("id");
				JSONArray jsonArray = jsonObject.getJSONArray("toId");
				moData.toId = new long[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					moData.toId[i] = jsonArray.getLong(i);
				}
				moData.info = jsonObject.getString("info");
				moData.sessionKey = jsonObject.getInt("sessionKey");
				moData.loginKey = jsonObject.getInt("loginKey");
				moData.jsonData = jsonObject.getJSONObject("jsonData");
				return moData;
			} else {
				return null;
			}
		} catch (Exception e) {
			System.out.println(TAG + "JSON数据解析失败");
			return null;
		}
	}

	public int getCmd() {
		return cmd;
	}

	public void setCmd(int cmd) {
		this.cmd = cmd;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long[] getToId() {
		return toId;
	}

	public void setToId(long[] toId) {
		this.toId = toId;
	}

	public int getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(int sessionKey) {
		this.sessionKey = sessionKey;
	}

	public int getLoginKey() {
		return loginKey;
	}

	public void setLoginKey(int loginKey) {
		this.loginKey = loginKey;
	}

	public JSONObject getJsonData() {
		return jsonData;
	}

	public void setJsonData(JSONObject jsonData) {
		this.jsonData = jsonData;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public int getSendFailCount() {
		return sendFailCount;
	}

	public void setSendFailCount(int sendFailCount) {
		this.sendFailCount = sendFailCount;
	}

}
