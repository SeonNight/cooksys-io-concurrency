package com.cooksys.ftd.ticker.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import com.cooksys.ftd.ticker.dto.QuoteField;
import com.cooksys.ftd.ticker.dto.QuoteRequest;
import com.cooksys.ftd.ticker.dto.ReturnQuote;
import com.cooksys.ftd.ticker.dto.ReturnQuoteField;
import com.cooksys.ftd.ticker.dto.ReturnQuotes;

public class TickerClientXml implements Runnable {
	public void run() {

		// List of fields that the client is requesting on a quote
		Set<QuoteField> fields = new HashSet<>(Collections.singletonList((QuoteField.LATEST_PRICE)));

		// Stock ticker symbols client is requesting
		Set<String> symbols = new HashSet<>(Arrays.asList("NIO", "TWTR"));

		// Encapsulating request object
		QuoteRequest request = new QuoteRequest(fields, symbols);
		// Okay this is what is sent to the server, the fields are who you want to look
		// up,
		// and symbols are what you want to look up.

		try (
				// Try to connect to server
				Socket socket = new Socket("localhost", 3000);
				// used to send interval
				DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
				// Used to send request to server
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
				// Used to receive quotes from server
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
			// Marshaller for quote request to send to server
			JAXBContext context = JAXBContext.newInstance(QuoteField.class, QuoteRequest.class);
			Marshaller marshaller = context.createMarshaller();

			// Marshaller and Unmarshaller for received quotes
			JAXBContext quoteContext = JAXBContext.newInstance(ReturnQuoteField.class, ReturnQuote.class,
					ReturnQuotes.class);
			Marshaller quoteMarshaller = quoteContext.createMarshaller();
			Unmarshaller quoteUnmarshaller = quoteContext.createUnmarshaller();

			StringWriter stringWriter; // writer to store request to send to server
			StringReader stringReader; // reader to read quotes from server

			int interval = 10; // interval of how long to wait till update

			// Send out interval
			dataOut.writeInt(interval);
			dataOut.flush();

			interval *= 1000;

			// Marshal request to stringWriter (Formatting request to send to server)
			stringWriter = new StringWriter();
			marshaller.marshal(request, stringWriter);

			// Create bufferedWriter from socket outpustream and write stringWriter to
			// out.write()
			out.write(stringWriter.toString()); // push request it to server
			out.newLine(); // Push a new line
			out.flush();

			//Loop until server or client dies
			while (true) {
				// Get response and output into a file
				// Unmarshall stringReader to QuoteRequest object
				stringReader = new StringReader(in.readLine());
				// There is probably a better way to do this, but this is what I got...
				quoteMarshaller.marshal((ReturnQuotes) quoteUnmarshaller.unmarshal(stringReader),
						new FileOutputStream("output.xml"));

				// wait than receive new quotes from server
				Thread.sleep(interval);
			}

		} catch (JAXBException | IOException e) {
			System.out.println("Client Failed: ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Error during Sleep: ");
			e.printStackTrace();
		}

	}

}
