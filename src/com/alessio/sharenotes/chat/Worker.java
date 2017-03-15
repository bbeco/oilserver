package com.alessio.sharenotes.chat;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class Worker implements Runnable {
	private static final String driverName = "org.sqlite.JDBC";
	private static final String dbURL = "jdbc:sqlite:Database/Chat.db";
	private static final String dbOil = "jdbc:sqlite:Database/mydb.db";
	
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
			String res = client.receive();
			if (res == null) {
				// Socket chiuso
				System.out.println("Socket chiuso: " + client.getSocket().getInetAddress().toString());
				break;
			}
			
			String[] parts = res.split("(?=" + Pattern.quote("{") + ")");
			for (String s : parts) {
				
				System.out.println("Ricevuto messaggio: " + s);
				
				//modifica da qui
				JSONObject obj = new JSONObject(s);
				int type = obj.getInt("type");
				switch(type){
				case MessageTypes.CHAT_MESSAGE:
					ChatMessage m = new ChatMessage(s);
					System.out.println("Ricevuto messaggio:\n\t" + m);
					for (final Client dest: list) {
						if (dest.userID.equals(m.recipient)) {
							dest.send(s);
						}
					}
					insertIntoDatabase(m);
					break;
				case MessageTypes.REGISTRATION_REQUEST:
					System.out.println("Registrationi received");
					RegistrationRequest r = new RegistrationRequest(s);
					client.userID = r.userId;
					client.name = r.name;
					sendUnreadMessages(client,r.ts);
					break;
				case MessageTypes.SEARCH_STATION_REQUEST:
					System.out.println("sending oil stations");
					SearchOilRequest sor = new SearchOilRequest(s);//contains user latitude and longitude to use in the query
					try {
						Class.forName(driverName);
						Connection c = DriverManager.getConnection(dbOil);
			            Statement stmt = c.createStatement();
			            String query = "select latitude,longitude,oil,diesel,gpl from OILMAP";
			            ResultSet rs = stmt.executeQuery(query);
			            SearchOilResponse msg = new SearchOilResponse();
			            while (rs.next()) {
							msg.oils.add(new SearchOilResponse.Oils(Double.parseDouble(rs.getString("latitude")),
									Double.parseDouble(rs.getString("longitude")),
									Double.parseDouble(rs.getString("oil")),
									Double.parseDouble(rs.getString("diesel")),
									Double.parseDouble(rs.getString("gpl"))));
						}
			            /* Removing oil stations that are farer than 15 Km */
			            int i = 0;
			            while(i < msg.oils.size()){
		                    if(measure(sor.latitude,sor.longitude,msg.oils.get(i).latitude,msg.oils.get(i).longitude) > 15){
		                      msg.oils.remove(i);
		                    } else {
		                      i++;
		                    }
		                  }
			            String json = msg.toJSONString();
			            System.out.println(json);
			            client.send(json);
						Thread.sleep(100);
			            stmt.close();
			            c.close();
			            //client.getSocket().close();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MessageTypes.SEARCH_USER_REQUEST:
					SearchUserRequest sur = new SearchUserRequest(s);
					sendSearchUserResponse(client, sur);
					break;
					
				case MessageTypes.MODIFY_REQUEST:
					ModifyRequest mreq = new ModifyRequest(s);
					try {
						Class.forName(driverName);
						Connection c = DriverManager.getConnection(dbOil);
			            Statement stmt = c.createStatement();
			            String query = "select * from OILMAP where latitude="+mreq.latitude+" and longitude="+mreq.longitude;
			            ResultSet rs = stmt.executeQuery(query);
			            if(!rs.next()){
			            	System.out.println("Inserting new oil station");
			            	String query1 = "insert into OILMAP(latitude,longitude,oil,diesel,gpl) values ("+mreq.latitude+","+mreq.longitude+","+mreq.oil+","+mreq.diesel+","+mreq.gpl+")";
			            	stmt.executeUpdate(query1);
			            } else {
			            	System.out.println("Executing update on an oil station");
			            	String query2 = "update OILMAP set oil="+mreq.oil+", diesel="+mreq.diesel+", gpl="+mreq.gpl+" where latitude="+mreq.latitude+" and longitude="+mreq.longitude;
				            stmt.executeUpdate(query2);
			            }
			            stmt.close();
			            c.close();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case MessageTypes.COMMUTE_REQUEST:
					System.out.println("ricevuto messaggio CommuteRequest");
					CommuteRequest creq = new CommuteRequest(s);
					try {
						Class.forName(driverName);
						Connection c = DriverManager.getConnection(dbOil);
				        Statement stmt = c.createStatement(); 

				        if(!creq.valid){
				        	//delete information from server
				        	String query = "delete from COMMUTE where email="+"'"+creq.email+"'";
				        	stmt.executeUpdate(query);
				        } else if (creq.latHome != null && creq.email != null){
							//new user, insert the information in the database
				            String query = "insert into COMMUTE(email,latitudeHome,longitudeHome,latitudeWork,longitudeWork) values("+"'"+creq.email+"',"+creq.latHome+","+creq.longHome+","+creq.latWork+","+creq.longWork+")";
				            System.out.println(query);
				            stmt.executeUpdate(query);
						} else {
							String query = "select * from COMMUTE where email="+"'"+creq.email+"'";
							ResultSet rs = stmt.executeQuery(query);
							if(!rs.next()){
								//send an empty commute request (except the mail)
								String json = creq.toJSONString();//the same one received
					            client.send(json);
								Thread.sleep(100);
							} else {
								//found a row matching the request, send all the information (home,work)
								CommuteRequest comResp = new CommuteRequest(Double.parseDouble(rs.getString("latitudeHome")),
																			Double.parseDouble(rs.getString("longitudeHome")),
																			Double.parseDouble(rs.getString("latitudeWork")),
																			Double.parseDouble(rs.getString("longitudeWork")),
																			rs.getString("email"),
																			true);
								String json = comResp.toJSONString();
								client.send(json);
								Thread.sleep(100);
								//Double distance = Math.sqrt(Math.pow(creq.latHome -creq.latWork, 2) + Math.pow(creq.longHome-creq.longWork, 2));
								
								query = "select latitude,longitude,oil,diesel,gpl from OILMAP where latitude >="+Math.min(comResp.latHome,comResp.latWork)+" and latitude <="+Math.max(comResp.latWork,comResp.latHome)+" and longitude>="+Math.min(comResp.longHome,comResp.longWork)+" and longitude <="+Math.max(comResp.longWork,comResp.longHome);
					            rs = stmt.executeQuery(query);
					            SearchOilResponse msg = new SearchOilResponse();
					            while (rs.next()) {
									msg.oils.add(new SearchOilResponse.Oils(Double.parseDouble(rs.getString("latitude")),
											Double.parseDouble(rs.getString("longitude")),
											Double.parseDouble(rs.getString("oil")),
											Double.parseDouble(rs.getString("diesel")),
											Double.parseDouble(rs.getString("gpl"))));
								}
					            json = msg.toJSONString();
					            System.out.println("invio oil stations nella commute");
					            client.send(json);
								Thread.sleep(100);
							}
						}
						stmt.close();
			            c.close();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				default:
					System.out.println("Ricevuto JSON non valido :" + s);
					
				}
			}
		}//while (<socket is not closed>)
		
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

	private void insertIntoDatabase(RegistrationRequest r) {
		String query = "INSERT INTO email(emailAddress,name) " +
						"SELECT * FROM (SELECT \"" + r.userId + "\", \"" + r.name + "\") AS tmp " +
						"WHERE NOT EXISTS ( " +
							"SELECT emailAddress FROM email WHERE emailAddress= \"" + r.userId + "\");";
		try{
			Class.forName(driverName);
			Connection c = DriverManager.getConnection(dbURL);
			Statement stmt = c.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
			c.close();
		} catch (SQLException | ClassNotFoundException ex) {
			ex.printStackTrace();
		}
		System.out.println("Fine insertIntoDatabase");
	}
	
	private void insertIntoDatabase(ChatMessage m) {
		System.out.println("Inzio insertIntoDatabase");
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
        
        System.out.println("Fine insertIntoDatabase");
	}
	
	/**
	 * 
	 * @param cl response recipient
	 * @param name The name we are looking for
	 */
	private void sendSearchUserResponse(Client cl, SearchUserRequest sur) {
		System.out.println("Inzio sendSearchUserResponse()");
		try {
			Class.forName(driverName);
			Connection c = DriverManager.getConnection(dbURL);
			Statement stmt = c.createStatement();
			String query = "SELECT * FROM email WHERE name LIKE '%" + sur.name + "%' AND emailAddress <> '" + sur.sender + "';";
			ResultSet rs = stmt.executeQuery(query);
			SearchUserResponse resp = new SearchUserResponse();
			/* Merge all the messages found in a single response */
			while (rs.next()) {
				resp.names.add(new SearchUserResponse.User(rs.getString("name"), rs.getString("emailAddress")));
			}
			String json = resp.toJSONString();
			System.out.println("Invio: " + json);				
			cl.send(json);
			Thread.sleep(100);
			stmt.close();
			c.close();
		} catch (SQLException | ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Fine sendSearchUserResponse()");
	}

	private void sendUnreadMessages(Client cl, long ts) {	
		System.out.println("Inzio sendUnreadMessages()");
		try {
			Class.forName(driverName);
			Connection c = DriverManager.getConnection(dbURL);
			Statement stmt = c.createStatement();
			/* ts is the timestamp of the last communication with the server. It is increased by a client when the 
			 * communication is done. 
			 */
			String query = "SELECT sender, a.name as sender_name, recipient, b.name as recipient_name, payload, ts FROM chat JOIN email a ON sender = a.emailAddress JOIN email b ON b.emailAddress = recipient WHERE (recipient = '" + cl.userID + "' OR sender = '" + cl.userID + "') AND ts > " + ts + " ORDER BY ts;";
			System.out.println("Query: "+query);
			ResultSet rs = stmt.executeQuery(query);
			RegistrationResponse resp = new RegistrationResponse();
			/* Merge all the messages found in a single response */
			while (rs.next()) {
				System.out.println(rs.getString("payload"));
				resp.messages.add(new ChatMessage(rs.getString("sender"), rs.getString("sender_name"),rs.getString("recipient"), rs.getString("recipient_name"),replaceShit(rs.getString("payload")),rs.getLong("ts")));
			}
			String json = resp.toJSONString();
			System.out.println("Invio: " + json);				
			cl.send(json);
			Thread.sleep(100);
			stmt.close();
			c.close();
		} catch (SQLException | ClassNotFoundException ex) {
			ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Fine sendUnreadMessages()");
	}
	
	private static String replaceShit(String s){
        //s = s.replaceAll("\"","\\\\\"");
        s = s.replaceAll("'", "''");
        return s;
    }
	
	/**
	 * Debug function that prints all the client id currently connected to the server.
	 */
	private void logConnectedClients() {
		for (Client c : list) {
			System.out.println("UserID = " + c.userID + " Name = " + c.name);
		}
	}
	
	private double measure(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6372.8;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
	
	
}
