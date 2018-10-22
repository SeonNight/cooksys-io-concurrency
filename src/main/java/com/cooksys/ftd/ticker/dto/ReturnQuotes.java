package com.cooksys.ftd.ticker.dto;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "ReturnQuotes")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReturnQuotes {
	@XmlElementWrapper
	@XmlElement(name = "quote")
	private Set<ReturnQuote> quotes;

	public Set<ReturnQuote> getQuotes() {
		return quotes;
	}

	public void setQuotes(Set<ReturnQuote> quotes) {
		this.quotes = quotes;
	}
}
