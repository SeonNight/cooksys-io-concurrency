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
		fields.add(QuoteField.OPEN);

		// Stock ticker symbols client is requesting
		Set<String> symbols = new HashSet<>(Arrays.asList("NIO", "TWTR"));

		// Encapsulating request object
		QuoteRequest request = new QuoteRequest(fields, symbols);
		// Okay this is what is sent to the server, the fields are who you want to look
		// up,
		// and symbols are what you want to look up.

		try (Socket socket = new Socket("localhost", 3000);) {
			DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
			int interval = 10;
			// Send out interval
			dataOut.writeInt(interval);
			dataOut.flush();

			interval *= 1000;

			JAXBContext context = JAXBContext.newInstance(QuoteField.class, QuoteRequest.class);
			Marshaller marshaller = context.createMarshaller();

			// Marshal request to stringWriter (Formatting request to send to server)
			StringWriter stringWriter = new StringWriter();
			marshaller.marshal(request, stringWriter);

			// Create bufferedWriter from socket outpustream and write stringWriter to
			// out.write()
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			out.write(stringWriter.toString()); // push request it to server
			out.newLine(); // Push a new line
			out.flush();

			while (true) {
				// Get response and output into a file
				JAXBContext quoteContext = JAXBContext.newInstance(ReturnQuoteField.class, ReturnQuote.class,
						ReturnQuotes.class);
				Marshaller quoteMarshaller = quoteContext.createMarshaller();
				Unmarshaller quoteUnmarshaller = quoteContext.createUnmarshaller();
				// Unmarshall stringReader to QuoteRequest object
				StringReader stringReader = new StringReader(
						new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine());
				ReturnQuotes returnQuotes = (ReturnQuotes) quoteUnmarshaller.unmarshal(stringReader);
				quoteMarshaller.marshal(returnQuotes, new FileOutputStream("test.xml"));

				System.out.println("UpdateQuotes: ");
				for (ReturnQuote r : returnQuotes.getQuotes()) {
					System.out.println(" Symbol: " + r.getSymbol());
					for (ReturnQuoteField f : r.getFields()) {
						System.out.println("     Fields: " + f.getOpen());
					}

				}

				Thread.sleep(interval);
			}

		} catch (JAXBException | IOException e) {
			System.out.println("Client Failed: ");
			e.printStackTrace();
		} catch (InterruptedException e) {
			System.out.println("Error in Sleep: ");
			e.printStackTrace();
		}

	}

}
