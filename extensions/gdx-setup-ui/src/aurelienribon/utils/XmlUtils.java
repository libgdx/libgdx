package aurelienribon.utils;

import java.io.StringWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Aurelien Ribon | http://www.aurelienribon.com/
 */
public class XmlUtils {
	private static final Transformer transformer;
	private static final XPath xpath;

	static {
		Transformer tempTransformer = null;

		try {
			tempTransformer = TransformerFactory.newInstance().newTransformer();
			tempTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
			tempTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		} catch (TransformerConfigurationException ex) {
			System.err.println("[error] XmlUtils: Cannot create a Transformer instance.");
		}

		transformer = tempTransformer;

		xpath = XPathFactory.newInstance().newXPath();
	}

	public static DocumentBuilder createParser() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {
			dbf.setNamespaceAware(true);
			dbf.setValidating(false);
			dbf.setFeature("http://xml.org/sax/features/namespaces", false);
			dbf.setFeature("http://xml.org/sax/features/validation", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}

		try {
			return dbf.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			System.err.println("[error] XmlUtils: Cannot create a DocumentBuilder instance.");
		}

		return null;
	}

	public static Object xpath(String expr, Object doc, QName type) {
		try {
			return xpath.compile(expr).evaluate(doc, type);
		} catch (XPathExpressionException ex) {
			return null;
		}
	}

	public static void clean(Document doc) {
		doc.normalize();
		NodeList emptyTextNodes = (NodeList) xpath("//text()[normalize-space(.) = '']", doc, XPathConstants.NODESET);

		for (int i=0; i<emptyTextNodes.getLength(); i++) {
			Node n = emptyTextNodes.item(i);
			n.getParentNode().removeChild(n);
		}
	}

	public static String transform(Document doc) throws TransformerException {
		DOMSource source = new DOMSource(doc);
		StringWriter sw = new StringWriter();
		StreamResult result = new StreamResult(sw);
		transformer.transform(source, result);
		return sw.toString();
	}
}
