package fi.vtt.dsp.service.serviceregistry.impl.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;
import java.util.Map.Entry;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.description.ServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.common.description.UserRating;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.SpringApplicationContext;

public class MongoDBServiceRegistryDAO implements ServiceRegistryDAO {

	private static final Logger LOGGER = Logger
			.getLogger(MongoDBServiceRegistryDAO.class.getName());

	private WriteConcern defaultWriteConcern = WriteConcern.SAFE;

	private DBCollection serviceRegistryEntryColl = null;

	private MongoDBConnection mongoDBConn;
	// To access user profiles in DAOUtils
	private MongoDBConnection uPmongoDBConn;
	// For user-group -access
	private MongoDBConnection uGmongoDBConn;

	public MongoDBServiceRegistryDAO() throws DAOGeneralSystemFault {
		if (mongoDBConn == null) {
			mongoDBConn = (MongoDBConnection) SpringApplicationContext
					.getBean("mongoDB");
			uPmongoDBConn = (MongoDBConnection) SpringApplicationContext
					.getBean("UPmongoDB");
			uGmongoDBConn = (MongoDBConnection) SpringApplicationContext
					.getBean("UGmongoDB");
		}
		// TODO: Is this reasonable, fetch new collection everytime DAO method
		// is invoked?
		serviceRegistryEntryColl = mongoDBConn.getDBCollection();
	}

	// This constructor is meant to be used in junit testing context. It allows
	// setting mongodb conn to both DBs including user profiles connection
	// outside of the Spring context
	public MongoDBServiceRegistryDAO(MongoDBConnection mongoDBConn,
			MongoDBConnection uPmongoDBConn) throws DAOGeneralSystemFault {
		this.mongoDBConn = mongoDBConn;
		this.uPmongoDBConn = uPmongoDBConn;
		serviceRegistryEntryColl = mongoDBConn.getDBCollection();
	}

	public void setMongoDBConnection(MongoDBConnection mongoDB) {
		this.mongoDBConn = mongoDB;
	}

	@Override
	public List<ServiceRegistryEntry> getAll() throws DAOGeneralSystemFault {

		List<ServiceRegistryEntry> sRegEntryList = new ArrayList<ServiceRegistryEntry>(
				0);
		DBCursor cursor = this.serviceRegistryEntryColl.find();
		try {
			while (cursor.hasNext()) {
				DBObject dbObj = cursor.next();
				sRegEntryList.add(DAOUtils.convertDBObjToWSObjViaJSON(dbObj,
						ServiceRegistryEntry.class));
			}
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault(
					"Fetching a list of service registry entries failed, super duper",
					e);
		} catch (Exception e) {
			throw new DAOGeneralSystemFault(
					"Fetching a list of service registry entries failed, super duper",
					e);
		}
		return sRegEntryList;
	}

