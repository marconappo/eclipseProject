package com.nappo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.exception.ConstraintViolationException;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.nappo.dbo.HedgeFund;
import com.nappo.dbo.Stock;
import com.nappo.dbo.Symbol;

import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;

//https://quotes.fidelity.com/mmnet/SymLookup.phtml?reqforlookup=REQUESTFORLOOKUP&productid=mmnet&isLoggedIn=mmnet&rows=50&for=stock&by=cusip&criteria=002824100&submit=Search
//=C2/SUMIF($A$2:$C$929,A2,$C$2:$C$929)
// SELECT symbol.cusip, ticker, symbol.nameOfIssuer, stock.value, periodOfReport FROM stock INNER JOIN symbol ON stock.cusip = symbol.cusip;

public class Test {
	static int BUFFER_SIZE = 4096;
	static String URL_PREFIX = "https://www.sec.gov/Archives/edgar/data/";
	private static DBManager dbManager = new DBManager();
	// static String CIK_NUMBER = "1697748"; // ARK INVEST
	// static String CIK_NUMBER = "1350694"; // RAY DALIO
	static String CIK_NUMBER = "1067983"; // WARREN BUFFET
	// static String CIK_NUMBER = "1135730"; // Philippe Laffont Coatue Management,
	// static String CIK_NUMBER = "1541617"; // GERSTNER BRAD Altimeter
	private static String PC_FOLDER = "C:\\Users\\Marco'PC\\git\\eclipseProject\\testHibernate-lib\\src\\main\\resources\\"
			+ CIK_NUMBER + "\\";

	public static void main(String[] args) throws IOException, ParseException {
		dbManager.setup();
		// downloadFilesAndPopulateHedgeFunds();
		// populateSymbols();
		//populateStocks();
		List<Symbol> symbolList = dbManager.getAllSymbols();
		for (Symbol symbol: symbolList.subList(0, 3)) {
			getStockYearsByWeek(symbol.getTicker(), 1);
		}
		dbManager.exit();
	}

	static void getStockYearsByWeek(String ticker, int year) throws IOException {
		// https://financequotes-api.com/
		Calendar from = Calendar.getInstance();
		Calendar to = Calendar.getInstance();
		from.add(Calendar.YEAR, -year); // from year years ago
		yahoofinance.Stock stock = YahooFinance.get(ticker, from, to, Interval.WEEKLY);
		List<HistoricalQuote> historicalQuoteList = stock.getHistory();
		for (HistoricalQuote historicalQuote: historicalQuoteList) {
			Date date = historicalQuote.getDate().getTime();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			System.out.println("ticker: "+ticker+" price:"+ historicalQuote.getClose() + "-" + dateFormat.format(date));
		}
		

	}

	static BigDecimal getStockQuote(String ticker, int year, int month, int day, int counter) {
		// https://financequotes-api.com/
		Calendar from, to;
		BigDecimal returnValue = null;
		try {
			from = new GregorianCalendar(year, month - 1, day - 1);
			to = new GregorianCalendar(year, month - 1, day + 3);
			returnValue = YahooFinance.get(ticker, from, to, Interval.DAILY).getHistory().get(0).getOpen();
		} catch (IOException e) {
			if (counter < 4) {
				return getStockQuote(ticker, year, month, day + 1, counter + 1);
			}
		}
		return returnValue;
	}

	/**
	 * @param ticker
	 * @param quarter
	 * @param year
	 * @return
	 * @throws IOException
	 */
	static BigDecimal getChangePercent(String ticker, int quarter, int year) throws IOException {
		// https://financequotes-api.com/
		BigDecimal open = getStockQuote(ticker, year, (quarter - 1) * 3 + 1, 1, 0);
		BigDecimal close = getStockQuote(ticker, year, (quarter - 1) * 3 + 4, 1, 0);
		BigDecimal changeInPercent = null;
		if (open != null && close != null) {
			changeInPercent = (close.subtract(open)).divide(open, 5, RoundingMode.HALF_EVEN)
					.multiply(new BigDecimal(100));
		}
		return changeInPercent;

	}

	static BigDecimal getChangePercent45(String ticker, int quarter, int year) throws IOException {
		// https://financequotes-api.com/
		BigDecimal open = getStockQuote(ticker, year, (quarter - 1) * 3 + 2, 16, 0);
		BigDecimal close = getStockQuote(ticker, year, (quarter - 1) * 3 + 4 + 1, 16, 0);
		BigDecimal changeInPercent = (close.subtract(open)).divide(open, 5, RoundingMode.HALF_EVEN)
				.multiply(new BigDecimal(100));
		return changeInPercent;

	}

