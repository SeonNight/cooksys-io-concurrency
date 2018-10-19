package com.cooksys.ftd.ticker.server;

import com.cooksys.ftd.ticker.dto.QuoteField;
import com.cooksys.ftd.ticker.dto.QuoteRequest;
import com.cooksys.ftd.ticker.api.StockApi;
import com.cooksys.ftd.ticker.api.StockUtils;
import pl.zankowski.iextrading4j.api.stocks.Quote;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

public class TickerServer {

    public static void main(String[] args) {
    	System.out.println("--Server--");

        try {
            JAXBContext context = JAXBContext.newInstance(QuoteField.class, QuoteRequest.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            // Start serverSocket and listen for connections
            ServerSocket server = new ServerSocket(3000);
            Socket client = server.accept();

            // Create buffered reader and string reader to read a request from a client socket inputstream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            StringReader stringReader = new StringReader(bufferedReader.readLine());

            // Unmarshall stringReader to QuoteRequest object
            QuoteRequest quoteRequest = (QuoteRequest) unmarshaller.unmarshal(stringReader);

            // Fetch quotes for each
            Set<Quote> quotes = StockApi.fetchQuotes(quoteRequest);

            // Map quote results to a formatted string
            String stringQuote = StockUtils.quotesToString(quotes, quoteRequest.getFields());

            // Write and flush formatted string to client socket
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            out.write(stringQuote);
            out.flush();


        } catch (IOException | JAXBException e) {
            e.printStackTrace();
        }

    }


}
