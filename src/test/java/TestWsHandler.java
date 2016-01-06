import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.util.Config;
import com.citrix.analyzerservice.wshandler.WsHandler;

public class TestWsHandler {
	
	static Map<String, String> configs = new Config().getPropValues();
	WsHandler ws = new WsHandler();
	
	/*
	 * Pre-condition: 
	 * 		1. Folder has conference 0, 1
	 * 		2. Cache is enabled with size = 3
	 * */
	@Test
	@Ignore
	public void testGetConference() {
		
		if (configs.get("Cache_Type").equalsIgnoreCase("LRU")) {
			System.out.println("\n***** Test get conference in LRU *****\n");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 summary.");
			String conf0 = ws.getConferenceSummary("00000000-0000-0000-0000000000000000");
			assertNotNull(conf0);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 11111111-1111-1111-1111111111111111 summary.");
			String conf1 = ws.getConferenceSummary("11111111-1111-1111-1111111111111111");
			assertNotNull(conf1);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 details.");
			String conf0d = ws.getConferenceDetails("00000000-0000-0000-0000000000000000");
			assertNotNull(conf0d);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 11111111-1111-1111-1111111111111111 details.");
			String conf1d = ws.getConferenceDetails("11111111-1111-1111-1111111111111111");
			assertNotNull(conf1d);
			System.out.println("Found from file.");
			
		} else if (configs.get("Cache_Type").equalsIgnoreCase("LFU")) {
			System.out.println("\n***** Test get conference in LFU *****\n");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 summary 2 times.");
			String conf0 = ws.getConferenceSummary("00000000-0000-0000-0000000000000000");
			assertNotNull(conf0);
			System.out.println("Found from file.");
			conf0 = ws.getConferenceSummary("00000000-0000-0000-0000000000000000");
			System.out.println("Found from LFU cache.");
			
			System.out.println("Get conference 11111111-1111-1111-1111111111111111 summary 1 time.");
			String conf1 = ws.getConferenceSummary("11111111-1111-1111-1111111111111111");
			assertNotNull(conf1);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 details 3 times.");
			String conf0d = ws.getConferenceDetails("00000000-0000-0000-0000000000000000");
			assertNotNull(conf0d);
			System.out.println("Found from file.");
			conf0d = ws.getConferenceDetails("00000000-0000-0000-0000000000000000");
			System.out.println("Found from LFU cache.");
			conf0d = ws.getConferenceDetails("00000000-0000-0000-0000000000000000");
			System.out.println("Found from LFU cache.");
			
			System.out.println("Get conference 11111111-1111-1111-1111111111111111 details.");
			String conf1d = ws.getConferenceDetails("11111111-1111-1111-1111111111111111");
			assertNotNull(conf1d);
			System.out.println("Found from file.");
			
		}
		
		System.out.println("-----------------------------------------------------\n");
	}
	
