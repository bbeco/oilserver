package com.alessio.sharenotes.chat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class Worker implements Runnable {
	private static final String driverName = "org.sqlite.JDBC";
	private static final String dbURL = "jdbc:sqlite:Database/Chat.db";
	
	private final List<Client> list;
	private final Client client;

	public Worker(List<Client> list, Client client) {
		this.list = list;
		this.client = client;
	}

	@Override
	public void run() {
		System.out.println("Inizio worker del client: " + client.getSocket().getInetAddress().toString());
		
		while(!client.getSocket().isClosed()) {
			String s = client.receive();
			System.out.println("Ricevuto messaggio: " + s);
			
			if (s == null) {
				// Socket chiuso
				System.out.println("Socket chiuso: " + client.getSocket().getInetAddress().toString());
				break;
			}
			
			Message message = new Message(s);
			
			if (message.registration != null) {
				client.userID = message.registration;
				sendUnreadMessages(client,message.ts);
			} else if (!message.recipient.isEmpty()) {
				for (final Client dest: list) {
					if (dest.userID.equals(message.recipient)) {
						dest.send(s);
					}
				}
				insertIntoDatabase(message);
			} else {
				System.out.println("Ricevuto JSON non valido :" + s);
			}
		}
		
		String ip = client.getSocket().getInetAddress().toString();
		
		// Rimuovo dalla lista dei client
		System.out.println("Devo rimuovere dalla lista il client: " + ip);
		for (int i=0; i<list.size(); i++) {
			if (list.get(i).getSocket() == client.getSocket()) {
				System.out.println("Trovato il client da rimuovere");
				list.remove(i);
				break;
			}
		}
		
		System.out.println("Fine thread del client: " + ip);
	}

	private void insertIntoDatabase(Message m) {
		System.out.println("Inzio insetIntoDatabase");
        try {
            Class.forName(driverName);
            Connection c = DriverManager.getConnection(dbURL);
            Statement stmt = c.createStatement();
            String query = "INSERT INTO CHAT (sender,recipient,payload,ts) VALUES ('"+m.sender+"','"+m.recipient+"','"+replaceShit(m.payload)+"',"+m.ts+");";
            System.out.println("Query: "+query);
            stmt.executeUpdate(query);
            stmt.close();
            c.close();
        } catch (SQLException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
        System.out.println("Fine insetIntoDatabase");
	}

	private void sendUnreadMessages(Client cl, long ts) {	
		System.out.println("Inzio sendUnreadMessages()");
		try {
			Class.forName(driverName);
			Connection c = DriverManager.getConnection(dbURL);
			Statement stmt = c.createStatement();
			String query = "SELECT sender, recipient, payload, ts FROM chat WHERE (recipient = " + cl.userID + " OR sender = " + cl.userID + ") AND ts > " + ts + " ORDER BY ts;";
			System.out.println("Query: "+query);
			ResultSet rs = stmt.executeQuery(query);
			
			while (rs.next()) {
				System.out.println(rs.getString("payload"));
				Message msg = new Message(rs.getString("sender"),rs.getString("recipient"),replaceShit(rs.getString("payload")),rs.getLong("ts"));
				String json = msg.toJSONString();
				System.out.println("Invio: " + json);				
				cl.send(json);
				Thread.sleep(100);
			}
			
			stmt.close();
			c.close();
		} catch (SQLException | ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Fine sendUnreadMessages()");
	}
	
	private static String replaceShit(String s){
        //s = s.replaceAll("\"","\\\\\"");
        s = s.replaceAll("'", "''");
        return s;
    }
}
