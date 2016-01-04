import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.citrix.analyzerservice.dtprocessor.DtProcessor;
import com.citrix.analyzerservice.util.Config;

public class Main {

	private static final Logger logger = Logger.getLogger(Main.class);
	
	private static Map<String, String> properties = new Config().getPropValues();
	private static String host = properties.get("Host");
	private static String port = properties.get("Port");
	private static String dtProcessorExecPeriod = properties.get("DataProcessor_Execution_Period");
	
	// Base URI the Grizzly HTTP server will listen on
	private static String BASE_URI = "http://" + host + ":" + port + "/AIRAnalyzerService";

	private static HttpServer startServer() {
        
		final ResourceConfig rc = new ResourceConfig().packages("com.citrix.analyzerservice.wshandler");

		return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }
	
//	private static void checkConfig() {
//		host = properties.get("Host");
//		port = properties.get("Port");
//		dtProcessorExecPeriod = properties.get("DataProcessor_Execution_Period");
//		String lines = properties.get("MAX_Stats_Read_Line");
//		String directory = properties.get("File_Directory");
//		
//		logger.debug("********** config.properties *********");
//		logger.debug("Host :: " + host);
//		logger.debug("Port :: " + port);
//		logger.debug("DataProcessor_Execution_Period :: " + dtProcessorExecPeriod + "s");
//		logger.debug("MAX_Stats_Read_Line :: " + lines);
//		logger.debug("File_Directory :: " + directory);
//		logger.debug("********** config.properties *********");
//		
//		if (host.isEmpty())
//			logger.error("HOST name needs to be configured in 'config.properties' file.");
//		if (lines == null || lines.isEmpty())
//			logger.warn("Maximal line number to read file is not configured.  Use default value: all.");
//		if (directory == null || directory.isEmpty())
//			logger.error("File directory needs to be configured in 'config.properties' file.");		
//	}

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
    	
		final HttpServer server = startServer();
		logger.info(String.format("AIRAnalyzerService started at %s\nHit enter to stop...", BASE_URI));
		        
		// Background running		
		if (dtProcessorExecPeriod == null || dtProcessorExecPeriod.isEmpty()) {
			logger.warn("Data processor execution period is not configured. Use default value: 1 hour.");
			dtProcessorExecPeriod = "3600000";
		} else
			logger.debug("Config: Data processor executes every " + Integer.parseInt(dtProcessorExecPeriod) + " seconds.");
		
		Timer timer = new Timer();
		timer.schedule(new DtProcessor(), 0, Integer.parseInt(dtProcessorExecPeriod)*1000);
		// Background running
        
		System.in.read();
		logger.info("AIRAnalyzerService stopped.");
        server.stop();
    }

}
