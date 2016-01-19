import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.citrix.analyzerservice.Main;
import com.citrix.analyzerservice.dbconnector.DbConnectorFactory;
import com.citrix.analyzerservice.dbconnector.IDbConnector;
import com.citrix.analyzerservice.dtprocessor.DtProcessor;

public class TestLocalDbContainer {
	
	DbConnectorFactory dcf = new DbConnectorFactory();
	IDbConnector ldc = dcf.getDbContainer("LOCAL");
	
	static String path = Main.configs.get("File_Directory");
	
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
		
		Map<String, List<String>> updatedConfIds;
		
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
		String confPath = path + "150508-092531-conference_22222222-2222-2222-2222222222222222";
		File oldConf = new File(confPath);
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
	
	@Test
	public void testReadWriteUpdateFile() {
		System.out.println("\n** Read/Write/Update file **");

		String file = path + "ConfList.txt";
		File f = new File(file);
		if (f.exists())
			f.delete();
		System.out.println("ConfList.txt file not exists.");
		assertFalse(f.exists());
		
		List<List<String>> readContent = ldc.readFile(file, ",", "all");
		assertEquals(readContent.size(), 0);
		
		System.out.println("Write 3 lines ...");
		String[] writeContent = { "id1, time1, 1, 1.1, 11.1", "id2, time2, 2, 2.2, 22.2", "id3, time3, 3, 3.3, 33.3" };
		assertTrue(ldc.writeFile("conference", writeContent));
		
		System.out.println("Read all contents: 3 lines.");
		readContent = ldc.readFile(file, ",", "all");
		assertEquals(readContent.size(), 4);
		
		System.out.println("Read only 2 lines.");
		readContent = ldc.readFile(file, ",", "2");
		assertEquals(readContent.size(), 3);
		
		System.out.println("Append 2 lines ...");
		String[] appendContent = { "id4, time4, 4, 4.4, 44.4", "id5, time5, 5, 5.5, 55.5" };
		assertTrue(ldc.writeFile("conference", appendContent));
		
		System.out.println("Read all contents: 5 lines.");
		readContent = ldc.readFile(file, ",", "all");
		assertEquals(readContent.size(), 6);
		
		System.out.println("Delete 1 line ...");
		String deleteContent = "id2";
		assertTrue(ldc.updateFile("conference", deleteContent));
		
		System.out.println("Read all contents: 4 lines.");
		readContent = ldc.readFile(file, ",", "all");
		assertEquals(readContent.size(), 5);
		for (int i=0; i<readContent.size(); i++) {
			assertFalse(readContent.get(i).contains("id2"));
		}
		
		f.delete();
	}
	

}
