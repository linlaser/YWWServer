package com.ywangwang.service.client;

import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {
	private final static String TAG = "Client->";

	public static final boolean OFFLINE = false;
	public static final boolean ONLINE = true;

	public String username = "";
	public String password = "";
	public long id = 0;
	public boolean online = false;
	public long[] gxjIds = new long[0];
	public boolean[] gxjOnline = new boolean[0];

	public Client() {
	}

	@Override
	public String toString() {
		return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"id\":" + id + ",\"online\":" + online + ",\"gxjIds\":" + Arrays.toString(gxjIds) + ",\"gxjOnline\":" + Arrays.toString(gxjOnline) + "}";
	}

	public JSONObject generateJsonData() {
		try {
			return new JSONObject(toString());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Client analyzeJsonData(JSONObject jsonObject) {
		try {
			Client client = new Client();
			client.username = jsonObject.getString("username");
			client.password = jsonObject.getString("password");
			client.id = jsonObject.getLong("id");
			client.online = jsonObject.getBoolean("online");
			JSONArray jsonArray = jsonObject.getJSONArray("gxjIds");
			client.gxjIds = new long[jsonArray.length()];
			for (int i = 0; i < jsonArray.length(); i++) {
				client.gxjIds[i] = jsonArray.getLong(i);
			}
			JSONArray jsonArray2 = jsonObject.getJSONArray("gxjOnline");
			client.gxjOnline = new boolean[jsonArray2.length()];
			for (int i = 0; i < jsonArray2.length(); i++) {
				client.gxjOnline[i] = jsonArray2.getBoolean(i);
			}
			return client;
		} catch (Exception e) {
			System.out.println(TAG + "JSONÊý¾Ý½âÎöÊ§°Ü");
			return null;
		}
	}
}
