package com.nappo.dbo;
import java.util.Date;

import javax.persistence.*;

/**
 * Stock.java
 *
 */
 
@Entity
@Table(name = "hedgefund")
public class HedgeFund {
    private long id;
    private String cikNumber;
    private String folderName;
    
    
    
 
    public HedgeFund() {
    }


	@Id
    @Column(name = "hedgefund_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return id;
    }
 
    public void setId(long id) {
        this.id = id;
    }


	public String getCikNumber() {
		return cikNumber;
	}

	public void setCikNumber(String cikNumber) {
		this.cikNumber = cikNumber;
	}


	public String getFolderName() {
		return folderName;
	}


	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}


	public HedgeFund(String cikNumber, String folderName) {
		super();
		this.cikNumber = cikNumber;
		this.folderName = folderName;
	}
 
 
 
}