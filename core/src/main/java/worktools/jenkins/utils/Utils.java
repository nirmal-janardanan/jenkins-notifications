package worktools.jenkins.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Utils {
	
	private static final ThreadLocal<XPath> xpathCacheFactory = new ThreadLocal<XPath>(){
		
		protected XPath initialValue() {
			return XPathFactory.newInstance().newXPath();
		};
	};
	
	private static final ThreadLocal<Map<String, XPathExpression>> xpathExpressionCacheFactory = new ThreadLocal<Map<String, XPathExpression>>() {
		protected java.util.Map<String,XPathExpression> initialValue() {
			return new HashMap<>();
		}
		
	};

	public static NodeList extractNodes(String data, String xpathExpression) {
		Document document = extractDocument(data);
		NodeList nodes = extractNodes(document, xpathExpression);
		return nodes;
	}

	public static NodeList extractNodes(Document document, String xpathExpression) {
		try {
			long start = System.currentTimeMillis();
			XPathExpression xpathExpressionBean = getCachedXpathExpression(xpathExpression);
			NodeList nodes = (NodeList) xpathExpressionBean.evaluate(document, XPathConstants.NODESET);
//			Utils.logTimeTaken("extractNodes()", start);
			return nodes;
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	private static XPathExpression getCachedXpathExpression(String xpathExpression) throws XPathExpressionException {
		Map<String, XPathExpression> cachedExpressions = xpathExpressionCacheFactory.get();
		XPathExpression xpathExpressionBean = cachedExpressions.get(xpathExpression);
		if(xpathExpressionBean == null) {
			xpathExpressionBean = xpathCacheFactory.get().compile(xpathExpression);
			cachedExpressions.put(xpathExpression, xpathExpressionBean);
		}
		//computeIfAbsent() would have worked but for the exception :(
		return xpathExpressionBean;
	}

	public static Document extractDocument(String data) {
		DocumentBuilder documentBuilder;
		Document document;
		try {
			long start = System.currentTimeMillis();
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = documentBuilder.parse(new ByteArrayInputStream(data.getBytes()));
//			Utils.logTimeTaken("extractDocument()", start);
		} catch (IOException | ParserConfigurationException | SAXException e) {
			throw new RuntimeException(e);
		}
		return document;
	}
	
	public static Document cloneDocument(Document originalDocument) {
		Document copiedDocument = null;
		DocumentBuilder documentBuilder;
		try {
			long start = System.currentTimeMillis();
			documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Node originalRoot = originalDocument.getDocumentElement();

			copiedDocument = documentBuilder.newDocument();
			Node copiedRoot = copiedDocument.importNode(originalRoot, true);
			copiedDocument.appendChild(copiedRoot);
//			Utils.logTimeTaken("cloning()", start);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return copiedDocument ;
	}

	public static String fetchClasspathFileData(String file) throws FileNotFoundException, IOException, URISyntaxException {
		URI uri = Thread.currentThread().getContextClassLoader().getResource(file).toURI();
		byte[] bytes = Files.readAllBytes(Paths.get(uri));
		return new String(bytes);
	}

	public static String fetchHttpData(String url) throws MalformedURLException, IOException {
		Utils.log("Fetching url: " + url);
		long start = System.currentTimeMillis();
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
		
		Utils.logTimeTaken("Fetching url", start);
		return response;
	}

	public static void log(String msg, Object... params) {
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
		String formattedMsg = String.format(msg, params);
		System.out.printf("[%s] - (%s) : %s%n", timeStamp, Thread.currentThread().getName(), formattedMsg);
	}
	
	public static void logTimeTaken(String msg, long start, Object... params) {
				 
		String timeStamp = new SimpleDateFormat("HH:mm:ss").format(new Date());
		String formattedMsg = String.format(msg, params);
		System.out.printf("[%s] - (%s) : %s . Time = %dms%n", timeStamp, Thread.currentThread().getName(), formattedMsg, (System.currentTimeMillis() - start));
	}
	
	

}
