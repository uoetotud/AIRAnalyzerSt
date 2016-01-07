import java.io.IOException;
import java.net.URI;
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

	private static HttpServer startServer(String uri) {        
		final ResourceConfig rc = new ResourceConfig().packages("com.citrix.analyzerservice.wshandler");

		return GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
    }

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		
		// Get configurations
		Config configuration = new Config();
		configuration.checkConfig();
		Map<String, String> configs = configuration.getPropValues();
		String host = configs.get("Host");
		String port = configs.get("Port");
		String dtProcessorExecPeriod = configs.get("DataProcessor_Execution_Period");
		String BASE_URI = "http://" + host + ":" + port + "/AIRAnalyzerService"; // Base URI for Grizzly HTTP server to listen on
    	
		final HttpServer server = startServer(BASE_URI);
		logger.info(String.format("AIRAnalyzerService started at %s\nHit enter to stop...", BASE_URI));
		        
		// Background running		
		if (dtProcessorExecPeriod == null || dtProcessorExecPeriod.isEmpty()) {
			logger.warn("Data processor execution period is not configured. Use default value: 1 hour.");
			dtProcessorExecPeriod = "3600000";
		} else
			logger.debug("Config: Data processor executes every " + Integer.parseInt(dtProcessorExecPeriod) + " seconds.");
		
		Timer timer = new Timer();
		timer.schedule(new DtProcessor(), 0, Integer.parseInt(dtProcessorExecPeriod)*1000);
		// End background running
        
		System.in.read();
		logger.info("AIRAnalyzerService stopped.");
        server.stop();
    }

}
