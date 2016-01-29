package com.citrix.analyzerservice;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.citrix.analyzerservice.dtprocessor.DtProcessor;
import com.citrix.analyzerservice.model.CacheItem;
import com.citrix.analyzerservice.util.Cache;
import com.citrix.analyzerservice.util.Config;

/**
 * @author Xi Luo
 *
 */
public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	
	private static Config configuration = null;
	public static Map<String, String> configs = null;
	@SuppressWarnings("rawtypes")
	public static Cache<String, CacheItem> cache = null;
	public static boolean cacheIsEnabled = false;
	
	/* Initialize cache */
	static {
		configuration = new Config();
		configuration.checkConfig();
		configs = configuration.getPropValues();
		if (configs.get("Cache_Enable").equalsIgnoreCase("true")) {
			logger.debug("Cache enabled.");
			cacheIsEnabled = true;
			createCache();
		} else {
			logger.debug("Cache NOT enabled.");
		}
	}

	/**
	* This method is used to start the Grizzly server.
	* @param BASE_URI: the root URI of the server
	* @return HttpServer: the Grizzly server
	*/
	private static HttpServer startServer(String BASE_URI) {        
		final ResourceConfig rc = new ResourceConfig().packages("com.citrix.analyzerservice.wshandler");

		return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }
	
	/**
	* This method is used to create the cache object.
	*/
	@SuppressWarnings("rawtypes")
	public static void createCache() {
		cache = new Cache<String, CacheItem>(configs.get("Cache_Type"), parseConfigsTime(configs.get("Cache_TimeOut")), 
				parseConfigsTime(configs.get("Cache_Clean_Interval")), parseConfigsCapacity(configs.get("Cache_Size")));
	}
	
	/**
	* This method is used to parse time in configuration.
	* @param time: the time with unit in String set in user configuration file.
	* @return long: the time in long.
	*/
	public static long parseConfigsTime(String time) {
		if (time.endsWith("s"))
			return (Long.parseLong(time.substring(0, time.length()-1)) * 1000);
		else if (time.endsWith("m"))
			return (Long.parseLong(time.substring(0, time.length()-1)) * 60000);
		else if (time.endsWith("h"))
			return (Long.parseLong(time.substring(0, time.length()-1)) * 3600000);
		else {
			logger.warn(new StringBuilder("Unvalid time configuration. It should end of 's' (second), 'm' (minute) or 'h' (hour), ")
					.append("e.g. 30s, 2m, 1h etc.\nUse default time value: 1 hour."));
			return 3600000;
		}			
	}
	
	/**
	* This method is used to parse cache size in configuration.
	* @param capacity: the cache size with unit in String set in user configuration file.
	* @return int: the cache size in int.
	*/
	public static int parseConfigsCapacity(String capacity) {
		if (capacity.toLowerCase().endsWith("bytes"))
			return (Integer.parseInt(capacity.substring(0, capacity.length()-5)));
		else if (capacity.toLowerCase().endsWith("kb"))
			return (Integer.parseInt(capacity.substring(0, capacity.length()-2)) * 1000);
		else if (capacity.toLowerCase().endsWith("mb"))
			return (Integer.parseInt(capacity.substring(0, capacity.length()-2)) * 1000000);
		else if (capacity.toLowerCase().endsWith("gb"))
			return (Integer.parseInt(capacity.substring(0, capacity.length()-2)) * 1000000000);
		else {
			logger.warn(new StringBuilder("Unvalid cache size configuration. It should end of 'Bytes', 'KB', 'MB' or 'GB', e.g. ")
					.append("80000000Bytes, 600000KB, 300MB, 1GB etc.\nUse default capacity value: 1 GB."));
			return 1000000000;
		}			
	}

	/**
	* This is the main method which manages the server start and termination, 
	* and invokes background processor periodically.
	* @exception IOException On input error.
	*/
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		
		/* get configuration */
		String host = configs.get("Host");
		String port = configs.get("Port");
		String dtProcessorExecPeriod = configs.get("DataProcessor_Execution_Period");
		StringBuilder BASE_URI = new StringBuilder("http://").append(host).append(':').append(port).append("/AIRAnalyzerService");
    	
		/* start server */
		final HttpServer server = startServer(BASE_URI.toString());
		logger.info(new StringBuilder("AIRAnalyzerService started at ").append(BASE_URI).append("\nHit enter to stop..."));
		        
		/* run DtProcessor periodically */
		long execPeriod = parseConfigsTime(dtProcessorExecPeriod);
		logger.info(new StringBuilder("Config: Data processor executes every ").append(Long.toString(execPeriod/1000)).append(" seconds."));
		
		Timer timer = new Timer();
		timer.schedule(new DtProcessor(), 0, execPeriod);
        
		/* stop server */
		System.in.read();
		logger.info("AIRAnalyzerService stopped.");
        server.stop();
    }

}
