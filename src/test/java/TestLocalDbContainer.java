import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.citrix.analyzerservice.Main;
import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dtprocessor.DtProcessor;

public class TestLocalDbContainer {
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	Map<String, List<String>> updatedConfIds;
	
	@BeforeClass
	public static void oneTimeSetUp() {
		System.out.println("=====================================================");
		System.out.println("### Test LocalDbContainer ###");
	}

	@AfterClass
	public static void oneTimeTearDown() {
		System.out.println("\n### End Test LocalDbContainer ###");
		System.out.println("=====================================================");
	}
	
	@Before
	public void setUp() {}

	@After
	public void tearDown() {
		System.out.println("-----------------------------------------------------");
	}

	/*
	 * Pre-condition: folder has conference 0, 1, 2, but none is processed
	 * Post-condition: folder has conference 0, 1, and both are processed
	 * */
	@Test
	public void testFindUpdatedConfIds() throws InterruptedException {
		System.out.println("\n** Get updated conference IDs **");
		
		/* test conferences added */
		// get new confIds before processing
		updatedConfIds = ldc.findUpdatedConfIds();
		int size = updatedConfIds.get("newConfIds").size();
		assertEquals(size, 3);
		System.out.println("Found " + size + " new conference(s):");
		for (int i=0; i<size; i++)
			System.out.println(updatedConfIds.get("newConfIds").get(i));
		
		// invoke processor to calculate these conferences
		DtProcessor dp = new DtProcessor();
		dp.run();
		
		// get new confIds again
		updatedConfIds = ldc.findUpdatedConfIds();
		assertFalse(updatedConfIds.containsKey("newConfIds"));
		System.out.println("Found no new conference(s).");
		
		/* test conferences removed */
		System.out.println("\nNow remove conference 22222222-2222-2222-2222222222222222...");
		
		// remove conference
		String path = Main.configs.get("File_Directory") + "150508-092531-conference_22222222-2222-2222-2222222222222222";
		File oldConf = new File(path);
		String[] files = oldConf.list();
		for (String f : files) {
			File currentFile = new File(oldConf, f);
			currentFile.delete();
		}
		oldConf.delete();
		System.out.println("Removed. Check again...");
		
		// get new confIds before processing
		updatedConfIds = ldc.findUpdatedConfIds();
		size = updatedConfIds.get("oldConfIds").size();
		assertEquals(size, 1);
		System.out.println("Found " + size + " removed conference(s): " + updatedConfIds.get("oldConfIds").get(0));
		
		// invoke processor to update list files
		dp.run();
		
		// get updated confIds again
		updatedConfIds = ldc.findUpdatedConfIds();
		assertFalse(updatedConfIds.containsKey("newConfIds"));
		assertFalse(updatedConfIds.containsKey("oldConfIds"));
		System.out.println("Found no updated conference(s).");
	}
	
	/*
	 * Pre-condition: 
	 * 		1. Folder has conference 0, 1, 2, but only 0, 1 have been processed
	 * 		2. If cache is enabled, its size > 3 
	 * */
	@Test
	@Ignore
	public void testFindConference() {
		System.out.println("\n***** Test find conference *****\n");
		
		if (Main.configs.get("Cache_Enable").equalsIgnoreCase("false")) {
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 info.");
			LocalDbConference conferenceExists = ldc.findConference("00000000-0000-0000-0000000000000000", false);
			assertNotNull(conferenceExists);
			assertNull(conferenceExists.getStats());
			System.out.println("Found from file.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 stats.");
			conferenceExists = ldc.findConference("00000000-0000-0000-0000000000000000", true);
			assertNotNull(conferenceExists.getStats());
			System.out.println("Found from file.");
			
			System.out.println("Get conference 22222222-2222-2222-2222222222222222 info.");
			LocalDbConference conferenceNotProcessed = ldc.findConference("22222222-2222-2222-2222222222222222", false);
			assertNotNull(conferenceNotProcessed);
			assertNull(conferenceNotProcessed.getStats());
			assertEquals(conferenceNotProcessed.getScore().getAvgLevelIndicator(), -1);
			assertEquals(conferenceNotProcessed.getScore().getAvgPLIndicator(), -1);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 22222222-2222-2222-2222222222222222 info again.");
			conferenceNotProcessed = ldc.findConference("22222222-2222-2222-2222222222222222", false);
			System.out.println("Found from cache.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000001 info.");
			LocalDbConference conferenceNotExists = ldc.findConference("00000000-0000-0000-0000000000000001", false);
			assertNull(conferenceNotExists);
			System.out.println("Not found.");
			
		} else if (Main.configs.get("Cache_Enable").equalsIgnoreCase("true")) {
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 info.");
			LocalDbConference conferenceExists = ldc.findConference("00000000-0000-0000-0000000000000000", false);
			assertNotNull(conferenceExists);
			assertNull(conferenceExists.getStats());
			System.out.println("Found from file.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 info again.");
			conferenceExists = ldc.findConference("00000000-0000-0000-0000000000000000", false);
			assertNull(conferenceExists.getStats());
			System.out.println("Found from cache.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 stats.");
			conferenceExists = ldc.findConference("00000000-0000-0000-0000000000000000", true);
			assertNotNull(conferenceExists.getStats());
			System.out.println("Found from file.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 stats.");
			conferenceExists = ldc.findConference("00000000-0000-0000-0000000000000000", true);
			assertNotNull(conferenceExists.getStats());
			System.out.println("Found from cache.");
			
			System.out.println("Get conference 22222222-2222-2222-2222222222222222 info.");
			LocalDbConference conferenceNotProcessed = ldc.findConference("22222222-2222-2222-2222222222222222", false);
			assertNotNull(conferenceNotProcessed);
			assertNull(conferenceNotProcessed.getStats());
			assertEquals(conferenceNotProcessed.getScore().getAvgLevelIndicator(), -1);
			assertEquals(conferenceNotProcessed.getScore().getAvgPLIndicator(), -1);
			System.out.println("Found from file.");
			
			System.out.println("Get conference 22222222-2222-2222-2222222222222222 info again.");
			conferenceNotProcessed = ldc.findConference("22222222-2222-2222-2222222222222222", false);
			System.out.println("Found from cache.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000001 info.");
			LocalDbConference conferenceNotExists = ldc.findConference("00000000-0000-0000-0000000000000001", false);
			assertNull(conferenceNotExists);
			System.out.println("Not found.");
			
		}
		
		System.out.println("-----------------------------------------------------\n");
	}
	
