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
	
	public Config() {}
 
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
			
//			checkConfig(configs);
			
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
	
	public void checkConfig() {
		if (configs.isEmpty())
			configs = getPropValues();
		
		String host = configs.get("Host");
		String port = configs.get("Port");
		String dtProcessorExecPeriod = configs.get("DataProcessor_Execution_Period");
		String lines = configs.get("MAX_Stats_Read_Line");
		String directory = configs.get("File_Directory");
		String cacheEnabled = configs.get("Cache_Enable");
		String cacheType = configs.get("Cache_Type");
		String cacheTimeOut = configs.get("Cache_TimeOut");
		String cacheCleanInterval = configs.get("Cache_Clean_Interval");
		String cacheSize = configs.get("Cache_Size");
		
		logger.debug("********** config.properties *********");
		logger.debug("Host :: " + host);
		logger.debug("Port :: " + port);
		logger.debug("DataProcessor_Execution_Period :: " + dtProcessorExecPeriod + "s");
		logger.debug("MAX_Stats_Read_Line :: " + lines);
		logger.debug("File_Directory :: " + directory);		
		logger.debug("Cache_Enable :: " + cacheEnabled);
		logger.debug("Cache_Type :: " + cacheType);
		logger.debug("Cache_TimeOut :: " + cacheTimeOut);
		logger.debug("Cache_Clean_Interval :: " + cacheCleanInterval);
		logger.debug("Cache_Size :: " + cacheSize);
		logger.debug("********** config.properties *********");
		
		if (host.isEmpty())
			logger.error("HOST name needs to be configured in 'config.properties' file.");
		if (lines == null || lines.isEmpty())
			logger.warn("Maximal line number to read file is not configured. Use default value: all.");
		if (directory == null || directory.isEmpty())
			logger.error("File directory needs to be configured in 'config.properties' file.");		
	}
	
}
