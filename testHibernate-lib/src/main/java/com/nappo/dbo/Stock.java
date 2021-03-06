package com.nappo.dbo;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

/**
 * Stock.java
 *
 */
 
@Entity
@IdClass(StockID.class)
@Table(name = "stock")
public class Stock implements Serializable{
    private long id;
    private String nameOfIssuer;
    @Id
    private String cusip;
    private float value;
    @Id
    private Date periodOfReport;
    @Id
    private String cikNumber;
    
    
    
 
    public Stock() {
    }

	public Stock(String nameOfIssuer, String cusip, float value, Date periodOfReport, String cikNumber) {
		super();
		this.nameOfIssuer = nameOfIssuer;
		this.cusip = cusip;
		this.value = value;
		this.periodOfReport = periodOfReport;
		this.cikNumber = cikNumber;
	}

	@Id
    @Column(name = "stock_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }
 
    public void setId(long id) {
        this.id = id;
    }

	public String getNameOfIssuer() {
		return nameOfIssuer;
	}

	public void setNameOfIssuer(String nameOfIssuer) {
		this.nameOfIssuer = nameOfIssuer;
	}

	public String getCusip() {
		return cusip;
	}

	public void setCusip(String cusip) {
		this.cusip = cusip;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}




	public Date getPeriodOfReport() {
		return periodOfReport;
	}




	public void setPeriodOfReport(Date periodOfReport) {
		this.periodOfReport = periodOfReport;
	}


	public String getCikNumber() {
		return cikNumber;
	}

	public void setCikNumber(String cikNumber) {
		this.cikNumber = cikNumber;
	}
 
 
 
}