	static void convertTxtInXml(File txtFile, File xmlPrimary, File xmlTable) throws IOException {
		Document doc;
		doc = Jsoup.parse(txtFile, "UTF-8");
		List<Element> elementList = doc.getElementsByTag("TEXT");
		if (elementList.size() > 1) {
			FileUtils.writeStringToFile(xmlPrimary, elementList.get(0).html(), Charset.defaultCharset(), false);
			FileUtils.writeStringToFile(xmlTable, elementList.get(1).html(), Charset.defaultCharset(), false);
		}
	}

	static void populateStocks() throws ParseException {
		List<String> folderNameList = dbManager.getFolders(CIK_NUMBER);
		for (String folderName : folderNameList.subList(0, 100)) {
			System.out.println("----------------------" + folderName + "----------------------");
			try {
				File input = new File(PC_FOLDER + folderName + "_primary.xml");
				String date = getDateFromXML(input);
				if (date.isEmpty())
					continue;
				String[] tagList = { "nameOfIssuer", "cusip", "value" };
				Document doc;
				input = new File(PC_FOLDER + folderName + "_table.xml");
				doc = Jsoup.parse(input, "UTF-8");
				List<Element> nameList = doc.getElementsByTag(tagList[0]);
				List<Element> cusipList = doc.getElementsByTag(tagList[1]);
				List<Element> valueList = doc.getElementsByTag(tagList[2]);
				for (int i = 0; i < cusipList.size(); i++) {
					String cusip = cusipList.get(i).text();
					Date periodOfReport = new SimpleDateFormat("MM-dd-yyyy").parse(date);
					String name = nameList.get(i).text();
					String value = valueList.get(i).text();
					Stock stock = new Stock(name, cusip, Float.parseFloat(value), periodOfReport, CIK_NUMBER);
					dbManager.createStock(stock);
				}
			} catch (IOException e) {
			}
		}
	}

	static boolean fileContainsWord(File fileToSave, String input) {
		boolean contains = false;
		Scanner words;
		try {
			words = new Scanner(fileToSave);
			while (words.hasNext() && !contains) {
				if (words.next().contains(input)) {
					contains = true;
					break;
				}
			}
			words.close();
			if (contains)
				System.out.println("It is a 13F File: " + fileToSave.getAbsolutePath());
		} catch (FileNotFoundException e) {
			System.out.println("FILE NOT FOUND: " + fileToSave.getAbsolutePath());
		}
		return contains;
	}

	static List<String> getFolderNameList(String CIK_NUMBER, String PC_FOLDER) throws IOException {
		Response response = Jsoup.connect("https://www.sec.gov/Archives/edgar/data/" + CIK_NUMBER + "/")
				.userAgent("Mozilla/5.0").timeout(10 * 1000).followRedirects(true).execute();
		Document doc = response.parse();
		List<Element> elementList = doc.getElementsByAttributeValueContaining("href", "/Archives/edgar/data/"); /// webxpress/get_quote?QUOTE_TYPE=&SID_VALUE_ID=ATVI
		List<String> folderNameList = new ArrayList<>();
		for (int i = 0; i < elementList.size(); i++) {
			folderNameList.add(elementList.get(i).text());
		}
		return folderNameList;

	}

	static void downloadFilesAndPopulateHedgeFunds() throws IOException, ParseException {
		List<String> folderNameList = getFolderNameList(CIK_NUMBER, PC_FOLDER);
		for (String folderName : folderNameList) {
			String fileURL = URL_PREFIX + CIK_NUMBER + "/" + getTxtFileNameFromFolderName(folderName);
			String fileName = PC_FOLDER + File.separator + getTxtFileNameFromFolderName(folderName);
			File txtFile = new File(fileName);
			if (downloadFile(CIK_NUMBER, folderName, fileURL, fileName, txtFile, PC_FOLDER)) {
				if (fileURL.contains("txt") && !fileContainsWord(txtFile, "13F"))
					txtFile.delete();
				else {
					File xmlPrimary = new File(PC_FOLDER + folderName + "_primary.xml");
					File xmlTable = new File(PC_FOLDER + folderName + "_table.xml");
					convertTxtInXml(txtFile, xmlPrimary, xmlTable);
					dbManager.createHedgeFund(new HedgeFund(CIK_NUMBER, folderName));
				}
			}
		}
		/*
		 * String fileName = PC_FOLDER + File.separator + folderName + "_table.xml";
		 * String fileURL = URL_PREFIX + CIK_NUMBER + "/" + folderName +
		 * "/form13fInfoTable.xml"; boolean success = true; if (downloadFile(CIK_NUMBER,
		 * folderName ,fileURL, fileName, hedgeFundList)) { fileName = PC_FOLDER +
		 * File.separator + folderName + "_primary.xml"; fileURL = URL_PREFIX +
		 * CIK_NUMBER + "/" + folderName + "/primary_doc.xml"; success =
		 * downloadFile(CIK_NUMBER, folderName ,fileURL, fileName, hedgeFundList); }
		 * else {
		 */
		// }
	}

