package com.nappo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

public class DBManager {
	protected SessionFactory sessionFactory;
	protected Session session;

	protected void setup() {
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure() // configures settings
																									// from
																									// hibernate.cfg.xml
				.build();
		try {
			sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
			session = sessionFactory.openSession();
		} catch (Exception ex) {
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
			session.save(stock);
		}
		session.getTransaction().commit();
	}

	protected void createStock(Stock stock) {
		session.beginTransaction();
		session.save(stock);
		session.getTransaction().commit();
	}

	protected void createHedgeFundList(List<HedgeFund> hedgeFundList) {
		session.beginTransaction();
		for (HedgeFund hedgeFund : hedgeFundList) {
			session.save(hedgeFund);
		}
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

	protected void createSymbolList(Set<Symbol> symbolList) {
		session.beginTransaction();
		for (Symbol symbol : symbolList) {
			session.saveOrUpdate(symbol);
		}
		session.getTransaction().commit();
	}

	protected boolean existSymbol(String cusip) {
		Query query = session.createQuery("FROM Symbol WHERE cusip=:cusip");
		query.setParameter("cusip", cusip);
		return (query.list().size() > 0);
	}

}