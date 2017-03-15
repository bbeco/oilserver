package com.alessio.sharenotes.chat;

import org.json.JSONObject;

public class SearchUserRequest {
	
	private final int type = MessageTypes.SEARCH_USER_REQUEST;
	public String name;
	public String sender;
	
	public SearchUserRequest (){};
	
	public SearchUserRequest (String s) {
		JSONObject obj = new JSONObject(s);
		this.name = obj.getString("user");
		this.sender = obj.getString("sender");
	}
	
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("type", Integer.toString(this.type));
		obj.put("user", this.name);
		obj.put("sender", sender);
		return obj.toString();
	}

}
