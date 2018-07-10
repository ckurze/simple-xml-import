package de.cksw.xmlbulkimport;

import java.io.File;
import java.util.Map;
import java.util.logging.LogManager;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.xml.sax.XMLReader;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

public class XMLImportBulk {
	
	private Logger logger;
	
	private CommandLineOptions options;
	private MongoClient mongoClient;
	
	public static void main(String[] args) {
		new XMLImportBulk(args);
	}
	
	public XMLImportBulk(String[] args) {
		LogManager.getLogManager().reset();

		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		
		logger = LoggerFactory.getLogger(XMLImportBulk.class);
		logger.info("Starting Bulk Import.");
		
		try {
			options = new CommandLineOptions(args);
		} 
		catch (ParseException e) {
			logger.error("Failed to parse command line options");
			logger.error(e.getMessage());
			System.exit(1);
		}
		
		String fileURL = convertToFileURL(args[0]);
		
		try {
			mongoClient = new MongoClient(new MongoClientURI(options.getOption("mongoURI", String.class)));
			
			dropCollectionsIfNeeded();
			
			SAXParserFactory spf = SAXParserFactory.newInstance();
		    spf.setNamespaceAware(true);
		    SAXParser saxParser = spf.newSAXParser();
		    XMLReader xmlReader = saxParser.getXMLReader();
		    XMLImportHandler xmlImportHandler = new XMLImportHandler(mongoClient, options);
		    xmlReader.setContentHandler(xmlImportHandler);
		    xmlReader.parse(convertToFileURL(options.getOption("xml_file", String.class)));
		    
		    System.out.println("finished");
		}
		catch(Exception e) {
			System.err.println("Error " + e.getMessage());
		}

	}
	
	private void dropCollectionsIfNeeded() {
		if (options.getOption("dropCollections", Boolean.class)) {
			Map<String, String> mapping = options.getOption("mapping", Map.class);
			for (String collections: mapping.values()) {
				try {
					mongoClient.getDatabase(options.getOption("database", String.class)).getCollection(collections).drop();
				}
				catch(Exception e) {
					logger.error(e.getMessage());
				}
			}
		}
	}
	
	private String convertToFileURL(String filename) {
        String path = new File(filename).getAbsolutePath();
        if (File.separatorChar != '/') {
            path = path.replace(File.separatorChar, '/');
        }

        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file:" + path;
    }
}
