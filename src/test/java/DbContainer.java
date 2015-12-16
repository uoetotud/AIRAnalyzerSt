import java.util.Arrays;
import java.util.List;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;

public class DbContainer {

	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	public void getNewConfIds() {
		List<String> confIds = ldc.findNewConfIds();
		
		for (int i=0; i<confIds.size(); i++)
			System.out.println(confIds.get(i));
	}
	
	public void GetConference() {
		LocalDbConference conference = ldc.findConference("00000000-0000-0000-0000000000000000", true);
		System.out.println(conference.getUuid());
		System.out.println(conference.getTimestamp());
		System.out.println(Arrays.toString(conference.getStats().getMixer().getQuantum()));
		System.out.println(Arrays.toString(conference.getStats().getMixer().getnConferenceId()));
		System.out.println(Arrays.toString(conference.getStats().getMixer().getnSpeakers()));
	}
	
	
	/* Get channel */
//	LocalDbChannel channel = ldc.findChannel("00000000-0000-0000-0000000000000000", "C42E0EA0-E3D3-453E-ABCDEFG123456789");
//	System.out.println(channel.getUuid());
//	System.out.println(Arrays.toString(channel.getStats().getStrProcessor().getSeqNr()));
//	System.out.println(Arrays.toString(channel.getStats().getStrProcessor().getNS_speechPowerOut()));
//	System.out.println(Arrays.toString(channel.getStats().getStrProcessor().getNS_noisePowerOut()));
//	if (channel.getScore()!=null) {
//		System.out.println(channel.getScore().getAvgPLIndicator());
//		System.out.println(channel.getScore().getAvgLevelIndicator());
//		System.out.println(channel.getScore().getAvgPacketLoss());
//	}
	
	/* Get conference's channel number */
//	ldc.findConference("00000000-0000-0000-0000000000000000");
	
	
	/* Read file */		
//	ArrayList<ArrayList<String>> data = ldc.readFile("C:/Users/xil/Desktop/AIR recording example/150507-120756-conference_00000000-0000-0000-0000000000000000/"
//			+ "BackendStreamProcessor-Meta_channel-0_channeluuid-C05C1C75-8DDF-4906-9C9CC69277926064_fid-4.txt", ",");
//	
//	ArrayList<ArrayList<String>> data = ldc.readFile("C:/Users/xil/Desktop/AIR recording example/150507-120756-conference_00000000-0000-0000-0000000000000000/"
//			+ "MixerSumStream-ActiveSpeakers_fid-2.txt", ",");
//	ArrayList<ArrayList<String>> data = ldc.readFile("C:/Users/xil/Desktop/AIR recording example/ConfList.txt", ",");
//	
//	int size = data.get(0).size();		
//	
//	for (int i=0; i<data.size(); i++) {
//		for (int j=0; j<size; j++) {
//			System.out.print(data.get(i).get(j));
//			if (j!=size-1)
//				System.out.print(",");
//		}
//		System.out.println();
//	}
//	
//	System.out.println(data.size());
	
	/* Write file */
//	String line1 = "uuid, timestamp, channelNo, score";
//	String line2 = "11111, 1234567, 3, 90";
//	String line3 = "22222, 2345678, 5, 85";
//	String line4 = "33333, 3456789, 2, 98";
//	String[] data = {line1, line2, line3, line4};
//	
//	String line5 = "44444, 4567890, 3, 88";
//	String line6 = "55555, 5678901, 4, 80";
//	String line7 = "66666, 6789012, 6, 92";
//	String[] data = {line5, line6, line7};
//	
//	ldc.writeFile("C:/Users/xil/Desktop/AIR recording example/ConfList.txt", data);
	
}
