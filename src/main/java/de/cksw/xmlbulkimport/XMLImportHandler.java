package de.cksw.xmlbulkimport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.mongodb.MongoClient;

public class XMLImportHandler extends DefaultHandler {
	
	boolean isHeader = false;
	boolean isNewCatalog = false;
	
    static final String TEXTKEY = "_text";

    List<Document> stack;
    
    private MongoClient mongoClient;
    private CommandLineOptions options;
    private String dbName;
    private Map<String, String> mapping;
    
    public XMLImportHandler(MongoClient mongoClient, CommandLineOptions options) {
    		this.mongoClient = mongoClient;
    		this.options = options;
    		this.dbName = options.getOption("database", String.class);
    		this.mapping = options.getOption("mapping", Map.class);
    		this.stack = new ArrayList<>();
    }

    public String attributeName(String name) {
    		return "@"+name;
    	}
    
    public String tagName(String tag) {
    		return tag.replace(".", "-").toLowerCase();
    }

    public void startDocument () throws SAXException { }

    public void endDocument () throws SAXException { }

    public void startElement (String uri, String localName,String qName, Attributes attributes) throws SAXException {
	
    		Document work = new Document();
    		for (int ix=0;ix<attributes.getLength();ix++) {
    			work.put( attributeName( tagName(attributes.getLocalName(ix)) ), attributes.getValue(ix) );
    		}
	        
	    stack.add(0,work);
    }
    
    public void endElement (String uri, String localName, String qName) throws SAXException {
    		Document pop = stack.remove(0);       // examine stack
        Object stashable = pop;
        if (pop.containsKey(TEXTKEY)) {
            String value = pop.getString(TEXTKEY).trim();
            if (pop.keySet().size()==1) stashable = value; // single value
            else if (StringUtils.isBlank(value)) pop.remove(TEXTKEY);
        }
        Document parent = stack.get(0);
        if (!parent.containsKey(tagName(localName))) {   // add new object
            parent.put( tagName(localName), stashable );
        }
        else {                                  // aggregate into arrays
            Object work = parent.get(tagName(localName));
            if (work instanceof ArrayList) {
                ((ArrayList)work).add(stashable);
            }
            else {
                parent.put(tagName(localName),new ArrayList());
                parent.get(tagName(localName), ArrayList.class).add(work);
                parent.get(tagName(localName), ArrayList.class).add(stashable);
            }
        }
        
        for (String tagName: mapping.keySet()) {
            if (localName.equals(tagName)) {
	    	    		try {
	    	    			mongoClient.getDatabase(dbName).getCollection(mapping.get(tagName)).insertOne(pop);
	    	    		}
	    	    		catch (Exception e) {
	    	    			System.err.println(e.getMessage());
	    	    			e.printStackTrace();
	    	    		}
	    	    		break;
	    	    }
        }
    }
    
    public void characters (char ch[], int start, int length) throws SAXException {
        Document work = stack.get(0);            // aggregate characters
        String value = (work.containsKey(TEXTKEY) ? work.getString(TEXTKEY) : "" );
        work.put(TEXTKEY, value+new String(ch,start,length) );
    }
 
}
