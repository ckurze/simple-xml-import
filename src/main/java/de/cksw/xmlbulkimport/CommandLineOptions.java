package de.cksw.xmlbulkimport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

public class CommandLineOptions {
	boolean helpOnly = false;
	private Logger logger; 
	String configFile=null;
	
	HashMap<String,Object> config=new HashMap<String,Object>();
	
	@SuppressWarnings("unchecked")
	public CommandLineOptions(String[] args) throws ParseException {
		logger = LoggerFactory.getLogger(CommandLineOptions.class);
		logger.info("Parsing Command Line");

		CommandLineParser parser = new DefaultParser();

		Options cliopt;
		cliopt = new Options();

		cliopt.addOption("h", "help", false, "Show Help");
		cliopt.addOption("c", "config", true, "config file");
	
		
		CommandLine cmd = parser.parse(cliopt, args);
		
		if (cmd.hasOption("c")) {
			configFile = cmd.getOptionValue("c");
		}
		
		
		if (cmd.hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("Schema Test", cliopt);
			System.exit(0);
		}
	
		if(configFile != null) {
			try {
				String configJson = new String(Files.readAllBytes(Paths.get(configFile)), StandardCharsets.UTF_8);
				Gson gson = new Gson(); 
				config = (HashMap<String,Object>) gson.fromJson(configJson, config.getClass());
			} catch (IOException e) {
				logger.error(e.getMessage());
				System.exit(1);
			}
		} else {
			logger.error("No config file supplied - exiting");
			System.exit(1);
		}
	}
	
	public Object getOption(String what)
	{
		return config.get(what);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getOption(String what, Class<T> type)
	{
		try {
			if (type.getName() == "java.lang.Integer") {
				return (T) new Integer(((Double) config.get(what)).intValue());
			}
			return (T) config.get(what);
		}
		catch (Exception e) {
			logger.error("Could not get command line option '{}' of type {}: {}", what, type.toString(), e.getMessage());
			System.exit(1);
		}
		
		return null;
	}
	
}
