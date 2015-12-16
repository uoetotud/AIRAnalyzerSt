import java.util.ArrayList;
import java.util.List;

import com.citrix.analyzerservice.dbconnector.LocalDbContainer;
import com.citrix.analyzerservice.dtprocessor.DtProcessor;
import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ChannelStats;
import com.citrix.analyzerservice.model.ConferenceScore;

public class Processor {

	DtProcessor dp = new DtProcessor();
	
	public List<ChannelScore> calChannelScore(String confId) {
//		ChannelScore score = dp.calChannelScore("00000000-0000-0000-0000000000000000", "C42E0EA0-E3D3-453E-ABCDEFG123456789");	
		
		List<ChannelScore> chanScores = new ArrayList<ChannelScore>();
		ChannelScore score = dp.calChannelScore(confId, "C05C1C75-8DDF-4906-9C9CC69277926064");
		chanScores.add(score);
		
		System.out.println("Channel 0:");
		System.out.println(score.getAvgPLIndicator());
		System.out.println(score.getAvgLevelIndicator());
		System.out.println(score.getAvgPacketLoss());
		System.out.println();
		
		score = dp.calChannelScore(confId, "C42E0EA0-E3D3-453E-A6A6458B611B89D6");
		chanScores.add(score);
		
		System.out.println("Channel 1:");
		System.out.println(score.getAvgPLIndicator());
		System.out.println(score.getAvgLevelIndicator());
		System.out.println(score.getAvgPacketLoss());
		System.out.println();
		
		score = dp.calChannelScore(confId, "B620092E-1B06-4A4D-829811D6003FB46A");
		chanScores.add(score);
		
		System.out.println("Channel 2:");
		System.out.println(score.getAvgPLIndicator());
		System.out.println(score.getAvgLevelIndicator());
		System.out.println(score.getAvgPacketLoss());
		System.out.println();
		
		return chanScores;
	}
	
	public ConferenceScore calConferenceScore(List<ChannelScore> chanScores) {
		ConferenceScore score = dp.calConferenceScore("00000000-0000-0000-0000000000000000", chanScores);
		
		System.out.println("Conference:");
		System.out.println(score.getAvgPLIndicator());
		System.out.println(score.getAvgLevelIndicator());
		
		return score;
	}
	
	public void updateChanList(List<ChannelScore> chanScores) {
		dp.updateChanList("00000000-0000-0000-0000000000000000", chanScores);
	}
	
	public void updateConfList(List<ConferenceScore> confScores) {
		List<String> confIds = new ArrayList<String>();
		confIds.add("00000000-0000-0000-0000000000000000");		
		
		dp.updateConfList(confIds, confScores);
	}
}
 