	public static String getTxtFileNameFromFolderName(String folderName) {
		return folderName.substring(0, 10) + "-" + folderName.substring(10, 12) + "-"
				+ folderName.substring(12, folderName.length()) + ".txt";

	}

	public static boolean downloadFile(String CIK_NUMBER, String folderName, String fileURL, String fileName,
			File txtFile, String PC_FOLDER) throws IOException {
		boolean success = true;
		if (!txtFile.exists()) {
			HttpURLConnection httpConn = (HttpURLConnection) new URL(fileURL).openConnection();
			if (httpConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				saveFileOnDisk(httpConn.getInputStream(), new FileOutputStream(fileName));
			} else {
				success = false;
			}
			httpConn.disconnect();
		} else {
			System.out.println("File already exist: " + fileName);
			success = false;
		}
		return success;
	}

	private static void saveFileOnDisk(InputStream inputStream, FileOutputStream outputStream) throws IOException {
		int bytesRead = -1;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((bytesRead = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, bytesRead);
		}
		outputStream.close();
		inputStream.close();
	}

	static void populateSymbols() throws ParseException {
		List<String> folderNameList = dbManager.getFolders(CIK_NUMBER);
		for (String folderName : folderNameList) {
			System.out.println("----------------------" + folderName + "----");
			Document doc;
			File input = new File(PC_FOLDER + folderName + "_table.xml");
			try {
				doc = Jsoup.parse(input, "UTF-8");
				List<Element> nameList = doc.getElementsByTag("nameOfIssuer");
				List<Element> cusipList = doc.getElementsByTag("cusip");
				for (int i = 0; i < nameList.size(); i++) {
					String cusip = cusipList.get(i).text();
					if (!dbManager.existSymbol(cusip)) {
						String ticker = getSymbolFromCusip(cusip);
						String name = nameList.get(i).text();
						Symbol symbol = new Symbol(ticker, cusip, name);
						dbManager.createSymbol(symbol);
					}
				}
			} catch (IOException e) {
				// e.printStackTrace();
			}
		}
	}

	static String getSymbolFromCusip(String cusip) throws IOException {
		Response response = Jsoup.connect("http://quotes.fidelity.com/mmnet/SymLookup.phtml").userAgent("Mozilla/5.0")
				.timeout(10 * 1000).method(Method.GET).data("reqforlookup", "REQUESTFORLOOKUP")
				.data("productid", "mmnet").data("isLoggedIn", "mmnet").data("rows", "50").data("for", "stock")
				.data("by", "cusip").data("rows", "50").data("criteria", cusip).data("submit", "Search")
				.followRedirects(true).execute();

		Document doc = response.parse();
		List<Element> symbolList = doc.getElementsByAttributeValueContaining("href", "SID_VALUE_ID");
		if (symbolList.size() > 0)
			return symbolList.get(0).text();
		else
			return "N/A";

	}

	static String getDateFromXML(File input) throws IOException {
		Document doc;
		String returnString = "";
		doc = Jsoup.parse(input, "UTF-8");
		if (doc.getElementsByTag("periodOfReport").size() > 0)
			returnString = doc.getElementsByTag("periodOfReport").get(0).text();
		return returnString;
	}

}

/*
 * import java.sql.Connection; import java.sql.DriverManager; import
 * java.sql.PreparedStatement; import java.sql.ResultSet; import
 * java.sql.SQLException;
 */

/*
 * String sqlSelectAllPersons = "SELECT * FROM person"; String connectionUrl =
 * "jdbc:mysql://35.187.72.119:3306/stockdb_schema?serverTimezone=UTC";
 * 
 * try (Connection conn = DriverManager.getConnection(connectionUrl, "root",
 * "Pa$$w0rd"); PreparedStatement ps =
 * conn.prepareStatement(sqlSelectAllPersons); ResultSet rs = ps.executeQuery())
 * { System.out.println("Successfully connected");
 * 
 * while (rs.next()) { long id = rs.getLong("ID"); String name =
 * rs.getString("FIRST_NAME"); String lastName = rs.getString("LAST_NAME");
 * System.out.println(id+" "+name+" "+lastName); } } catch (SQLException e) {
 * System.out.println(e.getMessage()); }
 */