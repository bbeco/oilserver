package com.alessio.sharenotes.chat;

import org.json.JSONObject;

public class SearchUserRequest {
	
	private final int type = MessageTypes.SEARCH_USER_REQUEST;
	public String name;
	
	public SearchUserRequest (){};
	
	public SearchUserRequest (String s) {
		JSONObject obj = new JSONObject(s);
		this.name = obj.getString("name");
	}
	
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("type", Integer.toString(this.type));
		obj.put("name", this.name);
		return obj.toString();
	}

}
