package fi.vtt.dsp.service.serviceregistry.impl.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryLogEntry;
import fi.vtt.dsp.service.serviceregistry.impl.SpringApplicationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KafkaLogDAO {
    private static final Logger LOGGER = Logger.getLogger(KafkaLogDAO.class.getName());
    private DBCollection kafkaLogDBCOllection = null;
    final private MongoDBConnection mongoDBConnection;
    final private WriteConcern defaultWriteConcern = WriteConcern.SAFE;

    public KafkaLogDAO() throws DAOGeneralSystemFault {
        mongoDBConnection = (MongoDBConnection) SpringApplicationContext.getBean("KafkaLogMongoDB");
        kafkaLogDBCOllection = mongoDBConnection.getDBCollection();
    }

    public KafkaLogDAO(MongoDBConnection mongoDBConnection) throws DAOGeneralSystemFault {
		this.mongoDBConnection = mongoDBConnection;
		kafkaLogDBCOllection = mongoDBConnection.getDBCollection();
	}
    
    public List<ServiceRegistryLogEntry> getAllLogsForServiceInstance(String serviceId, String instanceId) {
        ArrayList<ServiceRegistryLogEntry> logItems = new ArrayList<>();
        BasicDBObject queryDBObject = new BasicDBObject();
        DBCursor dBCursor;
        
        queryDBObject.append("hostingServiceId", serviceId);
        queryDBObject.append("hostingInstanceId", instanceId);
        
        dBCursor = kafkaLogDBCOllection.find(queryDBObject);
        
        LOGGER.log(Level.FINE, String.format("Found %d log-items", dBCursor.count()));
        
        while (dBCursor.hasNext()) {
            DBObject logItemDBObject = dBCursor.next();
            ServiceRegistryLogEntry logItem;
                    
            try {
                logItem = DAOUtils.convertDBObjToWSObjViaJSON((BasicDBObject) logItemDBObject, ServiceRegistryLogEntry.class);
                logItems.add(logItem);
            }
            catch (DAOGeneralSystemFault e) {
                LOGGER.log(Level.SEVERE, "Cannot convert log-item from database-object to pojo.", e);
            }
        }
        
        return logItems;
    }
    
    public List<ServiceRegistryLogEntry> getAllLogsForServiceInstanceForTimeInterval(String serviceId, String instanceId, String startTime, String endTime) {
        ArrayList<ServiceRegistryLogEntry> logItems = new ArrayList<>();
        BasicDBObject queryDBObject = new BasicDBObject();
        DBCursor dBCursor;
        
        queryDBObject.append("hostingServiceId", serviceId);
        queryDBObject.append("hostingInstanceId", instanceId);

        DBObject queryObject = QueryBuilder.start().and(queryDBObject, 
                QueryBuilder.start("timeStamp").
                        greaterThan(startTime).
                        lessThan(endTime).get()).get();
        
        dBCursor = kafkaLogDBCOllection.find(queryObject);
        
        LOGGER.log(Level.FINE, String.format("Found %d log-items", dBCursor.count()));
        
        while (dBCursor.hasNext()) {
            DBObject logItemDBObject = dBCursor.next();
            ServiceRegistryLogEntry logItem;
                    
            try {
                logItem = DAOUtils.convertDBObjToWSObjViaJSON((BasicDBObject) logItemDBObject, ServiceRegistryLogEntry.class);
                logItems.add(logItem);
            }
            catch (DAOGeneralSystemFault e) {
                LOGGER.log(Level.SEVERE, "Cannot convert log-item from database-object to pojo.", e);
            }
        }
       
        return logItems;
    }

    public void insertLogItem(ServiceRegistryLogEntry logItem) throws DAOUpdateFailedFault, DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault {
        try {
            DBObject dbObj = DAOUtils.convertWSObjToDBObjViaJSON(logItem, ServiceRegistryLogEntry.class);
            WriteResult writeResult = kafkaLogDBCOllection.insert(dbObj, this.defaultWriteConcern);

            if (writeResult.getError() != null) {
                LOGGER.log(Level.SEVERE, "Cannot save logItem");
                throw new DAONotSavedFault("Couldn't insert new user-group:  " + writeResult.getError());
            }

        } catch (IllegalArgumentException | MongoException e) {
            LOGGER.log(Level.SEVERE, "Cannot save logItem", e);
            throw new DAOGeneralSystemFault("Error inserting user-group", e);
        }
    }
}