	@Override
    public List<ServiceRegistryEntry> findAll(Set<Entry<String, List<String>>> queryParams, int top, int tail, int startSection, int endSection, String userId) throws DAOGeneralSystemFault {
        LOGGER.log(Level.FINE, "findAll with userId");
        
        List<ServiceRegistryEntry> sRegEntryList = new ArrayList<>(0);
        DBCursor cursor = null;

        BasicDBObject servDescQuery = null;
        BasicDBObject queryObject;
        QueryBuilder qBuilder = new QueryBuilder();
        List<String> serviceIds = new ArrayList<>();
        
        if (userId != null) {
            MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
            List<UserGroup> userGroups = userGroupDAO.getAllUserGroupsForUser(userId);
			for (UserGroup userGroup : userGroups) {
				for (String serviceId : userGroup.getServiceRegistryEntryId()) {
					serviceIds.add(serviceId);
				}
			}
        }

        queryObject = (BasicDBObject) QueryBuilder.start().or(
            QueryBuilder.start("serviceId").in(serviceIds).get(),
            QueryBuilder.start("serviceDescription.createdByUserId").is(userId).get(),
            QueryBuilder.start("serviceDescription.ownerGroup").is("public").get()
        ).get();            
  
        if (queryParams != null) {
            for (Entry<String, List<String>> entry : queryParams) {
                //top, section and tail pagination attributes are handled later and not to be mixed with the query
                if (!(entry.getKey().equals("top") || entry.getKey().equals("section") || entry.getKey().equals("tail") || entry.getKey().equals("_s") || entry.getKey().equals("fields"))) {
                    if (entry.getKey().equals("search_keywords")) {
                        //TODO: Get rid off of the stupid URL scheme of ?search_keywords={blah,blah}, replace e.g. with ?keywords=blah,blah
                        List<String> keywordsList = DAOUtils.parseKeywordQueryParams(entry.getValue().get(0));
                        qBuilder = qBuilder.put("serviceDescription.keywords");
                        qBuilder = qBuilder.all(keywordsList);
                    } else {
                        qBuilder = qBuilder.put(entry.getKey()).is(entry.getValue().get(0));
                    }
                }
            }

            DBObject dbQuery = QueryBuilder
				.start()
				.and(
                    qBuilder.get(), 
                    queryObject
                ).get();
            
            servDescQuery = (BasicDBObject) dbQuery;
        }
    
        if (servDescQuery != null) {
            try {
                if (top > 0) {
                    cursor = this.serviceRegistryEntryColl.find(servDescQuery).limit(top);
                }
                if (tail > 0) {
                    //Quick and dirty
                    long cursorLength = this.serviceRegistryEntryColl.count(servDescQuery);
                    cursor = this.serviceRegistryEntryColl.find(servDescQuery).skip((int) (cursorLength - tail));
                }
                if (startSection > 0 && endSection > 0) {
                    cursor = this.serviceRegistryEntryColl.find(servDescQuery).skip(startSection).limit(endSection);
                }
                if (cursor == null) {
                    cursor = this.serviceRegistryEntryColl.find(servDescQuery);
                }
                if (cursor != null) {
                    sRegEntryList = new ArrayList<>(cursor.size());
                    LOGGER.log(Level.FINE, "MongoDBServiceRegistryDAO.findAll with query {0} returned a cursor with size : {1}", new Object[]{servDescQuery, cursor.size()});
                    while (cursor.hasNext()) {
                        DBObject dbObj = cursor.next();
                        sRegEntryList.add(DAOUtils.convertDBObjToWSObjViaJSON(dbObj, ServiceRegistryEntry.class));
                    }
                }
            } catch (MongoException e) {
                throw new DAOGeneralSystemFault("Fetching a list of service registry entries failed, super duper", e);
            } catch (Exception e) {
                throw new DAOGeneralSystemFault("Fetching a list of service registry entries failed, super duper", e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return sRegEntryList;
    }
	// TEMP

	@Override
	public List<ServiceRegistryEntry> findAll(
			Set<Entry<String, List<String>>> queryParams, int top, int tail,
			int startSection, int endSection) throws DAOGeneralSystemFault {
		List<ServiceRegistryEntry> sRegEntryList = new ArrayList<ServiceRegistryEntry>(
				0);
		DBCursor cursor = null;

		BasicDBObject servDescQuery = null;
		QueryBuilder qBuilder = new QueryBuilder();

		if (queryParams != null) {
			for (Entry<String, List<String>> entry : queryParams) {
				// top, section and tail pagination attributes are handled later
				// and not to be mixed with the query
				if (!(entry.getKey().equals("top")
						|| entry.getKey().equals("section")
						|| entry.getKey().equals("tail")
						|| entry.getKey().equals("_s") || entry.getKey()
						.equals("fields"))) {
					if (entry.getKey().equals("search_keywords")) {
						// TODO: Get rid off of the stupid URL scheme of
						// ?search_keywords={blah,blah}, replace e.g. with
						// ?keywords=blah,blah
						List<String> keywordsList = DAOUtils
								.parseKeywordQueryParams(entry.getValue()
										.get(0));
						qBuilder = qBuilder.put("serviceDescription.keywords");
						qBuilder = qBuilder.all(keywordsList);
					} else {
						qBuilder = qBuilder.put(entry.getKey()).is(
								entry.getValue().get(0));
					}
				}
			}

			DBObject dbQuery = qBuilder.get();
			servDescQuery = (BasicDBObject) dbQuery;
		}
		if (servDescQuery != null) {
			try {
				if (top > 0) {
					cursor = this.serviceRegistryEntryColl.find(servDescQuery)
							.limit(top);
				}
				if (tail > 0) {
					// Quick and dirty
					long cursorLength = this.serviceRegistryEntryColl
							.count(servDescQuery);
					cursor = this.serviceRegistryEntryColl.find(servDescQuery)
							.skip((int) (cursorLength - tail));
				}
				if (startSection > 0 && endSection > 0) {
					cursor = this.serviceRegistryEntryColl.find(servDescQuery)
							.skip(startSection).limit(endSection);
				}
				if (cursor == null) {
					cursor = this.serviceRegistryEntryColl.find(servDescQuery);
				}
				if (cursor != null) {
					sRegEntryList = new ArrayList<>(
							cursor.size());
					LOGGER.log(
							Level.FINE,
							"MongoDBServiceRegistryDAO.findAll with query {0} returned a cursor with size : {1}",
							new Object[]{servDescQuery, cursor.size()});
					while (cursor.hasNext()) {
						DBObject dbObj = cursor.next();
						sRegEntryList.add(DAOUtils.convertDBObjToWSObjViaJSON(
								dbObj, ServiceRegistryEntry.class));
					}
				}
			} catch (MongoException e) {
				throw new DAOGeneralSystemFault(
						"Fetching a list of service registry entries failed, super duper",
						e);
			} catch (Exception e) {
				throw new DAOGeneralSystemFault(
						"Fetching a list of service registry entries failed, super duper",
						e);
			} finally {
				if (cursor != null) {
					cursor.close();
				}
			}
		}

		return sRegEntryList;
	}

	// TEMP

	@Override
	public ServiceRegistryEntry findServiceRegistryEntry(String serviceId)
			throws DAOGeneralSystemFault, DAONotFoundFault {
		DBObject dbObj = null;
		ServiceRegistryEntry dbRegistryEntry = null;
		try {
			dbObj = this.serviceRegistryEntryColl.findOne(new ObjectId(
					serviceId));

			if (dbObj != null) {
				dbRegistryEntry = DAOUtils.convertDBObjToWSObjViaJSON(dbObj,
						ServiceRegistryEntry.class);
			} else {
				throw new DAONotFoundFault("Requested service registry entry"
						+ serviceId + " was not found");
			}
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Fetching service registry entry"
					+ serviceId + " failed, super duper", e);
		}
		return dbRegistryEntry;
	}

	public String insertNewServiceRegistryEntry(ServiceRegistryEntry sRegEntr)
			throws DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault,
			DAOUpdateFailedFault {
		String insertedServiceId = "";
		DBObject existingService = null;
		try {
			if (sRegEntr.getServiceId() != null) {
				existingService = this.serviceRegistryEntryColl
						.findOne(new ObjectId(sRegEntr.getServiceId()));
				if (existingService != null) {
					throw new DAONotSavedFault(
							"Service registry entry with the same serviceId "
									+ sRegEntr.getServiceId()
									+ " exists already, bozo! The service register is supposed to issue service ids, you nincumpoop!");
				}
			}

			if (existingService == null) {
				// Check if the *ByUserId userid is valid, throws exception
				// DAONotSavedFault
				DAOUtils.checkUserIdValidityInServiceRegistryEntry(
						uPmongoDBConn, sRegEntr);
				// Set current time as creation and modified on dates
				long timestamp = DAOUtils.getCurrentDateAsUnixEpoch();
				sRegEntr.getServiceDescription().setCreatedOnDate(timestamp);
				sRegEntr.getServiceDescription().setModifiedOnDate(timestamp);

				for (ServiceInstance sI : sRegEntr.getServiceInstance()) {
					sI.setCreatedOnDate(timestamp);
					sI.setModifiedOnDate(timestamp);
				}

				DBObject dbObj = DAOUtils.convertWSObjToDBObjViaJSON(sRegEntr,
						ServiceRegistryEntry.class);
				WriteResult wr = this.serviceRegistryEntryColl.insert(dbObj,
						this.defaultWriteConcern);
				insertedServiceId = dbObj.get("_id").toString();
				if (wr.getError() != null) {
					throw new DAONotSavedFault(
							"New service registry entry was not inserted in the service registry entry "
									+ sRegEntr.getServiceId() + " : "
									+ wr.getError());
				}
				// FIXME: This is to coverup of _id vs. serviceId mismatch.
				// serviceId should be made to PK
				BasicDBObject updatedServiceId = new BasicDBObject("serviceId",
						insertedServiceId);
				BasicDBObject updateQuery = new BasicDBObject("$set",
						updatedServiceId);

				wr = this.serviceRegistryEntryColl.update(dbObj, updateQuery,
						false, false, this.defaultWriteConcern);
				if (wr.getError() != null || wr.getN() == 0) {
					throw new DAONotSavedFault(
							"(ID correction error) New service registry entry was not inserted in the service registry entry "
									+ sRegEntr.getServiceId()
									+ " : "
									+ wr.getError());
				}
			}
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname. Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault(
					"Inserting newservice registry entry"
							+ sRegEntr.getServiceId() + " failed, super duper",
					e);
		}

		return insertedServiceId;
	}

	@Override
	public String updateServiceRegistryEntry(String serviceId,
			ServiceRegistryEntry sRegEntr) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault, DAONotSavedFault {
		DBObject dbObj = null;
		String updatedServiceRegEntryId = null;
		sRegEntr.setServiceId(serviceId);

		if (serviceId != null) {
			dbObj = this.serviceRegistryEntryColl.findOne(new ObjectId(
					serviceId));
		}
		if (dbObj != null) {
			// Check if the *ByUserId userid is valid, throws exception
			// DAONotSavedFault
			DAOUtils.checkUserIdValidityInServiceRegistryEntry(uPmongoDBConn,
					sRegEntr);
			// Set current time as modified on date
			if (sRegEntr.getServiceDescription() != null) {
				sRegEntr.getServiceDescription().setModifiedOnDate(
						DAOUtils.getCurrentDateAsUnixEpoch());
			}
			DBObject updatedServiceRegistryEntryDB = DAOUtils
					.convertWSObjToDBObjViaJSON(sRegEntr,
							ServiceRegistryEntry.class);

			// Preserve orig created on date
			ObjectId idObj = (ObjectId) dbObj.get("_id");
			long origCreatedOnDate = idObj.getTime();

			if (updatedServiceRegistryEntryDB
					.containsField("serviceDescription")
					&& updatedServiceRegistryEntryDB.get("serviceDescription") != null) {
				((BasicDBObject) ((BasicDBObject) updatedServiceRegistryEntryDB)
						.get("serviceDescription")).put("createdOnDate",
						origCreatedOnDate);
			}

			WriteResult wr = this.serviceRegistryEntryColl.update(dbObj,
					updatedServiceRegistryEntryDB, false, false,
					this.defaultWriteConcern);

			if (wr.getError() != null || wr.getN() == 0) {
				throw new DAOUpdateFailedFault("Service Registry entry"
						+ sRegEntr.getServiceId() + " update failed :"
						+ wr.getError());
			}
			updatedServiceRegEntryId = sRegEntr.getServiceId();
		}
		if (dbObj == null) {
			throw new DAONotFoundFault("Service registry entry "
					+ sRegEntr.getServiceId()
					+ " was not found in DB for updates");
		}
		return updatedServiceRegEntryId;
	}

	@Override
	public void deleteServiceRegistryEntry(String serviceId)
			throws DAOGeneralSystemFault, DAONotFoundFault {
		DBObject dbObj = null;

		if (serviceId != null) {
			dbObj = this.serviceRegistryEntryColl.findOne(new ObjectId(
					serviceId));
		}
		if (dbObj == null) {
			throw new DAONotFoundFault("Service registry entry " + serviceId
					+ " to be deleted was not found in DB");
		}

		WriteResult wr = this.serviceRegistryEntryColl.remove(dbObj,
				this.defaultWriteConcern);
		if (wr.getError() != null || wr.getN() == 0) {
			throw new DAOGeneralSystemFault(wr.getError(), null);
		}
	}

	public void updateUserRating(String serviceId)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault, DAONotSavedFault {
		ServiceRegistryEntry serviceRegistryEntry = findServiceRegistryEntry(serviceId);
		if (serviceRegistryEntry == null) {
			return;
		}

		ServiceDescription serviceDescription = serviceRegistryEntry
				.getServiceDescription();
		if (serviceDescription == null) {
			return;
		}

		List<UserFeedback> userFeedbacks = serviceDescription.getUserFeedback();
		int sum = 0;
		int count = 0;
		for (UserFeedback userFeedback : userFeedbacks) {
			sum += userFeedback.getUserRating();
			count++;
		}

		if (count > 0) {
			UserRating userRating = serviceDescription.getUserRating();
			if (userRating == null) {
				userRating = new UserRating();
			}
			userRating.setRating(Math.round((float) sum / (float) count));
			serviceDescription.setUserRating(userRating);
			serviceRegistryEntry.setServiceDescription(serviceDescription);
			updateServiceRegistryEntry(serviceId, serviceRegistryEntry);
		}
	}
}
