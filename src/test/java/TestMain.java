import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * @author Xi Luo
 *
 */
public class TestMain {

	public static void main(String[] args) {
		
		// test Cache
		testCache();
		
		// test LocalDbContainer
		testLocalDbContainer();
		
		// test DtCollector
		testDtCollector();

	}

	private static void testCache() {
		System.out.println("=====================================================");
		System.out.println("\n### Test Cache ###");
		
		Result result = JUnitCore.runClasses(TestCache.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		
		System.out.println("Successful: " + result.wasSuccessful());
		System.out.println("\n### End Test Cache ###\n");
	}
	
	private static void testLocalDbContainer() {
		System.out.println("=====================================================");
		System.out.println("\n### Test LocalDbContainer ###");
		
		Result result = JUnitCore.runClasses(TestLocalDbContainer.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		
		System.out.println("Successful: " + result.wasSuccessful());
		System.out.println("\n### End Test LocalDbContainer ###\n");
	}
	
	private static void testDtCollector() {
		System.out.println("=====================================================");
		System.out.println("\n### Test DtCollector ###");
		
		Result result = JUnitCore.runClasses(TestDtCollector.class);
		for (Failure failure : result.getFailures()) {
			System.out.println(failure.toString());
		}
		
		System.out.println("Successful: " + result.wasSuccessful());
		System.out.println("\n### End Test DtCollector ###\n");
	}
}
