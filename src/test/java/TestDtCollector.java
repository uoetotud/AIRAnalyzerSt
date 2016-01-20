import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.citrix.analyzerservice.Main;
import com.citrix.analyzerservice.dbconnector.LocalDbChannel;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dtcollector.DtCollector;

public class TestDtCollector {

	DtCollector dc = new DtCollector();
	static List<LocalDbConference> confList;
	static List<LocalDbChannel> chanList;
	static LocalDbConference conf;
	static LocalDbChannel chan;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		System.out.println("=====================================================");
		System.out.println("### Test DtCollector ###\n");
	}

	@AfterClass
	public static void oneTimeTearDown() {
		System.out.println("### End Test DtCollector ###");
		System.out.println("=====================================================");
	}

	@Before
	public void setUp() {
		Main.cacheIsEnabled = true;
		Main.createCache();
	}

	@After
	public void tearDown() {
		// clear cache
		Main.cache.clear();
		System.out.println("-----------------------------------------------------\n");
	}
	
	@Test
	public void testGetConfList() {
		System.out.println("\n** Get conference list (all) with cache **");
		
		// fetch from file
		confList = dc.getConferenceList("all", "any", "any");
		assertEquals(confList.size(), 10);
		
		// fetch from cache
		confList = dc.getConferenceList("all", "any", "any");
		assertEquals(confList.size(), 10);
		
		System.out.println("\n** Get conference list (all) without cache **");		
		Main.cacheIsEnabled = false;
		
		// fetch from file twice
		confList = dc.getConferenceList("all", "any", "any");
		assertEquals(confList.size(), 10);
		
		confList = dc.getConferenceList("all", "any", "any");
		assertEquals(confList.size(), 10);
	}
	
	@Test
	public void testGetConfListBySizeFilter() {
		System.out.println("\n** Get conference list by size filter with cache **");
		
		/* fetch less, then more */
		// fetch from file
		confList = dc.getConferenceList("1", "any", "any");
		assertEquals(confList.size(), 1);
		
		// fetch from cache
		confList = dc.getConferenceList("all", "any", "any");
		assertEquals(confList.size(), 10);
		
		// clear cache
		Main.cache.clear();
		
		/* fetch more, then less */
		// fetch from file
		confList = dc.getConferenceList("all", "any", "any");
		assertEquals(confList.size(), 10);
		
		// fetch from cache
		confList = dc.getConferenceList("1", "any", "any");
		assertEquals(confList.size(), 1);
		
		System.out.println("\n** Get conference list by size filter without cache **");
		Main.cacheIsEnabled = false;
		
		/* fetch less, then more */
		// fetch from file twice
		confList = dc.getConferenceList("1", "any", "any");
		assertEquals(confList.size(), 1);

		confList = dc.getConferenceList("all", "any", "any");
		assertEquals(confList.size(), 10);
		
		/* fetch more, then less */
		// fetch from file twice
		confList = dc.getConferenceList("all", "any", "any");
		assertEquals(confList.size(), 10);

		confList = dc.getConferenceList("1", "any", "any");
		assertEquals(confList.size(), 1);
	}
	
	@Test
	public void testGetConfListByTimeFilter() {
		System.out.println("\n** Get conference list by time filter with cache **");
		
		/*
		 * Test "from"
		 * */
		
		/* fetch more, then less */
		// fetch from file
		confList = dc.getConferenceList("all", "20160118-0612", "any");
		assertEquals(confList.size(), 10);
		
		// fetch from cache
		confList = dc.getConferenceList("all", "20160119-0739", "any");
		assertEquals(confList.size(), 3);
		
		// clear cache
		Main.cache.clear();
		
		/* fetch less, then more */
		// fetch from file
		confList = dc.getConferenceList("all", "20160119-0739", "any");
		assertEquals(confList.size(), 3);
		
		// fetch from cache
		confList = dc.getConferenceList("all", "20160118-0612", "any");
		assertEquals(confList.size(), 10);
		
		// clear cache
		Main.cache.clear();
		
		/*
		 * Test "to"
		 * */
		
		/* fetch more, then less */
		// fetch from file
		confList = dc.getConferenceList("all", "any", "20160120-0612");
		assertEquals(confList.size(), 10);
		
		// fetch from cache
		confList = dc.getConferenceList("all", "any", "20160119-0612");
		assertEquals(confList.size(), 1);
		
		// clear cache
		Main.cache.clear();
		
		/* fetch less, then more */
		// fetch from file		
		confList = dc.getConferenceList("all", "any", "20160119-0732");
		assertEquals(confList.size(), 5);
		
		// fetch from cache
		confList = dc.getConferenceList("all", "any", "20160120-0612");
		assertEquals(confList.size(), 10);
		
		// clear cache
		Main.cache.clear();
		
		/*
		 * Test both "from" and "to"
		 * */
		
		/* fetch more, then less */
		// fetch from file
		confList = dc.getConferenceList("all", "20160118-0612", "20160120-0612");
		assertEquals(confList.size(), 10);
		
		// fetch from cache
		confList = dc.getConferenceList("all", "20160119-0612", "20160119-0731");
		assertEquals(confList.size(), 4);
		
		// clear cache
		Main.cache.clear();
		
		// fetch from file
		confList = dc.getConferenceList("all", "20160119-0612", "20160119-0750");
		assertEquals(confList.size(), 6);
		
		// fetch from cache
		confList = dc.getConferenceList("all", "20160118-0612", "20160120-0612");
		assertEquals(confList.size(), 10);
		
		System.out.println("\n** Get conference list by time filter without cache **");
		Main.cacheIsEnabled = false;
		
		/*
		 * Test "from"
		 * */
		
		/* fetch more, then less */
		// fetch from file twice
		confList = dc.getConferenceList("all", "20160118-0612", "any");
		assertEquals(confList.size(), 10);

		confList = dc.getConferenceList("all", "20160119-0739", "any");
		assertEquals(confList.size(), 3);
		
		/* fetch less, then more */
		// fetch from file twice
		confList = dc.getConferenceList("all", "20160119-0739", "any");
		assertEquals(confList.size(), 3);

		confList = dc.getConferenceList("all", "20160118-0612", "any");
		assertEquals(confList.size(), 10);
		
		/*
		 * Test "to"
		 * */
		
		/* fetch more, then less */
		// fetch from file twice
		confList = dc.getConferenceList("all", "any", "20160120-0612");
		assertEquals(confList.size(), 10);

		confList = dc.getConferenceList("all", "any", "20160119-0612");
		assertEquals(confList.size(), 1);
		
		/* fetch less, then more */
		// fetch from file twice		
		confList = dc.getConferenceList("all", "any", "20160119-0732");
		assertEquals(confList.size(), 5);

		confList = dc.getConferenceList("all", "any", "20160120-0612");
		assertEquals(confList.size(), 10);
		
		/*
		 * Test both "from" and "to"
		 * */
		
		/* fetch more, then less */
		// fetch from file twice
		confList = dc.getConferenceList("all", "20160118-0612", "20160120-0612");
		assertEquals(confList.size(), 10);
		
		confList = dc.getConferenceList("all", "20160119-0612", "20160119-0731");
		assertEquals(confList.size(), 4);
		
		// fetch from file twice
		confList = dc.getConferenceList("all", "20160119-0612", "20160119-0750");
		assertEquals(confList.size(), 6);

		confList = dc.getConferenceList("all", "20160118-0612", "20160120-0612");
		assertEquals(confList.size(), 10);
	}
	
	@Test
	public void testGetConfListByFilter() {
		System.out.println("\n** Get conference list by size and time filter with cache **");
		
		/* size range > time range */
		// fetch from file
		confList = dc.getConferenceList("8", "20160119-0612", "20160119-0742");
		assertEquals(confList.size(), 6);
		
		// fetch from cache
		confList = dc.getConferenceList("8", "20160119-0612", "20160119-0742");
		assertEquals(confList.size(), 6);
		
		// clear cache
		Main.cache.clear();
		
		/* size range < time range */
		// fetch from file
		confList = dc.getConferenceList("5", "20160119-0612", "20160119-0742");
		assertEquals(confList.size(), 5);
		
		// fetch from cache
		confList = dc.getConferenceList("5", "20160119-0612", "20160119-0742");
		assertEquals(confList.size(), 5);
		
		System.out.println("\n** Get conference list by size and time filter without cache **");
		Main.cacheIsEnabled = false;
		
		/* size range > time range */
		// fetch from file twice
		confList = dc.getConferenceList("8", "20160119-0612", "20160119-0742");
		assertEquals(confList.size(), 6);
		
		confList = dc.getConferenceList("8", "20160119-0612", "20160119-0742");
		assertEquals(confList.size(), 6);
		
		/* size range < time range */
		// fetch from file twice
		confList = dc.getConferenceList("5", "20160119-0612", "20160119-0742");
		assertEquals(confList.size(), 5);

		confList = dc.getConferenceList("5", "20160119-0612", "20160119-0742");
		assertEquals(confList.size(), 5);
	}
	
	@Test
	public void testGetConfSummary() {
		System.out.println("\n** Get conference summary with cache **");
		
		// fetch non-exist conference
		conf = dc.getConferenceSummary("00000000-0000-0000-0000000000000001");
		assertNull(conf);
		
		// fetch from file
		conf = dc.getConferenceSummary("00000000-0000-0000-000000000000000A");
		assertNotNull(conf);
		assertNull(conf.getStats());
		
		// fetch from cache
		conf = dc.getConferenceSummary("00000000-0000-0000-000000000000000A");
		assertNotNull(conf);
		assertNull(conf.getStats());
		System.out.println("Conference stats is null.");
		
		System.out.println("\n** Get conference summary without cache **");
		Main.cacheIsEnabled = false;
		
		// fetch from file twice
		conf = dc.getConferenceSummary("00000000-0000-0000-000000000000000A");
		assertNotNull(conf);
		assertNull(conf.getStats());
				
		conf = dc.getConferenceSummary("00000000-0000-0000-000000000000000A");
		assertNotNull(conf);
		assertNull(conf.getStats());
		System.out.print("Conference stats is null.");
	}
	
	@Test
	public void testGetConfDetails() {
		System.out.println("\n** Get conference details with cache **");
		
		// fetch non-exist conference
		conf = dc.getConferenceDetails("00000000-0000-0000-0000000000000001");
		assertNull(conf);
		
		// fetch from file
		conf = dc.getConferenceDetails("00000000-0000-0000-000000000000000B");
		assertNotNull(conf);
		assertNotNull(conf.getStats());
		
		// fetch from cache
		conf = dc.getConferenceDetails("00000000-0000-0000-000000000000000B");
		assertNotNull(conf);
		assertNotNull(conf.getStats());
		System.out.println("Conference stats is not null.");
		
		System.out.println("\n** Get conference details without cache **");
		Main.cacheIsEnabled = false;
		
		// fetch from file twice
		conf = dc.getConferenceDetails("00000000-0000-0000-000000000000000B");
		assertNotNull(conf);
		assertNotNull(conf.getStats());
				
		conf = dc.getConferenceDetails("00000000-0000-0000-000000000000000B");
		assertNotNull(conf);
		assertNotNull(conf.getStats());
		System.out.print("Conference stats is not null.");
	}
	
	@Test
	public void testGetConfChannels() {
		System.out.println("\n** Get conference channels with cache **");
		
		// fetch non-exist conference
		chanList = dc.getConfChannels("00000000-0000-0000-0000000000000001");
		assertEquals(chanList.size(), 0);
		
		// fetch from file
		chanList = dc.getConfChannels("00000000-0000-0000-000000000000000E");
		assertEquals(chanList.size(), 3);
		
		// fetch from cache
		chanList = dc.getConfChannels("00000000-0000-0000-000000000000000E");
		assertEquals(chanList.size(), 3);
		System.out.println("Conference has 3 channels.");
		
		System.out.println("\n** Get chanListerence channels without cache **");
		Main.cacheIsEnabled = false;
		
		// fetch from file twice
		chanList = dc.getConfChannels("00000000-0000-0000-000000000000000E");
		assertEquals(chanList.size(), 3);
				
		chanList = dc.getConfChannels("00000000-0000-0000-000000000000000E");
		assertEquals(chanList.size(), 3);
		System.out.println("Conference has 3 channels.");
	}
	
	@Test
	public void testGetChanSummary() {
		System.out.println("\n** Get channel summary with cache **");
		
		// fetch non-exist channel
		chan = dc.getChannelSummary("3C5E66C8-F511-4A8B-99C6FF45C0797800");
		assertNull(chan);
		
		// fetch from file
		chan = dc.getChannelSummary("3C5E66C8-F511-4A8B-99C6FF45C0797812");
		assertNotNull(chan);
		assertNull(chan.getStats());
		
		// fetch from cache
		chan = dc.getChannelSummary("3C5E66C8-F511-4A8B-99C6FF45C0797812");
		assertNotNull(chan);
		assertNull(chan.getStats());
		System.out.println("Channel stats is null.");
		
		System.out.println("\n** Get channel summary without cache **");
		Main.cacheIsEnabled = false;
		
		// fetch from file twice
		chan = dc.getChannelSummary("3C5E66C8-F511-4A8B-99C6FF45C0797812");
		assertNotNull(chan);
		assertNull(chan.getStats());
				
		chan = dc.getChannelSummary("3C5E66C8-F511-4A8B-99C6FF45C0797812");
		assertNotNull(chan);
		assertNull(chan.getStats());
		System.out.print("Channel stats is null.");
	}
	
	@Test
	public void testGetChanDetails() {
		System.out.println("\n** Get conference details with cache **");
		
		// fetch non-exist channel
		chan = dc.getChannelDetails("3C5E66C8-F511-4A8B-99C6FF45C0797800");
		assertNull(chan);
		
		// fetch from file
		chan = dc.getChannelDetails("3C5E66C8-F511-4A8B-99C6FF45C0797812");
		assertNotNull(chan);
		assertNotNull(chan.getStats());
		assertNotNull(chan.getStats().getStrProcessor());
		assertNotNull(chan.getStats().getStrEnhancer());
		assertNotNull(chan.getStats().getMixerOut());
		
		assertNotNull(chan.getStats().getStrProcessor().getSeqNr());
		assertNotNull(chan.getStats().getStrProcessor().getRTP_streamBegin());
		assertNotNull(chan.getStats().getStrProcessor().getRTP_isDelayed());
		assertNotNull(chan.getStats().getStrProcessor().getRTP_isReordered());
		assertNotNull(chan.getStats().getStrProcessor().getTimeStamp());
		assertNotNull(chan.getStats().getStrProcessor().getMedia_BufSize());
		assertNotNull(chan.getStats().getStrProcessor().getIATJitter());
		assertNotNull(chan.getStats().getStrProcessor().getNS_speechPowerIn());
		assertNotNull(chan.getStats().getStrProcessor().getNS_speechPowerOut());
		assertNotNull(chan.getStats().getStrProcessor().getNS_noisePowerIn());
		assertNotNull(chan.getStats().getStrProcessor().getNS_noisePowerOut());
		assertNotNull(chan.getStats().getStrProcessor().getAGC_speechLevelOut());
		assertNotNull(chan.getStats().getStrProcessor().getAGC_noiseLevelOut());
		assertNotNull(chan.getStats().getStrProcessor().getAGC_vadState());
		
		assertNotNull(chan.getStats().getStrEnhancer().getQNr());
		assertNotNull(chan.getStats().getStrEnhancer().getQuantumsInJitterBuffer());
		assertNotNull(chan.getStats().getStrEnhancer().getPacketScaleFast());
		assertNotNull(chan.getStats().getStrEnhancer().getPacketScaleSlow());
		assertNotNull(chan.getStats().getStrEnhancer().getPacketScaleFactor());
		assertNotNull(chan.getStats().getStrEnhancer().getQuantumUnderrunCounter());
		assertNotNull(chan.getStats().getStrEnhancer().getQuantumType());
		assertNotNull(chan.getStats().getStrEnhancer().getPopTimeDelta());
		
		assertNotNull(chan.getStats().getMixerOut().getSeqNr());
		assertNotNull(chan.getStats().getMixerOut().getTimeStamp());
		
		// fetch from cache
		chan = dc.getChannelDetails("3C5E66C8-F511-4A8B-99C6FF45C0797812");
		assertNotNull(chan);
		assertNotNull(chan.getStats());
		System.out.println("Channel stats is not null.");
		
		System.out.println("\n** Get chanerence details without cache **");
		Main.cacheIsEnabled = false;
		
		// fetch from file twice
		chan = dc.getChannelDetails("3C5E66C8-F511-4A8B-99C6FF45C0797812");
		assertNotNull(chan);
		assertNotNull(chan.getStats());
				
		chan = dc.getChannelDetails("3C5E66C8-F511-4A8B-99C6FF45C0797812");
		assertNotNull(chan);
		assertNotNull(chan.getStats());
		System.out.print("Channel stats is not null.");
	}

}
