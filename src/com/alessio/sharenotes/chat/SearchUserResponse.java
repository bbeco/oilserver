package com.alessio.sharenotes.chat;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

public class SearchUserResponse {
	
	static class User {
		  public String name = null;
		  public String userId = null;
		  
		  public User(String n, String id) {
		   name = n;
		   userId = id;
		  }
		 
	}
	private final int type = MessageTypes.SEARCH_USER_RESPONSE;
	public ArrayList<User> names;
	
	public SearchUserResponse() {
		names = new ArrayList<>();
		// TODO Auto-generated constructor stub
	}
	public SearchUserResponse(String s){
		JSONObject obj = new JSONObject(s);
		names = new ArrayList<>();
		JSONArray array = obj.getJSONArray("names");
		for(int i = 0;i < array.length();i++){
			JSONObject values = array.getJSONObject(i);
			names.add(new User(values.getString("name"),values.getString("userId")));
		}
	}
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("type", Integer.toString(type));
		Collection<JSONObject> values = new ArrayList<JSONObject>(names.size());
		for (User u : names) {
			JSONObject jo = new JSONObject();
			jo.put("name", u.name);
			jo.put("userId", u.userId);
			values.add(jo);
		}
		return obj.toString();
	}

}
