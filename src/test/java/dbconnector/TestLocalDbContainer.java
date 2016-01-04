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

import com.citrix.analyzerservice.dbconnector.LocalDbContainer;
import com.citrix.analyzerservice.dtprocessor.DtProcessor;
import com.citrix.analyzerservice.util.Config;

public class TestLocalDbContainer {
	
	static LocalDbContainer ldc = new LocalDbContainer();
	
	private static Config config;
	static Map<String, String> configs;
	Map<String, List<String>> updatedConfIds;
	
	@BeforeClass
	public static void oneTimeSetUp() throws Exception {
		config = new Config();
		configs = config.getPropValues();
	}

	@AfterClass
	public void oneTimeTearDown() throws Exception {
		config = null;
		configs.clear();
	}

	@Before
	public void setUp() {
		
	}

	@After
	public void tearDown() {
		updatedConfIds.clear();
	}

	@Ignore
	@Test
	public void testFindNewConfIds() {
		updatedConfIds = ldc.findUpdatedConfIds();
		assertNotNull(updatedConfIds.get("newConfIds"));
		assertFalse(updatedConfIds.get("newConfIds").isEmpty());
	}
	
	@Test
	public void testFindNoNewConfIds() {		
		updatedConfIds = ldc.findUpdatedConfIds();
		assertNull(updatedConfIds.get("newConfIds"));
	}
	
	@Ignore
	@Test
	public void testFindOldConfIds() {		
		updatedConfIds = ldc.findUpdatedConfIds();
		assertNotNull(updatedConfIds.get("oldConfIds"));
		assertFalse(updatedConfIds.get("oldConfIds").isEmpty());
	}
	
	@Test
	public void testFindNoOldConfIds() {		
		updatedConfIds = ldc.findUpdatedConfIds();
		assertNull(updatedConfIds.get("oldConfIds"));
	}

}
