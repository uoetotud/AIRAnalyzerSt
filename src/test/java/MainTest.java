import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dbconnector.LocalDbContainer;
import com.citrix.analyzerservice.dtprocessor.DtProcessor;
import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ConferenceScore;
import com.citrix.analyzerservice.util.Config;

public class MainTest {
	
	static LocalDbContainer ldc = new LocalDbContainer();
	static DtProcessor dp = new DtProcessor();

	public static void main(String[] args) {

		testUpdateFile("conference", "00000000-0000-0000-0000000000000000");
		testUpdateFile("channel", "00000000-0000-0000-0000000000000000");
		
	}
	
	static void testConfigProperties() {
		Config config = new Config();
		Map<String, String> configs = config.getPropValues();
		for (String key : configs.keySet())
			System.out.println(key + " :: " + configs.get(key));
	}
	
	static void testGetConferenceDetails() {
		LocalDbConference conference = ldc.findConference("00000000-0000-0000-0000000000000000", true);
		
		System.out.println(conference.getUuid());
		if (conference.getStats() != null) {
			System.out.println(Arrays.toString(conference.getStats().getMixer().getQuantum()));
			System.out.println(Arrays.toString(conference.getStats().getMixer().getnConferenceId()));
			System.out.println(Arrays.toString(conference.getStats().getMixer().getnSpeakers()));
		}
		System.out.println(conference.getScore().getAvgPLIndicator() + ", " + conference.getScore().getAvgLevelIndicator());
	}
	
	static void testGetChannel() {
		LocalDbChannel channel = ldc.findChannel("00000000-0000-0000-0000000000000000", "C42E0EA0-E3D3-453E-A6A6458B611B89D6", true);
		
		System.out.println(channel.getUuid());
		if (channel.getStats() != null) {
			System.out.println(Arrays.toString(channel.getStats().getStrProcessor().getSeqNr()));
			System.out.println(Arrays.toString(channel.getStats().getStrProcessor().getNS_speechPowerOut()));
			System.out.println(Arrays.toString(channel.getStats().getStrProcessor().getNS_noisePowerOut()));
		}
		System.out.println(channel.getScore().getAvgPLIndicator() + ", " + channel.getScore().getAvgLevelIndicator() + ", " + channel.getScore().getAvgPacketLoss());
	}
	
//	static void testDtProcessor() {
//		
//		List<String> newConfIds = ldc.findNewConfIds();
//		
//		if (newConfIds != null || !newConfIds.isEmpty()) {
//			
//			List<ConferenceScore> confScores = new ArrayList<ConferenceScore>();
//			ConferenceScore confScore = new ConferenceScore(0, 0);
//			
//			for (String confId : newConfIds) {
//				
//				List<LocalDbChannel> channels = ldc.findConfChannels(confId);
//				List<ChannelScore> chanScores = new ArrayList<ChannelScore>();
//				
//				for (LocalDbChannel channel : channels) {					
//					ChannelScore chanScore = dp.calChannelScore(confId, channel.getUuid());
//					chanScores.add(chanScore);
//				}
//				
////				System.out.println(chanScores.size());
//				dp.updateChanList(confId, chanScores);
//				
//				confScore = dp.calConferenceScore(confId, chanScores);
//				confScores.add(confScore);
//			}		
//			
//			dp.updateConfList(newConfIds, confScores);
//		}
//	}
	
	static void testFindConfForChannel() {
//		System.out.println(ldc.getChannelConference("B620092E-1B06-4A4D-829811D6003FB46C"));
	}
	
	static Map<String, List<String>> getUpdatedConfIds() {
		return ldc.findUpdatedConfIds();
	}
	
	static void testUpdateFile(String type, String confId) {
		ldc.updateFile(type, confId);
	}
	
}
