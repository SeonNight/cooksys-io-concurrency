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

public class TicketClientHandler implements Runnable {
	private Socket clientSocket;

	public TicketClientHandler(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}

	public void run() {
		try {
			DataInputStream dataIn = new DataInputStream(this.clientSocket.getInputStream());
			// Interval of waiting and responding
			int interval = dataIn.readInt();
			if (interval < 1 || interval > 10) {
				System.out.println("Interval too big or too small: " + interval);
				return;
			} else {
				interval *= 1000;
			}

			// Create buffered reader and string reader to read a request from a client
			// socket inputstream
			StringReader stringReader = new StringReader(
					new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream())).readLine());
			// request
			JAXBContext context = JAXBContext.newInstance(QuoteField.class, QuoteRequest.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			// returnQuote
			JAXBContext quoteContext = JAXBContext.newInstance(ReturnQuoteField.class, ReturnQuote.class,
					ReturnQuotes.class);
			Marshaller quoteMarshaller = quoteContext.createMarshaller();

			// Unmarshall stringReader to QuoteRequest object
			QuoteRequest quoteRequest = (QuoteRequest) unmarshaller.unmarshal(stringReader);

			while (true) {
				// Fetch quotes for each
				Set<Quote> quotes = StockApi.fetchQuotes(quoteRequest);
				ReturnQuotes returnQuotes = TicketClientHandler.QuoteToXml(quotes, quoteRequest.getFields());
				// quoteMarshaller.marshal(returnQuotes, new FileOutputStream("test.xml"));

				// Marshal request to stringWriter (Formatting request to send to server)
				StringWriter stringWriter = new StringWriter();
				quoteMarshaller.marshal(returnQuotes, stringWriter);

				// Create bufferedWriter from socket outpustream and write stringWriter to
				// out.write()
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				out.write(stringWriter.toString()); // push request it to server
				out.newLine(); // Push a new line
				out.flush();

				Thread.sleep(interval);
			}
		} catch (IOException | JAXBException e) {
			System.out.println("Client Handler Failed: ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Error in Sleep: ");
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
