package com.citrix.analyzerservice.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Config {

	private static final Logger logger = Logger.getLogger(Config.class);
	Map<String, String> configs = new HashMap<String, String>();
	InputStream inputStream;
 
	public Map<String, String> getPropValues() {
 
		String propFileName = "config.properties";
		
		try {
			Properties prop = new Properties(); 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				logger.error("Cannot find '" + propFileName + "' configuration file.");
			}
			
			Enumeration<?> e = prop.propertyNames();
			while (e.hasMoreElements()) {
				String key = (String) e.nextElement();
				configs.put(key, prop.getProperty(key));
			}
			
		} catch (Exception e) {
			logger.error("Error in Config class.");
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error("Cannot close " + propFileName + " file.");
			}
		}
		
		return configs;
	}
	
}
