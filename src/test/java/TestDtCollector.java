import static org.junit.Assert.*;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.citrix.analyzerservice.dbconnector.LocalDbConference;
import com.citrix.analyzerservice.dtcollector.DtCollector;

public class TestDtCollector {

	DtCollector dc = new DtCollector();
	
	@Test
	@Ignore
	public void testGetConferenceList() {
		List<LocalDbConference> confList = dc.getConferenceList("all", "any", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
	}
	
	@Test
//	@Ignore
	public void testGetConferenceListBySizeFilter() {
		List<LocalDbConference> confList = dc.getConferenceList("1", "any", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 1);
		
		confList = dc.getConferenceList("2", "any", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 2);
		
		confList = dc.getConferenceList("all", "any", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
		
		DtCollector.cache.clear();
		
		confList = dc.getConferenceList("all", "any", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
		
		confList = dc.getConferenceList("1", "any", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 1);
	}
	
	@Test
//	@Ignore
	public void testGetConferenceListByTimeFilter() {
		List<LocalDbConference> confList = dc.getConferenceList("all", "2015-05-07_06:12", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
		
		confList = dc.getConferenceList("all", "2015-05-07_13:12", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 2);
		
		confList = dc.getConferenceList("all", "2015-05-07_15:12", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 1);
		
		DtCollector.cache.clear();
		
		confList = dc.getConferenceList("all", "2015-05-07_15:12", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 1);
		
		confList = dc.getConferenceList("all", "2015-05-07_13:12", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 2);
		
		confList = dc.getConferenceList("all", "2015-05-07_06:12", "any");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
		
		DtCollector.cache.clear();
		System.out.println();
		
		confList = dc.getConferenceList("all", "any", "2015-05-09_06:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
		
		confList = dc.getConferenceList("all", "any", "2015-05-08_07:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 2);
		
		confList = dc.getConferenceList("all", "any", "2015-05-07_13:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 1);
		
		DtCollector.cache.clear();
		
		confList = dc.getConferenceList("all", "any", "2015-05-07_13:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 1);
		
		confList = dc.getConferenceList("all", "any", "2015-05-08_07:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 2);
		
		confList = dc.getConferenceList("all", "any", "2015-05-09_06:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
		
		DtCollector.cache.clear();
		System.out.println();
		
		confList = dc.getConferenceList("all", "2015-05-07_06:12", "2015-05-09_06:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
		
		confList = dc.getConferenceList("all", "2015-05-07_13:12", "2015-05-08_07:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 1);
		
		DtCollector.cache.clear();
		
		confList = dc.getConferenceList("all", "2015-05-07_13:12", "2015-05-08_07:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 1);
		
		confList = dc.getConferenceList("all", "2015-05-07_06:12", "2015-05-09_06:12");
		assertNotNull(confList);
		assertEquals(confList.size(), 3);
	}
	
	@Test
	public void testGetConferenceListByFilter() {
		List<LocalDbConference> confList = dc.getConferenceList("2", "2015-05-07_06:12", "2015-05-08_12:00");
		assertNotNull(confList);
		assertEquals(confList.size(), 2);
		
		confList = dc.getConferenceList("1", "2015-05-07_06:12", "2015-05-08_12:00");
		assertEquals(confList.size(), 1);
		
		DtCollector.cache.clear();
		System.out.println();
		
		confList = dc.getConferenceList("2", "2015-05-07_13:12", "2015-05-08_15:00");
		assertEquals(confList.size(), 2);
		
		confList = dc.getConferenceList("1", "2015-05-07_13:12", "2015-05-07_15:00");
		assertEquals(confList.size(), 1);
		
		DtCollector.cache.clear();
		System.out.println();
		
		confList = dc.getConferenceList("2", "2015-05-07_06:12", "2015-05-08_15:00");
		assertEquals(confList.size(), 2);
		
		confList = dc.getConferenceList("2", "2015-05-07_06:12", "2015-05-07_13:00");
		assertEquals(confList.size(), 1);
	}

}
