package com.alessio.sharenotes.chat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
	private ServerSocket socket;
	private int port;


	public ChatServer(int port) {
		this.port = port;
		try {
			this.socket = new ServerSocket(port); 
		} catch (IOException ex) {
			System.err.println("Errore creazione del server. Terminazione");
			System.exit(1);
		}
	}

	public ServerSocket getSocket() {
		return socket;
	}
	
	public int getPort() {
		return port; 
	}



	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println("Inserisci il numero di porta");
			System.exit(0);
		}
		int port = 0;
		try {
			port = Integer.parseInt(args[0]);
			System.out.println("Numero porta: " + port);
		} catch (NumberFormatException ex ) {
			System.err.println("Inserisci una porta valida");
			System.exit(0);
		}

		List<Client> clients = new LinkedList<Client>();
		ChatServer server = new ChatServer(port);
		ExecutorService executor = Executors.newFixedThreadPool(100);

		for (;;) {
			System.out.println("Attesa di un client...");
			Socket s = server.getSocket().accept();
			System.out.println("Client accettato");
			Client cl = new Client(s);

			if(!clients.add(cl)) {
				System.err.println("Errore nell'aggiungere il client");
				s.close();
			} else {
				System.out.println("Faccio partire thread");
				Runnable worker = new Worker(clients, cl);
				executor.execute(worker);
			}
		}
	}
}