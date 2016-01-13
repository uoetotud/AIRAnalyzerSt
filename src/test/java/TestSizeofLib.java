import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.carrotsearch.sizeof.RamUsageEstimator;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dtcollector.DtCollector;

public class TestSizeofLib {
	
	static DtCollector dc = new DtCollector();
	
	@BeforeClass
	public static void oneTimeSetUp() {
		System.out.println("=====================================================");
		System.out.println("### Test sizeof library ###\n");
	}

	@AfterClass
	public static void oneTimeTearDown() {
		System.out.println("### End sizeof library ###");
		System.out.println("=====================================================");
	}

	@Before
	public void setUp() {}

	@After
	public void tearDown() {
		System.out.println("-----------------------------------------------------\n");
	}
	
	@Test
	public void getPrimitiveSize() {
		System.out.println("** Get primitives sizes **");
		
		boolean b = true;
		char c = 'a';
		short s = 3;
		float f = 3;
		int i = 3;
		double d = 3;
		long l = 3;
		
		System.out.println("boolean: " + RamUsageEstimator.sizeOf(b) + " bytes.");
		System.out.println("char: " + RamUsageEstimator.sizeOf(c) + " bytes.");
		System.out.println("short: " + RamUsageEstimator.sizeOf(s) + " bytes.");
		System.out.println("float: " + RamUsageEstimator.sizeOf(f) + " bytes.");
		System.out.println("int: " + RamUsageEstimator.sizeOf(i) + " bytes.");
		System.out.println("double: " + RamUsageEstimator.sizeOf(d) + " bytes.");
		System.out.println("long: " + RamUsageEstimator.sizeOf(l) + " bytes.");
	}
	
	@Test
	public void getJavaObjectSize() {
		System.out.println("** Get java objects sizes **");
		
		String confId = "00000000-0000-0000-0000000000000000";
		String chanId = "C05C1C75-8DDF-4906-9C9CC69277926064";
		LocalDateTime time = LocalDateTime.now();
		
		System.out.println("LocalDateTime: " + RamUsageEstimator.sizeOf(time) + " bytes.");
		System.out.println("String: " + RamUsageEstimator.sizeOf(confId) + " bytes.");
		System.out.println("Long string: " + RamUsageEstimator.sizeOf(chanId) + " bytes.");
	}
	
	@Test
	public void getMyObjectSize() {
		System.out.println("** Get self-defined objects sizes **");
		
		List<LocalDbConference> confList = dc.getConferenceList("all", "any", "any");
		
		LocalDbConference confSum = dc.getConferenceSummary("00000000-0000-0000-0000000000000000");
		LocalDbConference confDet = dc.getConferenceDetails("00000000-0000-0000-0000000000000000");
		
		List<LocalDbChannel> chanList = dc.getConfChannels("00000000-0000-0000-0000000000000000");
		
		LocalDbChannel chanSum = dc.getChannelSummary("C05C1C75-8DDF-4906-9C9CC69277926064");
		LocalDbChannel chanDet = dc.getChannelDetails("C05C1C75-8DDF-4906-9C9CC69277926064");
		
		System.out.println("conference list: " + RamUsageEstimator.sizeOf(confList) + " bytes.");
		System.out.println("conference summary: " + RamUsageEstimator.sizeOf(confSum) + " bytes.");
		System.out.println("conference details: " + RamUsageEstimator.sizeOf(confDet) + " bytes.");
		System.out.println("channel list: " + RamUsageEstimator.sizeOf(chanList) + " bytes.");
		System.out.println("channel summary: " + RamUsageEstimator.sizeOf(chanSum) + " bytes.");
		System.out.println("channel details: " + RamUsageEstimator.sizeOf(chanDet) + " bytes.");
	}
}
