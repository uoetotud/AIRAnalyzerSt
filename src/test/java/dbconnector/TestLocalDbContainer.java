package dbconnector;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dbconnector.LocalDbContainer;
import com.citrix.analyzerservice.dtprocessor.DtProcessor;
import com.citrix.analyzerservice.util.Config;

public class TestLocalDbContainer {
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");

	static Map<String, String> configs = new Config().getPropValues();
	Map<String, List<String>> updatedConfIds;
	
//	@BeforeClass
//	public static void oneTimeSetUp() throws Exception {
//		config = new Config();
//		configs = config.getPropValues();
//	}
//
//	@AfterClass
//	public static void oneTimeTearDown() throws Exception {
//		config = null;
//		configs.clear();
//	}
//
//	@Before
//	public void setUp() {
//		
//	}
//
//	@After
//	public void tearDown() {
//		updatedConfIds.clear();
//	}

	@Test
	@Ignore
	public void testFindNewConfIds() {
		updatedConfIds = ldc.findUpdatedConfIds();
		assertNotNull(updatedConfIds.get("newConfIds"));
		assertFalse(updatedConfIds.get("newConfIds").isEmpty());
	}
	
	@Test
	@Ignore
	public void testFindNoNewConfIds() {		
		updatedConfIds = ldc.findUpdatedConfIds();
		assertNull(updatedConfIds.get("newConfIds"));
	}
	
	@Test
	@Ignore
	public void testFindOldConfIds() {		
		updatedConfIds = ldc.findUpdatedConfIds();
		assertNotNull(updatedConfIds.get("oldConfIds"));
		assertFalse(updatedConfIds.get("oldConfIds").isEmpty());
	}
	
	@Test
	@Ignore
	public void testFindNoOldConfIds() {		
		updatedConfIds = ldc.findUpdatedConfIds();
		assertNull(updatedConfIds.get("oldConfIds"));
	}
	
	@Test
	public void testFindConference() {
		System.out.println("\n***** Test find conference *****\n");
		
		if (configs.get("Cache_Enable").equalsIgnoreCase("false")) {
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 info.");
			LocalDbConference conferenceExists = ldc.findConference("00000000-0000-0000-0000000000000000", false);
			assertNotNull(conferenceExists);
			assertNull(conferenceExists.getStats());
			System.out.println("Found from file.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000000 stats.");
			conferenceExists = ldc.findConference("00000000-0000-0000-0000000000000000", true);
			assertNotNull(conferenceExists.getStats());
			System.out.println("Found from file.");
			
			System.out.println("Get conference 00000000-0000-0000-0000000000000001 info.");
			LocalDbConference conferenceNotExists = ldc.findConference("00000000-0000-0000-0000000000000001", false);
			assertNull(conferenceNotExists);
			System.out.println("Not found.");
		} else if (configs.get("Cache_Enable").equalsIgnoreCase("true")) {
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
			
//			System.out.println("Get conference 00000000-0000-0000-0000000000000001 info.");
//			LocalDbConference conferenceNotExists = ldc.findConference("00000000-0000-0000-0000000000000001", false);
//			assertNull(conferenceNotExists);
//			System.out.println("Not found.");
		}
		
		System.out.println("-----------------------------------------------------\n");
	}

}
