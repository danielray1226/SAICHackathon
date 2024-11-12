package tests;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import utils.JsonUtils;

public class RuleNode {
	
	/*
	 * /a/b/c			<a><b><c>...  <-- points to c  .... </c></b><a> 
	 * /a/b/c/d/@id		<a><b><c><d id="123">  <-- points to attribute 'id' in <d>  .... </d></c></b><a>
	 * /a/b/c/d/*		<a><b><c><d>hello</d></c></b></a>   points to text "hello"
	 */
	String xmlPath;
	String jsonPropertyName;
	JsonUtils.JsonType jsonPropertyType;
	List<RuleNode> children=new ArrayList<>();
	boolean isValueRule=false;
	boolean isAttributeRule=false;
	boolean isElementRule=false;
	String elementName;
	String attributeName;
	
	private void setChildren(List<RuleNode> children) {
		this.children=children;
	}
	public List<RuleNode> getChildren() {
		return children;
	}
	public String getXmlPath() {
		return xmlPath;
	}
	
	RuleNode(String xmlPath, JsonObject data) {
		this.xmlPath=xmlPath;
		jsonPropertyName=JsonUtils.getString(data, "jsonProperty");
		String strType=JsonUtils.getString(data, "jsonPropertyType");
		jsonPropertyType=JsonUtils.JsonType.valueOf(strType);
		
		String[] split=xmlPath.split("\\/");
		String last=split[split.length-1];
		isValueRule=last.equals("*");
		isAttributeRule=last.startsWith("@");
		if (isAttributeRule) {
			attributeName = last.substring(1);
		}
		if (!isAttributeRule && !isValueRule) {
			isElementRule=true;
			elementName=last;
		} else {
			// split for /@id will be ["","@id], no element here
			// split for /a/@id will be ["","a","@id], length is 3, "a" is the element
			if (split.length<3) throw new RuntimeException("Invalid "+xmlPath+"path");
			elementName=split[split.length-2];
		}
	}
	public String getJsonPropertyName() {
		return jsonPropertyName;
	}
	public JsonUtils.JsonType getJsonPropertyType() {return jsonPropertyType;}
	public boolean isValueRule() {
		return isValueRule;
	}
	public boolean isAttributeRule() {
		return isAttributeRule;
	}
	public boolean isElementRule() {
		return isElementRule;
	}
	public String getElementName() {
		return elementName;
	}	
	public String getAttributeName() {
		return attributeName;
	}


	@Override
	public String toString() {
		return "RuleNode [xmlPath=" + xmlPath + ", jsonPropertyName=" + jsonPropertyName + ", jsonPropertyType="
				+ jsonPropertyType + ", elementName=" + elementName + ", attributeName=" + attributeName+ "]";
	}
	
	public static void main (String [] args) {
		InputStream is = RuleNode.class.getClassLoader().getResourceAsStream("tests/rules.json");
		JsonElement rules = JsonParser.parseReader(new InputStreamReader(is));
		System.out.println(rules);
		JsonObject rulesObject = JsonUtils.getJsonObject(rules, "rules");
		
		List<RuleNode> roots = getRoots(rulesObject);
		System.out.println("Roots: "+roots);
	}
	

	public static List<RuleNode> getRoots(JsonObject rulesObject) {		
		if (rulesObject==null) return Collections.emptyList();
		
		Map<String, RuleNode> pathToNode=new HashMap<>();
		Map<String, List<RuleNode>> parentPathToNode=new HashMap<>();
		
		
		for (Entry<String, JsonElement> e : rulesObject.entrySet()) {
			String path=e.getKey();
			JsonElement propValue=e.getValue();
			if (!propValue.isJsonObject()) continue;
			RuleNode ruleNode=new RuleNode(path, propValue.getAsJsonObject());
			//System.out.println(path+" -> "+propValue);
			pathToNode.put(path, ruleNode);
			
			String parentPath=JsonUtils.getString(propValue, "parent");
			List<RuleNode> childrenList = parentPathToNode.get(parentPath);
			if (childrenList==null) {
				childrenList=new ArrayList<>();
				parentPathToNode.put(parentPath, childrenList);
			}
			childrenList.add(ruleNode);
		}
		for (Entry<String, RuleNode> e : pathToNode.entrySet()) {
			String path=e.getKey();
			RuleNode ruleNode = e.getValue();
			List<RuleNode> children=parentPathToNode.get(path);
			if (children!=null) ruleNode.setChildren(children);
		}
		List<RuleNode> ret = parentPathToNode.get(null);
		return ret==null?Collections.emptyList():ret;
	}





}
