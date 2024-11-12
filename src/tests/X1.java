package tests;
import org.dom4j.Attribute;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.tree.DefaultText;

import java.io.InputStream;
import java.util.List;
import org.dom4j.Node;


public class X1 {

	static void traverse(int offset, org.dom4j.Element el) {
		print(offset);
		System.out.println("<"+el.getName()+"> path=("+el.getPath()+") uniquePath=("+el.getUniquePath()+")");
		print(offset);
		System.out.println("Text: ["+el.getTextTrim()+"]");
		el.getNodeType();
		List<org.dom4j.Attribute> attrs = el.attributes();
		print(offset);
		for (org.dom4j.Attribute a : attrs) {
			System.out.print(a.getName()+"='"+a.getValue()+"' path=("+a.getPath()+") uniquePath=("+a.getUniquePath()+")");
		}
		System.out.println();
		print(offset);System.out.println("--------------------");
		
		List<org.dom4j.Element> children = el.elements();
		for (org.dom4j.Element c : children) {
			traverse(offset+1,c);
		}
	}	
	
	static void traverse1(int offset, org.dom4j.Element el) {
		print(offset);
		System.out.println("<"+el.getName()+">");
    	//print(offset);
    	//System.out.println(el.getNodeTypeName()+" - "+el.getNodeType());
		print(offset);
		System.out.println("--------------------");
		print(offset);
		for (Attribute a : el.attributes()) {
			System.out.print("("+a.getName()+")=("+a.getValue()+") ");
		}
		System.out.println();
		for (int i = 0, size = el.nodeCount(); i < size; i++) {
	        Node node = el.node(i);

	        print(offset);
	        System.out.println("child: "+node.getNodeTypeName()+" - "+node.getNodeType()+" path=("+node.getPath()+") uniquePath=("+node.getUniquePath()+")");
	        if (node instanceof org.dom4j.Element) {
	        	traverse1(offset+1, (org.dom4j.Element) node);
	        } else if (node instanceof DefaultText) {
	        	print(offset+1);
	        	System.out.println(node.getText());
	        }
		}
	}
	
	
	
	static void print(int offset) {
		for (int i=0;i<offset;++i) System.out.print("\t");
	}
	
	public static void main(String[] args) throws Exception {

		
		InputStream is = X1.class.getClassLoader().getResourceAsStream("tests/e1.xml");
        SAXReader reader = new SAXReader();
        Document document = reader.read(is);
        org.dom4j.Element root = document.getRootElement();
        traverse(0, root);
        
	}

}
