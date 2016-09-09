package com.alessio.sharenotes.chat;

import java.util.ArrayList;
import java.util.Collection;

import org.json.*;


public class Message {
	/** This class is used to return results from a search for a user. */
	static class User {
		public String name = null;
		public String userId = null;
		
		public User() {}
		public User(String n, String id) {
			name = n;
			userId = id;
		}
		public String toString() {
			return "{ name = \"" + this.name + "\", userId = \"" + userId + "\" }";
		}
	}
	
	/**
	 * This field is used by clients to request if a given username exists. The reply by the server is the same json
	 * object and this field contains the userID (if username was found) or ERROR if not.
	 */
	public String query;
	
	/**
	 * These fields are used by the server and they are filled with the result of the previously received query
	 */
	public ArrayList<User> result;
	public int resultSize;
	
	/**
	 * This field is the profile name for this client. It is not a unique value like userID.
	 * Its value is sent during the registration phase and it is inserted by the worker thread.
	 */
	public String name;
	
	public String registration, sender, recipient, payload;
	public long ts = 0;

	public Message() {
		query = name = registration = sender = recipient = payload = null;
		result = null;
		resultSize = 0;
	}
	
	public Message(String s) {
		JSONObject obj = new JSONObject(s);
		
		if (s.contains("query")) {
			/* query message */
			this.query = obj.getString("query");
		} else {
			if (s.contains("registration")) {
				/* registration message */
				this.registration = obj.getString("registration");
				this.name = obj.getString("name");
			} else {
				/* default message */
				this.sender = obj.getString("sender");
				this.recipient = obj.getString("recipient");
				this.payload = obj.getString("payload");
			}
			
			this.ts = Long.parseLong(obj.getString("ts"), 10);
		}
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
        	// non controllo registration e name perchè non devo mai inviarli
        	
        	if (this.result != null) {
        		Collection<JSONObject> users = new ArrayList<JSONObject>();
        		for (Message.User u : result) {
        			JSONObject jo = new JSONObject();
        			jo.put("name", u.name);
        			jo.put("UserId", u.userId);
        			users.add(jo);
        		}
        		obj.put("result", users);
        		obj.put("resultSize", Integer.toString(resultSize));
        	}
        	        	
        	if (this.query != null) {
                obj.put("query",this.query);
            }
        	
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

    		obj.put("resultSize", Integer.toString(this.resultSize));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }
}
