package com.alessio.sharenotes.chat;

import org.json.JSONObject;

public class ChatMessage {
	
	private final int type = MessageTypes.CHAT_MESSAGE;
	public String sender, senderName, recipient, recipientName, payload;
	public long ts = 0;

	public ChatMessage(String sender, String senderName, String recipient, String recipientName, String payload, long ts) {
		
		this.sender = sender;
		this.senderName = senderName;
		this.recipient = recipient;
		this.recipientName = recipientName;
		this.payload = payload;
		this.ts = ts;
	}
	public ChatMessage (String s){
		JSONObject obj = new JSONObject(s);
		this.sender = obj.getString("sender");
		this.senderName = obj.getString("senderName");
		this.recipient = obj.getString("recipient");
		this.recipientName = obj.getString("recipientName");
		this.payload = obj.getString("payload");
		this.ts = Long.parseLong(obj.getString("ts"),10);
	}
	public String toJSONString() {
		JSONObject obj = new JSONObject();
		obj.put("type", Integer.toString(this.type));
		obj.put("sender", this.sender);
		obj.put("senderName", this.senderName);
		obj.put("recipient", this.recipient);
		obj.put("recipientName", this.recipientName);
		obj.put("payload", this.payload);
		obj.put("ts", Long.toString(this.ts));
		return obj.toString();
	}


}
