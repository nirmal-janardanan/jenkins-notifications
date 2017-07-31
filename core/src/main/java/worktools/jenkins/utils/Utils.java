package worktools.jenkins.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Utils {

	public static NodeList extractNodes(String data, String xpathExpression) {
		Document document = extractDocument(data);
		return extractNodes(document, xpathExpression);
		
	}

	public static NodeList extractNodes(Document document, String xpathExpression) {
		XPath xPath = XPathFactory.newInstance().newXPath();
	
		try {
			return (NodeList) xPath.compile(xpathExpression).evaluate(document, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	public static Document extractDocument(String data) {
		DocumentBuilder documentBuilder;
		Document document;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = documentBuilder.parse(new ByteArrayInputStream(data.getBytes()));
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
		return document;
	}

	public static String fetchFileData(String file) throws FileNotFoundException, IOException {
		BufferedReader rd = new BufferedReader(new FileReader(file));
		char[] buf = new char[1024 * 256];
		int read = -1;
		StringBuilder result = new StringBuilder();
		while ( (read = rd.read(buf)) > 0) {
			result.append(buf, 0, read);
		}
		
		String response = result.toString();
		return response;
	}

	public static String fetchHttpData(String url) throws MalformedURLException, IOException {
		URL httpsUrl = new URL(url);
		HttpURLConnection urlConnection = (HttpURLConnection) httpsUrl.openConnection();
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
		char[] buf = new char[1024 * 256];
		int read = -1;
		StringBuilder result = new StringBuilder();
		while ( (read = rd.read(buf)) > 0) {
			result.append(buf, 0, read);
		}
		
		String response = result.toString();
		
		
		return response;
	}

	public static void log(String msg) {
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
		System.out.printf("[%s] - (%s) : %s%n", timeStamp, Thread.currentThread().getName(), msg);
	}
	
	

}
