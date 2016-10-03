package com.alessio.sharenotes.chat;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private final Socket socket;
	public String userID = "";
	public String name = "";
    
	public Client (Socket socket) {
		this.socket = socket;
	}
    
    public Socket getSocket() {
		return socket;
	}
    
    /*
     * This is used to submit a message to this client object
     */
    public boolean send(String s) {
    	//System.out.println("send(): " + s);
    	byte[] msg = s.getBytes();
    	
        try {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            out.write(msg);
            out.flush();
        } catch (IOException e) {
            System.err.println("Errore invio messaggio");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    
    public String receive() {
        byte[] tmp = new byte[4096];
        byte[] buffer;
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            int ret = in.read(tmp);
            if (ret == -1) {
                buffer = null;
            } else {
                buffer = new byte[ret];
                System.arraycopy(tmp, 0, buffer, 0, ret);
            }
            
        } catch (IOException e) {
            System.err.println("Errore ricezione messaggio");
            e.printStackTrace();
            buffer = null;
        }
        
        if (buffer == null) {
        	System.out.println("receive(): nulla");
        	return null;
        }
        
        
        String s = new String(buffer);
        System.out.println("receive(): " + s);
        return s;
    }
    
    @Override
    public boolean equals(Object obj) {
        Client c = (Client) obj;
        if (obj != null) {
            return c.userID.equals(this.userID);
        }
        return false;
    }
}
