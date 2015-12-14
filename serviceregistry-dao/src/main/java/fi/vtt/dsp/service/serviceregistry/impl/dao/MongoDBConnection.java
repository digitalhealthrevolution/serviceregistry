package fi.vtt.dsp.service.serviceregistry.impl.dao;

import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;

public class MongoDBConnection {

	private static final Logger LOGGER = Logger
			.getLogger(MongoDBConnection.class.getName());

	private MongoClient mongoClient;
	private DB db;
	private DBCollection serviceRegistryEntryColl;

	// Injected by Spring according to the WEB-INF/beans.xml
	private String dbUserName = "";
	private String dbPassword = "";
	private String dbName = "";
	private String dbCollectionName = "";
	private String dbServerIP = "";
	private int dbServerPort;

	public MongoDBConnection() {
	}

	public DBCollection getDBCollection() throws DAOGeneralSystemFault {
		if (serviceRegistryEntryColl != null) {
			return serviceRegistryEntryColl;
		} else {
			openAndAuthenticateDB();
			return serviceRegistryEntryColl;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		this.closeDB();
		super.finalize();
	}

	private boolean openAndAuthenticateDB() throws DAOGeneralSystemFault {
		try {
			this.mongoClient = new MongoClient(dbServerIP, dbServerPort);
		} catch (UnknownHostException e) {
			LOGGER.log(Level.SEVERE,
					"Unknown host exception while connecting to the DB at "
							+ dbServerIP + ":" + dbServerPort);
			throw new DAOGeneralSystemFault(
					"Unknown host exception while connecting to the DB at "
							+ dbServerIP + ":" + dbServerPort, e);
		}
		this.db = mongoClient.getDB(dbName);
		boolean auth = db.authenticate(dbUserName, (dbPassword.toCharArray()));
		this.serviceRegistryEntryColl = db.getCollection(dbCollectionName);

		if (!auth) {
			LOGGER.log(Level.SEVERE, "User " + dbUserName + "/" + dbPassword
					+ " not authorized to the DB " + dbName
					+ " and collection " + dbCollectionName);
			throw new DAOGeneralSystemFault("User " + dbUserName + "/"
					+ dbPassword + " not authorized to the DB " + dbName
					+ " and collection " + dbCollectionName, null);
		}
		LOGGER.log(Level.FINE, "MongoDB connection succesfully opened to "
				+ dbServerIP + ":" + dbServerPort + " for user " + dbUserName
				+ "/" + dbPassword + " to DB " + dbName + " and collection "
				+ dbCollectionName);
		return auth;
	}

	private void closeDB() {
		try {
			if( this.mongoClient != null ) {
				this.mongoClient.close();
			}
			this.db = null;
			this.serviceRegistryEntryColl = null;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
		}
	}

	// Injected by Spring according to the WEB-INF/beans.xml
	public String getDbUserName() {
		return dbUserName;
	}

	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
	}

	public String getDbPassword() {
		return dbPassword;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbCollectionName() {
		return dbCollectionName;
	}

	public void setDbCollectionName(String dbCollectionName) {
		this.dbCollectionName = dbCollectionName;
	}

	public String getDbServerIP() {
		return dbServerIP;
	}

	public void setDbServerIP(String dbServerIP) {
		this.dbServerIP = dbServerIP;
	}

	public int getDbServerPort() {
		return dbServerPort;
	}

	public void setDbServerPort(int dbServerPort) {
		this.dbServerPort = dbServerPort;
	}

}
