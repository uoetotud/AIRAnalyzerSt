//import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.citrix.analyzerservice.dtprocessor.DtProcessor;
import com.citrix.analyzerservice.model.ChannelScore;
import com.citrix.analyzerservice.model.ConferenceScore;


public class TestDtProcessor {
	
	DtProcessor dp = new DtProcessor();
	
	@BeforeClass
	public static void oneTimeSetUp() {
		System.out.println("=====================================================");
		System.out.println("### Test DtProcessor ###\n");
	}

	@AfterClass
	public static void oneTimeTearDown() {
		System.out.println("### End Test DtProcessor ###");
		System.out.println("=====================================================");
	}
	
	@Before
	public void setUp() {}

	@After
	public void tearDown() {
		System.out.println("-----------------------------------------------------\n");
	}

	/*
	 * Pre-condition: 
	 * 		1. Folder has conference 0, 1, 2, but only 0, 1 have been processed
	 * 		2. If cache is enabled, its size > 3 
	 * */
	@Test
	public List<ChannelScore> calChannelScore(String confId) {
//		ChannelScore score = dp.calChannelScore("00000000-0000-0000-0000000000000000", "C42E0EA0-E3D3-453E-ABCDEFG123456789");	
		
		List<ChannelScore> chanScores = new ArrayList<ChannelScore>();
		ChannelScore score = dp.calcChannelScore(confId, "C05C1C75-8DDF-4906-9C9CC69277926064");
		chanScores.add(score);
		
		System.out.println("Channel 0:");
		System.out.println(score.getAvgPLIndicator());
		System.out.println(score.getAvgLevelIndicator());
		System.out.println(score.getAvgPacketLoss());
		System.out.println();
		
		score = dp.calcChannelScore(confId, "C42E0EA0-E3D3-453E-A6A6458B611B89D6");
		chanScores.add(score);
		
		System.out.println("Channel 1:");
		System.out.println(score.getAvgPLIndicator());
		System.out.println(score.getAvgLevelIndicator());
		System.out.println(score.getAvgPacketLoss());
		System.out.println();
		
		score = dp.calcChannelScore(confId, "B620092E-1B06-4A4D-829811D6003FB46A");
		chanScores.add(score);
		
		System.out.println("Channel 2:");
		System.out.println(score.getAvgPLIndicator());
		System.out.println(score.getAvgLevelIndicator());
		System.out.println(score.getAvgPacketLoss());
		System.out.println();
		
		return chanScores;
	}
	
	@Test
	public ConferenceScore calConferenceScore(List<ChannelScore> chanScores) {
		ConferenceScore score = dp.calcConferenceScore("00000000-0000-0000-0000000000000000", chanScores);
		
		System.out.println("Conference:");
		System.out.println(score.getAvgPLIndicator());
		System.out.println(score.getAvgLevelIndicator());
		
		return score;
	}
	
	@Test
	public void updateChanList(List<ChannelScore> chanScores) {
		dp.updateChanList("00000000-0000-0000-0000000000000000", chanScores);
	}
	
	@Test
	public void updateConfList(List<ConferenceScore> confScores) {
		List<String> confIds = new ArrayList<String>();
		confIds.add("00000000-0000-0000-0000000000000000");		
		
		dp.updateConfList(confIds, confScores);
	}

}
