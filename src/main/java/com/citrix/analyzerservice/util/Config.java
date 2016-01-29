package com.citrix.analyzerservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Xi Luo
 *
 */
public class Config {

	private static final Logger logger = Logger.getLogger(Config.class);
	Map<String, String> configs = new HashMap<String, String>();
	InputStream inputStream;
	
	public Config() {}
 
	/**
	* This method is used to get properties from the user configuration file.
	* @return Map: properties of configuration in hashmap
	*/
	public Map<String, String> getPropValues() {
 
		String propFileName = "config.properties";
		
		try {
			Properties prop = new Properties(); 
			inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				logger.error(new StringBuilder("Cannot find '").append(propFileName).append("' configuration file."));
			}
			
			/* read configuration file */
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
				logger.error(new StringBuilder("Cannot close ").append(propFileName).append(" file."));
			}
		}
		
		return configs;
	}
	
	/**
	* This method is used to check the validation of properties in the user configuration file.
	*/
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
		logger.debug(new StringBuilder("Host :: ").append(host));
		logger.debug(new StringBuilder("Port :: ").append(port));
		logger.debug(new StringBuilder("DataProcessor_Execution_Period :: ").append(dtProcessorExecPeriod).append('s'));
		logger.debug(new StringBuilder("MAX_Stats_Read_Line :: ").append(lines));
		logger.debug(new StringBuilder("File_Directory :: ").append(directory));		
		logger.debug(new StringBuilder("Cache_Enable :: ").append(cacheEnabled));
		logger.debug(new StringBuilder("Cache_Type :: ").append(cacheType));
		logger.debug(new StringBuilder("Cache_TimeOut :: ").append(cacheTimeOut));
		logger.debug(new StringBuilder("Cache_Clean_Interval :: ").append(cacheCleanInterval));
		logger.debug(new StringBuilder("Cache_Size :: ").append(cacheSize));
		logger.debug("********** config.properties *********");
		
		if (host.isEmpty())
			logger.error("HOST name needs to be configured in 'config.properties' file.");
		if (lines == null || lines.isEmpty())
			logger.warn("Maximal line number to read file is not configured. Use default value: all.");
		if (directory == null || directory.isEmpty())
			logger.error("File directory needs to be configured in 'config.properties' file.");		
	}
	
}
