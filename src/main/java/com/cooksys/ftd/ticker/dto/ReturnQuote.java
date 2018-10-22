package com.cooksys.ftd.ticker.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReturnQuote {
	@XmlElement(name = "symbol")
	private String symbol;

	@XmlElement(name = "fields")
	private ReturnQuoteField fields;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public ReturnQuoteField getFields() {
		return fields;
	}

	public void setFields(ReturnQuoteField fields) {
		this.fields = fields;
	}
}
