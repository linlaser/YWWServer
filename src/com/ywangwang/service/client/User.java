package com.ywangwang.service.client;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Cloneable {
	private final static String TAG = "User->";

	public String username = null;
	public String password = null;

	public User() {
	}

	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
			// return new User();
		}
	}

	@Override
	public String toString() {
		return "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
	}

	public JSONObject generateJsonData() {
		try {
			return new JSONObject(toString());
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static User analyzeJsonData(JSONObject jsonObject) {
		try {
			User user = new User();
			user.username = jsonObject.getString("username");
			user.password = jsonObject.getString("password");
			return user;
		} catch (Exception e) {
			System.out.println(TAG + "JSONÊý¾Ý½âÎöÊ§°Ü");
			return null;
		}
	}
}
