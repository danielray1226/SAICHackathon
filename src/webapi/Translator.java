package webapi;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.google.gson.JsonObject;

public class Translator {
	ReplyData translate(byte[] clientData, String contentType, String path, Map<String, String[]> params) throws Exception {
		System.out.println("Called translator, Content Type:" + contentType + " Path:" + path + " Params:" + params);
		ReplyData reply = new ReplyData(convertXMLToJSON(clientData).getBytes(StandardCharsets.UTF_8), "application/json");
		return reply;
	}

	private String convertXMLToJSON(byte[] xmlData) throws Exception {
		System.out.println("XML Data: " + new String(xmlData, StandardCharsets.UTF_8));
		 SAXReader reader = new SAXReader();
	     Document document = reader.read(new ByteArrayInputStream(xmlData));
	     org.dom4j.Element root = document.getRootElement();
	     JsonObject ret = new JsonObject();
	     for(Element e: root.elements()) {
	    	 String textResult = e.getTextTrim();
	    	 if(!textResult.isEmpty()) {
	    		 ret.addProperty(e.getName(), textResult);
	    	 }
	     }
	    System.out.println("Converted to JSON: " + ret);
		return ret.toString();
	}

}
