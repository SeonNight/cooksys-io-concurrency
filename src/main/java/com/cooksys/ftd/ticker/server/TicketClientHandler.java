package com.cooksys.ftd.ticker.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.Socket;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.cooksys.ftd.ticker.api.StockApi;
import com.cooksys.ftd.ticker.api.StockUtils;
import com.cooksys.ftd.ticker.dto.QuoteField;
import com.cooksys.ftd.ticker.dto.QuoteRequest;

import pl.zankowski.iextrading4j.api.stocks.Quote;

public class TicketClientHandler implements Runnable {
	private Socket clientSocket;
	
	public TicketClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

    public void run() {
        try (
            // Create buffered reader and string reader to read a request from a client socket inputstream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            StringReader stringReader = new StringReader(bufferedReader.readLine());
        ) {
            JAXBContext context = JAXBContext.newInstance(QuoteField.class, QuoteRequest.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            
            // Unmarshall stringReader to QuoteRequest object
            QuoteRequest quoteRequest = (QuoteRequest) unmarshaller.unmarshal(stringReader);

            // Fetch quotes for each
            Set<Quote> quotes = StockApi.fetchQuotes(quoteRequest);

            // Map quote results to a formatted string
            String stringQuote = StockUtils.quotesToString(quotes, quoteRequest.getFields());

            // Write and flush formatted string to client socket
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            out.write(stringQuote);
            out.flush();
            
        } catch (IOException | JAXBException e) {
        	System.out.println("Client Handler Failed: ");
            e.printStackTrace();
        }
        
        try {
			this.clientSocket.close();
		} catch (IOException e) {
			System.out.println("Failed to close ClinetHandler: ");
			e.printStackTrace();
		}

		System.out.println("Removed Client: " + TicketServerMult.removeClient());
    }

    public Socket getClientSocket() {
		return this.clientSocket;
	}
}
