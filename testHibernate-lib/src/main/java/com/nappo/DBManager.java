package com.nappo;

import java.sql.SQLIntegrityConstraintViolationException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.NonUniqueObjectException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;

import com.nappo.dbo.HedgeFund;
import com.nappo.dbo.Stock;
import com.nappo.dbo.Symbol;

public class DBManager {
	protected SessionFactory sessionFactory;
	protected Session session;

	protected void setup() {
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure() // configures settings
				.build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
			session = sessionFactory.openSession();
		} catch (Exception ex) {
			ex.printStackTrace();
			StandardServiceRegistryBuilder.destroy(registry);
		}
	}

	protected void exit() {
		session.close();
		sessionFactory.close();
	}

	protected void createStockList(List<Stock> stockList) {
		session.beginTransaction();
		for (Stock stock : stockList) {
			session.saveOrUpdate(stock);
		}
		session.getTransaction().commit();
	}

	protected void createStock(Stock stock) throws ParseException {
		if (!existStock(stock.getCusip(), stock.getPeriodOfReport(), stock.getCikNumber())){
			session.beginTransaction();
			session.saveOrUpdate(stock);
			session.getTransaction().commit();
		}
	}

	protected void createHedgeFund(HedgeFund hedgeFund) {
		session.beginTransaction();
		session.saveOrUpdate(hedgeFund);
		session.getTransaction().commit();
	}

	protected List<String> getFolders(String cikNumber) {
		List<String> folderList = new ArrayList<String>();
		Query query = session.createQuery("Select folderName FROM HedgeFund WHERE cikNumber=:cikNumber");
		query.setParameter("cikNumber", cikNumber);
		for (Object folderName : query.list()) {
			folderList.add(folderName.toString());
		}
		return folderList;

	}

	protected boolean existStock(String cusip, Date periodOfReport, String cikNumber) throws ParseException {
		Query query = session.createQuery(
				"FROM Stock WHERE cusip=:cusip and periodOfReport=:periodOfReport and cikNumber=:cikNumber");
		query.setParameter("cikNumber", cikNumber);
		query.setParameter("cusip", cusip);
		query.setParameter("periodOfReport", periodOfReport);

		return !query.list().isEmpty();

	}

	protected void createSymbol(Symbol symbol) {
		session.beginTransaction();
		session.saveOrUpdate(symbol);
		session.getTransaction().commit();
	}

	protected boolean existSymbol(String cusip) {
		Query query = session.createQuery("FROM Symbol WHERE cusip=:cusip");
		query.setParameter("cusip", cusip);
		return (query.list().size() > 0);
	}
	protected List<Symbol> getAllSymbols() {
		  return (List<Symbol>) session.createQuery("from Symbol").list();
	}

}