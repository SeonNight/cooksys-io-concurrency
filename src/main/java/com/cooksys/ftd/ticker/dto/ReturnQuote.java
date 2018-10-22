package com.cooksys.ftd.ticker.dto;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ReturnQuote {
	@XmlElement(name = "symbol")
	private String symbol;

	@XmlElementWrapper
	private Set<ReturnQuoteField> fields;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public Set<ReturnQuoteField> getFields() {
		return fields;
	}

	public void setFields(Set<ReturnQuoteField> fields) {
		this.fields = fields;
	}
}
