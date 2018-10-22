package com.cooksys.ftd.ticker.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.atomic.AtomicInteger;


public class TicketServerMult {
	private static AtomicInteger currentClients = new AtomicInteger(0);
	
    public static void main(String[] args) {
    	System.out.println("--Server--");

    	try (
    		ServerSocket serverSocket = new ServerSocket(3000);
    	) {
			while(true) {
				try {
					new Thread(new TicketClientHandler(serverSocket.accept())).start();
					System.out.println("Added Client: " + TicketServerMult.addClient());
				} catch (IOException e) {
					System.out.println("Server Connection Failed: ");
				    e.printStackTrace();
				}
			}
    	} catch (IOException e) {
    		System.out.println("Server Failed To Open: ");
    		e.printStackTrace();
    	}

    }
	
	public static int addClient() {
		return TicketServerMult.currentClients.incrementAndGet();
	}
	
	public static int removeClient() {
		return TicketServerMult.currentClients.decrementAndGet();
	}
}