	/*
	 * Pre-condition: 
	 * 		1. Folder has conference 0, 1
	 * 		2. Cache is enabled with size = 3
	 * */
	@Test
	@Ignore
	public void testGetChannel() {
		
		if (configs.get("Cache_Type").equalsIgnoreCase("LRU")) {
			System.out.println("\n***** Test get channel in LRU *****\n");
			
			System.out.println("Get channel C05C1C75-8DDF-4906-9C9CC69277926064 summary.");
			String chan0 = ws.getChannelSummary("C05C1C75-8DDF-4906-9C9CC69277926064");
			assertNotNull(chan0);
			System.out.println("Found from file.");
			
			System.out.println("Get channel C42E0EA0-E3D3-453E-A6A6458B611B89D6 summary.");
			String conf1 = ws.getChannelSummary("C42E0EA0-E3D3-453E-A6A6458B611B89D6");
			assertNotNull(conf1);
			System.out.println("Found from file.");
			
			System.out.println("Get channel C05C1C75-8DDF-4906-9C9CC69277926064 details.");
			String conf0d = ws.getChannelDetails("C05C1C75-8DDF-4906-9C9CC69277926064");
			assertNotNull(conf0d);
			System.out.println("Found from file.");
			
			System.out.println("Get channel C42E0EA0-E3D3-453E-A6A6458B611B89D6 details.");
			String conf1d = ws.getChannelDetails("C42E0EA0-E3D3-453E-A6A6458B611B89D6");
			assertNotNull(conf1d);
			System.out.println("Found from file.");
			
		} else if (configs.get("Cache_Type").equalsIgnoreCase("LFU")) {
			System.out.println("\n***** Test get channel in LFU *****\n");
			
			System.out.println("Get channel C05C1C75-8DDF-4906-9C9CC69277926064 summary 2 times.");
			String chan0 = ws.getChannelSummary("C05C1C75-8DDF-4906-9C9CC69277926064");
			assertNotNull(chan0);
			System.out.println("Found from file.");
			chan0 = ws.getChannelSummary("C05C1C75-8DDF-4906-9C9CC69277926064");
			System.out.println("Found from LFU cache.");
			
			System.out.println("Get channel C42E0EA0-E3D3-453E-A6A6458B611B89D6 summary 1 time.");
			String chan1 = ws.getChannelSummary("C42E0EA0-E3D3-453E-A6A6458B611B89D6");
			assertNotNull(chan1);
			System.out.println("Found from file.");
			
			System.out.println("Get channel C05C1C75-8DDF-4906-9C9CC69277926064 details 3 times.");
			String chan0d = ws.getChannelDetails("C05C1C75-8DDF-4906-9C9CC69277926064");
			assertNotNull(chan0d);
			System.out.println("Found from file.");
			chan0d = ws.getChannelDetails("C05C1C75-8DDF-4906-9C9CC69277926064");
			System.out.println("Found from LFU cache.");
			chan0d = ws.getChannelDetails("C05C1C75-8DDF-4906-9C9CC69277926064");
			System.out.println("Found from LFU cache.");
			
			System.out.println("Get channel C42E0EA0-E3D3-453E-A6A6458B611B89D6 details.");
			String chan1d = ws.getChannelDetails("C42E0EA0-E3D3-453E-A6A6458B611B89D6");
			assertNotNull(chan1d);
			System.out.println("Found from file.");
			
		}
		
		System.out.println("-----------------------------------------------------\n");
	}
	
	/*
	 * Pre-condition: 
	 * 		1. Folder has conference 0, 1, 2
	 * 		2. Cache is enabled with size = 2
	 * */
	@Test
	public void testGetConfChannels() {
		
		if (configs.get("Cache_Type").equalsIgnoreCase("LRU")) {
			System.out.println("\n***** Test get conference channels in LRU *****\n");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 channels.");
			String conf0chans = ws.getConfChannels("00000000-0000-0000-0000000000000000");
			assertNotNull(conf0chans);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 11111111-1111-1111-1111111111111111 channels.");
			String conf1chans = ws.getConfChannels("11111111-1111-1111-1111111111111111");
			assertNotNull(conf1chans);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 channels again.");
			conf0chans = ws.getConfChannels("00000000-0000-0000-0000000000000000");
			assertNotNull(conf0chans);
			System.out.println("Found from cache.");
			
			System.out.println("Get conference 22222222-2222-2222-2222222222222222 channels.");
			String conf2chans = ws.getConfChannels("22222222-2222-2222-2222222222222222");
			assertNotNull(conf2chans);
			System.out.println("Found from file.");
			
		} else if (configs.get("Cache_Type").equalsIgnoreCase("LFU")) {
			System.out.println("\n***** Test get conference channels in LFU *****\n");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 channels 2 times.");
			String conf0chans = ws.getConfChannels("00000000-0000-0000-0000000000000000");
			assertNotNull(conf0chans);
			System.out.println("Found from file.");
			conf0chans = ws.getConfChannels("00000000-0000-0000-0000000000000000");
			System.out.println("Found from LFU cache.");
			
			System.out.println("Get conference 11111111-1111-1111-1111111111111111 channels 1 time.");
			String conf1chans = ws.getConfChannels("11111111-1111-1111-1111111111111111");
			assertNotNull(conf1chans);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 22222222-2222-2222-2222222222222222 channels.");
			String conf2chans = ws.getConfChannels("22222222-2222-2222-2222222222222222");
			assertNotNull(conf2chans);
			System.out.println("Found from file.");
			
		}
		
		System.out.println("-----------------------------------------------------\n");
	}

}
