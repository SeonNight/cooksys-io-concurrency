package com.cooksys.ftd.ticker.client;

//Multiple client threads
public class TickerClientMult {
	public static void main(String[] args) {
		for (int i = 0; i < 10; i++) {
			new Thread(new TickerClientXml()).start();
		}
	}
}
