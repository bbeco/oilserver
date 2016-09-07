package com.alessio.sharenotes.chat;

import org.json.*;


public class Message {
	public String registration, sender, recipient, payload;
	public long ts = 0;

	public Message(String s) {
		JSONObject obj = new JSONObject(s);
		
		if (s.contains("registration")) {
			this.registration = obj.getString("registration");
		} else {
			this.sender = obj.getString("sender");
			this.recipient = obj.getString("recipient");
			this.payload = obj.getString("payload");
		}
		
		this.ts = Long.parseLong(obj.getString("ts"), 10);
	}
	
	/* added to create a message on client side */
    public Message(String sender, String recipient, String payload, long ts) {
        this.sender = sender;
        this.recipient = recipient;
        this.payload = payload;
        this.ts = ts;
    }
    
    public String toJSONString () {
        JSONObject obj = new JSONObject();
        try {
        	// non controllo registration perchè non devo mai inviarlo
        	
            if (this.sender != null) {
                obj.put("sender",this.sender);
            }

            if (this.recipient != null) {
                obj.put("recipient",this.recipient);
            }

            if (this.payload != null) {
                obj.put("payload",this.payload);
            }

            obj.put("ts",Long.toString(this.ts));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
