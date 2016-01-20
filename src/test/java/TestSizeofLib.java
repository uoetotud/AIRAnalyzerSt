import java.time.LocalDateTime;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
	@Ignore
	public void getPrimitiveSize() {
		System.out.println("** Get primitives sizes **");
		
		boolean b = true;
		char c = 'a';
		short s = (short) 3000000;
		float f = (float) 3000000.1111;
		int i = 3000000;
		double d = 3000000;
		long l = 3000000;
		
		System.out.println("boolean: " + RamUsageEstimator.sizeOf(b) + " bytes.");
		System.out.println("char: " + RamUsageEstimator.sizeOf(c) + " bytes.");
		System.out.println("short: " + RamUsageEstimator.sizeOf(s) + " bytes.");
		System.out.println("float: " + RamUsageEstimator.sizeOf(f) + " bytes.");
		System.out.println("int: " + RamUsageEstimator.sizeOf(i) + " bytes.");
		System.out.println("double: " + RamUsageEstimator.sizeOf(d) + " bytes.");
		System.out.println("long: " + RamUsageEstimator.sizeOf(l) + " bytes.");
	}
	
	@Test
	@Ignore
	public void getJavaObjectSize() {
		System.out.println("** Get java objects sizes **");
		
		String confId = "00000000-0000-0000-000000000000000J";
		String chanId = "E0EA6DAF-3504-47DB-8FEB4EEBFC336C59";
		LocalDateTime time = LocalDateTime.now();
		
		System.out.println("LocalDateTime: " + RamUsageEstimator.sizeOf(time) + " bytes.");
		System.out.println("String: " + RamUsageEstimator.sizeOf(confId) + " bytes.");
		System.out.println("Long string: " + RamUsageEstimator.sizeOf(chanId) + " bytes.");
	}
	
	@Test
//	@Ignore
	public void getMyObjectSize() {
		System.out.println("** Get self-defined objects sizes **");
		
		List<LocalDbConference> confList = dc.getConferenceList("all", "any", "any");
		
		LocalDbConference confSum = dc.getConferenceSummary("00000000-0000-0000-000000000000000J");
		LocalDbConference confDet = dc.getConferenceDetails("00000000-0000-0000-000000000000000J");
		
		List<LocalDbChannel> chanList = dc.getConfChannels("00000000-0000-0000-000000000000000J");
		
		LocalDbChannel chanSum = dc.getChannelSummary("E0EA6DAF-3504-47DB-8FEB4EEBFC336C59");
		LocalDbChannel chanDet = dc.getChannelDetails("E0EA6DAF-3504-47DB-8FEB4EEBFC336C59");
		
		System.out.println("conference list: " + RamUsageEstimator.sizeOf(confList) + " bytes.");
		System.out.println("conference summary: " + RamUsageEstimator.sizeOf(confSum) + " bytes.");
		System.out.println("conference details: " + RamUsageEstimator.sizeOf(confDet) + " bytes.");
		System.out.println("channel list: " + RamUsageEstimator.sizeOf(chanList) + " bytes.");
		System.out.println("channel summary: " + RamUsageEstimator.sizeOf(chanSum) + " bytes.");
		System.out.println("channel details: " + RamUsageEstimator.sizeOf(chanDet) + " bytes.");
	}
}
