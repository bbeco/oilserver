package com.alessio.sharenotes.chat;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegistrationResponse {
	private final int type = MessageTypes.REGISTRATION_RESPONSE;
	
	public ArrayList<ChatMessage> messages;
	
	public RegistrationResponse() {
		messages = new ArrayList<>();
	}
	
	public RegistrationResponse(String s) {
		JSONObject obj = new JSONObject(s);
		
		messages = new ArrayList<>();
		JSONArray array = obj.getJSONArray("messages");
		for (int i = 0; i < array.length(); i++) {
			JSONObject jsonMessage = array.getJSONObject(i);
			messages.add(new ChatMessage(jsonMessage.getString("sender"),
					jsonMessage.getString("senderName"),
					jsonMessage.getString("recipient"),
					jsonMessage.getString("recipientName"),
					jsonMessage.getString("payload"),
					Long.parseLong(jsonMessage.getString("ts"))));
		}
		
	}
	
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("type", Integer.toString(type));
		
		Collection<JSONObject> jsonCollection = new ArrayList<>();
		
		for (ChatMessage m : messages) {
			JSONObject jo = new JSONObject();
			jo.put("sender", m.sender);
			jo.put("senderName", m.senderName);
			jo.put("recipient", m.recipient);
			jo.put("recipientName", m.recipientName);
			jo.put("payload", m.payload);
			jo.put("ts", Long.toString(m.ts));
			jsonCollection.add(jo);
		}
		
		obj.put("messages", jsonCollection);
		return obj.toString();
	}
}
