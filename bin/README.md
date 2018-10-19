# Java Assignment - IO and Concurrency

# Assignment Overview

For this assignment, you will be expanding and enhancing a client/server **Stock Ticker** application. Clients will send the [Ticker Symbol](https://en.wikipedia.org/wiki/Ticker_symbol)s that they want to get quotes on, as well as fields that they may be interested in. Example fields are 'high', 'low', 'latest price', etc. 

### What you are given

*This is a description of what the skeleton **currently** does.*

This assignment comes with a skeleton which contains working client/server applications. Below is a description of what each application does and how they communicate with each other.

When the client application is started, it creates in memory a hard-coded `QuoteRequest` object. This contains the ticker symbols and fields that the client will be requesting. It creates a socket connection, marshals the `QuoteRequest` object to a StringWriter, and then writes the object to the server via a `BufferedWriter`. It then loops indefinitely and reads in string responses from the socket and prints them to the console.

When the server is started, it waits for an incoming client socket connection via `server.accept()`. Once the server recieves a connection, it creates a `BufferedReader` and `StringReader` that it then uses to unmarshall the 'stringified' XML request. Once it has the `QuoteRequest` object, it makes a call to a provided `StockApi.java` method. This class contains a static method that will fetch a `Set` of `Quote` objects from an [open-source stock market API](https://iextrading.com/developer/docs/) using a [3rd party Java wrapper library](https://github.com/WojciechZankowski/iextrading4j) that wraps this API in easy-to-use Java classes and methods. Once the server gets the set of quotes, it then uses the provided `StockUtils` class to format the quotes to a string which it then sends back to the client via a BufferedWriter connected to the client socket.

The server and client both close after a single request is made.

---

# Assignment Requirements

You are tasked with building on top an existing client/server application and configuring it to support concurrent requests. The DTO's will need to be expanded to allow the client to provide an 'interval' (1-10 seconds) which the server will use in order to continue sending real-time quotes back to the client application. You will also be enhancing the server response to return XML as a response. The client will then save the XML response to an XML file on the client machine.

You can change the hard-coded values on the client side to get more example data. The `Additional Notes` section contains more information regarding these values and contains an extra-credit challenge to make the application more flexible.

The server should accept client requests and create a new thread to manage each client's socket connection and provide the client with quotes at their requested interval. The server will use the `interval` to determine how frequently to fetch quotes from the provided API and respond to the client. The thread serving the client with responses should remain open until either the client closes their application or the server is forcibly stopped.

The server should remain open to new requests until the server is forcibly stopped.

*Due to latency when communicating with Stock API, the timing for the interval does not have have extreme precision*.

The response from the server should be changed from a formatted string to a marshalled XML file. The required format of this response is shown below.

#### Example XML response

*The stock market is constantly changing, so there may be differences in values*

```xml
<quotes>
    <quote>
        <symbol>TWTR</symbol>
        <fields>
            <high>29.79</high>
            <latestPrice>29.38</latestPrice>
        </fields>
    </quote>
    <quote>
        <symbol>FB</symbol>
        <fields>
            <high>155.54</high>
            <latestPrice>155.07</latestPrice>
        </fields>
    </quote>
</quotes>
```

Once the server sends the XML response to the client, the client application should save the XML file to the client's machine. The location of where this file is stored is up to you. Each time the client gets a response, it should overwrite the existing file.

## Additional Information

* The client's request data is hard-coded and does not need to be changed. For 'extra-credit', you can implement functionality that allows this information to be read in from an XML file (easy) OR read in from the command-line (hard). It is not required that it changed from what is given in the skeleton.

* The available fields have already been included in the project skeleton inside of `QuoteField.java` - a DTO [enum](https://docs.oracle.com/javase/tutorial/java/javaOO/enum.html) annoted with JAXB annotations.

* The client will be passing a single set of fields to view. They should not be able to view different fields for different ticker symbols.

