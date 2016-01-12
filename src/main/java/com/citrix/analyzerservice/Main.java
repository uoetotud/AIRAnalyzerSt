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
		cache = new Cache<String, CacheItem>(configs.get("Cache_Type"), Long.parseLong(configs.get("Cache_TimeOut")), 
				Long.parseLong(configs.get("Cache_Clean_Interval")), Integer.parseInt(configs.get("Cache_Size")));
	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		
		// get info from configuration
		String host = configs.get("Host");
		String port = configs.get("Port");
		String dtProcessorExecPeriod = configs.get("DataProcessor_Execution_Period");
		String BASE_URI = "http://" + host + ":" + port + "/AIRAnalyzerService"; // Base URI for Grizzly HTTP server to listen on
    	
		// start server
		final HttpServer server = startServer(BASE_URI);
		logger.info(String.format("AIRAnalyzerService started at %s\nHit enter to stop...", BASE_URI));
		        
		// run DtProcessor in background in cycle	
		if (dtProcessorExecPeriod == null || dtProcessorExecPeriod.isEmpty()) {
			logger.warn("Data processor execution period is not configured. Use default value: 1 hour.");
			dtProcessorExecPeriod = "3600000";
		} else
			logger.debug("Config: Data processor executes every " + Integer.parseInt(dtProcessorExecPeriod) + " seconds.");
		
		Timer timer = new Timer();
		timer.schedule(new DtProcessor(), 0, Integer.parseInt(dtProcessorExecPeriod)*1000);
        
		// stop server
		System.in.read();
		logger.info("AIRAnalyzerService stopped.");
        server.stop();
    }

}
