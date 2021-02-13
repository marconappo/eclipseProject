package com.nappo.dbo;

import java.util.Objects;

import javax.persistence.*;

import org.hibernate.annotations.NaturalId;

/**
 * Symbol.java
 *
 */

@Entity
@Table(name = "symbol")
public class Symbol {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;

	@NaturalId
	private String cusip;
	private String ticker;
	private String nameOfIssuer;

	public String getNameOfIssuer() {
		return nameOfIssuer;
	}

	public void setNameOfIssuer(String nameOfIssuer) {
		this.nameOfIssuer = nameOfIssuer;
	}

	public Symbol() {
	}

	public Symbol(String ticker, String cusip, String nameOfIssuer) {
		super();
		this.ticker = ticker;
		this.cusip = cusip;
		this.nameOfIssuer = nameOfIssuer;
	}

	public String getTicker() {
		return ticker;
	}

	public void setTicker(String ticker) {
		this.ticker = ticker;
	}

	public String getCusip() {
		return cusip;
	}

	public void setCusip(String cusip) {
		this.cusip = cusip;
	}

	@Override
	public String toString() {
		return cusip+"-"+ticker+"-"+nameOfIssuer;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Symbol)) return false;
        Symbol symbol = (Symbol) o;
        return Objects.equals(getCusip(), symbol.getCusip());
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(getCusip());
    }
}