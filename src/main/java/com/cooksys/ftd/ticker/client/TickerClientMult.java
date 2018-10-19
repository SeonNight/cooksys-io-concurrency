package com.cooksys.ftd.ticker.client;


public class TickerClientMult {
	public static void main(String[] args) {
    	System.out.println("--ClientMult--");
    	for(int i = 0; i < 1; i++) {
    		new Thread(new TickerClientXml()).start();
    	}
    	System.out.println("--ClientMult End--");
	}
}