	/*
	 * Pre-condition: 
	 * 		1. Folder has conference 0, 1
	 * 		2. Cache is enabled with type = LRU, size = 3
	 * */
	@Test
	@Ignore
	public void testFindConferenceInLru() {		
		System.out.println("\n***** Test find conference in LRU *****\n");
		
		System.out.println("Get conference 00000000-0000-0000-0000000000000000 info.");
		LocalDbConference conference0 = ldc.findConference("00000000-0000-0000-0000000000000000", false);
		assertNotNull(conference0);
		System.out.println("Found from file.");
		
		System.out.println("Get conference 00000000-0000-0000-0000000000000000 stats.");
		conference0 = ldc.findConference("00000000-0000-0000-0000000000000000", true);
		System.out.println("Found from file.");
		
		System.out.println("Get conference 11111111-1111-1111-1111111111111111 info.");
		LocalDbConference conference1 = ldc.findConference("11111111-1111-1111-1111111111111111", false);
		assertNotNull(conference1);
		System.out.println("Found from file.");
		
		System.out.println("Get conference 11111111-1111-1111-1111111111111111 stats.");
		conference1 = ldc.findConference("11111111-1111-1111-1111111111111111", true);
		System.out.println("Found from file.");
		
		System.out.println("Get conference 00000000-0000-0000-0000000000000000 stats again to refresh timestamp.");
		conference0 = ldc.findConference("00000000-0000-0000-0000000000000000", true);
		System.out.println("Found from LRU cache.");
		
		System.out.println("Get conference 00000000-0000-0000-0000000000000000 info again.");
		conference0 = ldc.findConference("00000000-0000-0000-0000000000000000", false);
		System.out.println("Found from LRU cache.");
		
		System.out.println("-----------------------------------------------------\n");
	}
	
	/*
	 * Pre-condition:
	 * 		1. Folder has conference 0, 1
	 * 		2. Cache is enabled with type = LFU, size = 3
	 * */
	@Test
	@Ignore
	public void testFindConferenceInLfu() {		
		System.out.println("\n***** Test find conference in LFU *****\n");
		
		System.out.println("Get conference 00000000-0000-0000-0000000000000000 info 2 times.");
		LocalDbConference conference0 = ldc.findConference("00000000-0000-0000-0000000000000000", false);
		assertNotNull(conference0);
		System.out.println("Found from file.");
		conference0 = ldc.findConference("00000000-0000-0000-0000000000000000", false);
		System.out.println("Found from LFU cache.");
		
		System.out.println("Get conference 11111111-1111-1111-1111111111111111 info 1 time.");
		LocalDbConference conference1 = ldc.findConference("11111111-1111-1111-1111111111111111", false);
		assertNotNull(conference1);
		System.out.println("Found from file.");
		
		System.out.println("Get conference 22222222-2222-2222-2222222222222222 info 3 times.");
		LocalDbConference conference2 = ldc.findConference("22222222-2222-2222-2222222222222222", false);
		assertNotNull(conference2);
		System.out.println("Found from file.");
		conference2 = ldc.findConference("22222222-2222-2222-2222222222222222", false);
		System.out.println("Found from LFU cache.");
		conference2 = ldc.findConference("22222222-2222-2222-2222222222222222", false);
		System.out.println("Found from LFU cache.");
		
		System.out.println("Get conference 00000000-0000-0000-0000000000000000 stats.");
		conference0 = ldc.findConference("00000000-0000-0000-0000000000000000", true);
		System.out.println("Found from file.");
			
		System.out.println("Get conference 11111111-1111-1111-1111111111111111 stats.");
		conference1 = ldc.findConference("11111111-1111-1111-1111111111111111", true);
		System.out.println("Found from file.");
		
		System.out.println("-----------------------------------------------------\n");
	}
	

}
