import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import com.citrix.analyzerservice.dtprocessor.DtProcessor;
import com.citrix.analyzerservice.util.Config;

public class Main {

	// Base URI the Grizzly HTTP server will listen on
	public static final String BASE_URI = "http://localhost:9090/AIRAnalyzer";

	public static HttpServer startServer() {
        
		final ResourceConfig rc = new ResourceConfig().packages("com.citrix.analyzerservice.wshandler");

		return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
    	
		Map<String, String> properties = new Config().getPropValues();
    	
		final HttpServer server = startServer();
//		System.out.println(String.format("Jersey app started with WADL available at " 
//				+ "%sapplication.wadl\nHit enter to stop it...", BASE_URI));
		
		System.out.println(String.format("Jersey app started at %s\nHit enter to stop...", BASE_URI));
        
		// Background running...
		Timer timer = new Timer();
		timer.schedule(new DtProcessor(), 0, Integer.parseInt(properties.get("DataProcessor_Excecution_Period")));
        
		System.in.read();
        server.stop();
    }

}
