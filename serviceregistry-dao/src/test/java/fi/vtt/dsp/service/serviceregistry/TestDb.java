package fi.vtt.dsp.service.serviceregistry;

import com.mongodb.DBCollection;
import fi.vtt.dsp.service.serviceregistry.impl.dao.*;

public class TestDb {

	private TestDb() {
	}

	public static MongoDBConnection getDbConnection(String collectionName) {
		MongoDBConnection conn = new MongoDBConnection();
		conn.setDbServerIP(TestProperties.get(TestProperty.DB_ADDRESS));
		conn.setDbServerPort(Integer.parseInt(TestProperties.get(TestProperty.DB_PORT)));
		conn.setDbUserName(TestProperties.get(TestProperty.DB_USER_NAME));
		conn.setDbPassword(TestProperties.get(TestProperty.DB_USER_PWD));
		conn.setDbName(TestProperties.get(TestProperty.DB_NAME));
		conn.setDbCollectionName(collectionName);
		return conn;
	}

	public static void cleanEntries(MongoDBConnection conn)
			throws DAOGeneralSystemFault {
		DBCollection dbColl = conn.getDBCollection();
		dbColl.drop();
	}
}
