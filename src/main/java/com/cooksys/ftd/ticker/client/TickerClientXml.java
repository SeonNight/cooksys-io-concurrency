package com.cooksys.ftd.ticker.client;


import com.cooksys.ftd.ticker.dto.QuoteField;
import com.cooksys.ftd.ticker.dto.QuoteRequest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class TickerClientXml implements Runnable{
    public void run() {

        // List of fields that the client is requesting on a quote
        Set<QuoteField> fields = new HashSet<>(Collections.singletonList((QuoteField.LATEST_PRICE)));

        // Stock ticker symbols client is requesting
        Set<String> symbols = new HashSet<>(Arrays.asList("NIO", "TWTR"));

        // Encapsulating request object
        QuoteRequest request = new QuoteRequest(fields, symbols);

        try (
            Socket socket = new Socket("localhost", 3000);
        ) {

            JAXBContext context = JAXBContext.newInstance(QuoteField.class, QuoteRequest.class);
            Marshaller marshaller = context.createMarshaller();

            // Marshal request to stringWriter
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(request, stringWriter);

            // Create bufferedWriter from socket outpustream and write stringWriter to out.write()
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            out.write(stringWriter.toString());
            out.newLine();
            out.flush();

            
            // Get responses
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String message;
            while (socket.isConnected() && (message = in.readLine()) != null) {
                System.out.println(message);
            }
            
            //Save responses in quotes.xml
    		//marshaller.marshal(fields, new FileOutputStream("quotes.xml"));

        } catch (JAXBException | IOException e) {
        	System.out.println("Client Failed: ");
            e.printStackTrace();
        }

    }

}
