package com.cooksys.ftd.ticker.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.cooksys.ftd.ticker.api.StockApi;
import com.cooksys.ftd.ticker.dto.QuoteField;
import com.cooksys.ftd.ticker.dto.QuoteRequest;
import com.cooksys.ftd.ticker.dto.ReturnQuote;
import com.cooksys.ftd.ticker.dto.ReturnQuoteField;
import com.cooksys.ftd.ticker.dto.ReturnQuotes;

import pl.zankowski.iextrading4j.api.stocks.Quote;

//Server thread that handles client
public class TicketClientHandler implements Runnable {
	private Socket clientSocket;

	public TicketClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		try (
				// Input to receive interval
				DataInputStream dataIn = new DataInputStream(this.clientSocket.getInputStream());
				// Create buffered reader and string reader to read a request from a client
				BufferedReader in = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
				// Create bufferedWriter from socket outpustream and write stringWriter to
				// client
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));) {
			// Unmarshallar for requests
			JAXBContext context = JAXBContext.newInstance(QuoteField.class, QuoteRequest.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			// Marshaller for quotes
			JAXBContext quoteContext = JAXBContext.newInstance(ReturnQuoteField.class, ReturnQuote.class,
					ReturnQuotes.class);
			Marshaller quoteMarshaller = quoteContext.createMarshaller();

			StringReader stringReader; // Read quote Requests from client
			StringWriter stringWriter; // StringWriter to send store RequestQuotes to send to client

			QuoteRequest quoteRequest; // Receive request from client
			ReturnQuotes returnQuotes; // xml formated quotes to send to client
			Set<Quote> quotes; // Quotes fetched from StockApi

			int interval; // Interval of waiting to update quote request

			// Receive interval if failed return
			interval = dataIn.readInt();
			if (interval < 1 || interval > 10) {
				System.out.println("Interval too big or too small: " + interval);
				return;
			} else {
				interval *= 1000;
			}

			// Unmarshall stringReader to QuoteRequest object
			stringReader = new StringReader(in.readLine());
			quoteRequest = (QuoteRequest) unmarshaller.unmarshal(stringReader);

			// Repeat until server or client shuts down
			while (true) {
				// Fetch quotes for each
				quotes = StockApi.fetchQuotes(quoteRequest);
				returnQuotes = TicketClientHandler.QuoteToXml(quotes, quoteRequest.getFields());

				// Marshal request to stringWriter (Formatting request to send to server)
				stringWriter = new StringWriter();
				quoteMarshaller.marshal(returnQuotes, stringWriter);

				// write xml to client
				out.write(stringWriter.toString()); // push request it to server
				out.newLine(); // Push a new line
				out.flush();

				// Wait to update request
				Thread.sleep(interval);
			}
		} catch (IOException | JAXBException e) {
			System.out.println("Client Handler Failed: ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Error during Sleep: ");
			e.printStackTrace();
		}

		// Close client socket
		try {
			this.clientSocket.close();
		} catch (IOException e) {
			System.out.println("Failed to close ClinetHandler: ");
			e.printStackTrace();
		}
	}

	// Turns given quotes into xml file
	public static ReturnQuotes QuoteToXml(Set<Quote> quotes, Set<QuoteField> quoteRequestField) {
		ReturnQuotes returnQuotes = new ReturnQuotes();
		Set<ReturnQuote> returnQuoteSet = new HashSet<ReturnQuote>();

		for (Quote q : quotes) {
			ReturnQuote returnQuote = new ReturnQuote();
			returnQuote.setSymbol(q.getSymbol());
			ReturnQuoteField returnQuoteField = new ReturnQuoteField();
			for (QuoteField qf : quoteRequestField) {
				switch (qf.getField()) {
				case "open":
					returnQuoteField.setOpen(q.getOpen());
					break;
				case "close":
					returnQuoteField.setClose(q.getClose());
					break;
				case "high":
					returnQuoteField.setHigh(q.getHigh());
					break;
				case "low":
					returnQuoteField.setLow(q.getLow());
					break;
				case "latestPrice":
					returnQuoteField.setLatestPrice(q.getLatestPrice());
					break;
				case "change":
					returnQuoteField.setChange(q.getChange());
					break;
				case "changePercent":
					returnQuoteField.setChangePercent(q.getChangePercent());
					break;
				default:
					break;
				}
			}
			returnQuote.setFields(returnQuoteField);
			returnQuoteSet.add(returnQuote);
		}

		returnQuotes.setQuotes(returnQuoteSet);

		return returnQuotes;
	}
}
