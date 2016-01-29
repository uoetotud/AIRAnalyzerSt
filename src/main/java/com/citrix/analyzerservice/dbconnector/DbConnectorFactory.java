package com.citrix.analyzerservice.dbconnector;

/**
 * @author Xi Luo
 *
 */
public class DbConnectorFactory {

	public IDbConnector getDbContainer(String db) {
		if (db == null)
			return null;
		
		if (db.equalsIgnoreCase("LOCAL"))
			return new LocalDbContainer();
		
//		if (db.equalsIgnoreCase("S3"))
//			return new S3DbConnector();
		
		return null;
	}
}
