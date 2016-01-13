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

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	
	// Cache configurations
	private static Config configuration = null;
	public static Map<String, String> configs = null;
	@SuppressWarnings("rawtypes")
	public static Cache<String, CacheItem> cache = null;
	public static boolean cacheIsEnabled = false;
	
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

	private static HttpServer startServer(String uri) {        
		final ResourceConfig rc = new ResourceConfig().packages("com.citrix.analyzerservice.wshandler");

		return GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
    }
	
	@SuppressWarnings("rawtypes")
	public static void createCache() {
		cache = new Cache<String, CacheItem>(configs.get("Cache_Type"), parseConfigsTime(configs.get("Cache_TimeOut")), 
				parseConfigsTime(configs.get("Cache_Clean_Interval")), Integer.parseInt(configs.get("Cache_Size")));
	}
	
	public static long parseConfigsTime(String time) {
		if (time.endsWith("s"))
			return (Long.parseLong(time.substring(0, time.length()-1)) * 1000);
		else if (time.endsWith("m"))
			return (Long.parseLong(time.substring(0, time.length()-1)) * 60000);
		else if (time.endsWith("h"))
			return (Long.parseLong(time.substring(0, time.length()-1)) * 3600000);
		else {
			logger.warn("Unvalid time configuration. Time in configuration should end of 's' (second), "
					+ "'m' (minute) or 'h' (hour), e.g. 30s, 2m, 1h etc.\nUse default time value: 1 hour.");
			return 3600000;
		}
			
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		
		// get configuration
		String host = configs.get("Host");
		String port = configs.get("Port");
		String dtProcessorExecPeriod = configs.get("DataProcessor_Execution_Period");
		String BASE_URI = "http://" + host + ":" + port + "/AIRAnalyzerService"; // Base URI for Grizzly HTTP server to listen on
    	
		// start server
		final HttpServer server = startServer(BASE_URI);
		logger.info(String.format("AIRAnalyzerService started at %s\nHit enter to stop...", BASE_URI));
		        
		// run DtProcessor in background in periodically
		long execPeriod = parseConfigsTime(dtProcessorExecPeriod);
		logger.info("Config: Data processor executes every " + execPeriod/1000 + " seconds.");
		
		Timer timer = new Timer();
		timer.schedule(new DtProcessor(), 0, execPeriod);
        
		// stop server
		System.in.read();
		logger.info("AIRAnalyzerService stopped.");
        server.stop();
    }

}
