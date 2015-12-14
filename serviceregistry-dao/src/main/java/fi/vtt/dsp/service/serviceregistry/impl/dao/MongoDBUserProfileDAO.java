package fi.vtt.dsp.service.serviceregistry.impl.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.types.ObjectId;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.impl.SpringApplicationContext;
import java.util.HashMap;

/**
 * @author ttehia
 * 
 */
public class MongoDBUserProfileDAO implements UserProfileDAO
{

	private DBCollection userProfileColl = null;
	private WriteConcern defaultWriteConcern = WriteConcern.SAFE;
	private MongoDBConnection mongoDB = null;

	private static final Logger LOGGER = Logger
			.getLogger(MongoDBUserProfileDAO.class.getName());

	public MongoDBUserProfileDAO() throws DAOGeneralSystemFault {
		if (mongoDB == null) {
			mongoDB = (MongoDBConnection) SpringApplicationContext
					.getBean("UPmongoDB");
		}
		userProfileColl = mongoDB.getDBCollection();
	}

	// This constructor is meant to be used only in junit testing context
	// allowing setting mongo db conn outside of the Spring context
	public MongoDBUserProfileDAO(MongoDBConnection mongoDB)
			throws DAOGeneralSystemFault {
		this.mongoDB = mongoDB;
		userProfileColl = mongoDB.getDBCollection();
	}

	@Override
	public String create(UserProfile userProfile) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault {
		DBObject existingUserProfile = null;
		String insertedId = "";

		try {
			// Check for existing email
			BasicDBObject query = new BasicDBObject().append("email",
					userProfile.getEmail());
			existingUserProfile = userProfileColl.findOne(query);
			if (existingUserProfile != null) {
				throw new DAONotSavedFault(
						"User profile entry with the same user email "
								+ userProfile.getEmail() + " exists already");
			}
			// Check for existing id
			if (userProfile.getUserId() != null
					&& userProfile.getUserId().length() > 0) {
				existingUserProfile = this.userProfileColl
						.findOne(new ObjectId(userProfile.getUserId()));

				if (existingUserProfile != null) {
					throw new DAONotSavedFault(
							"User profile entry with the same userId "
									+ userProfile.getUserId()
									+ " exists already");
				}
			}

			DBObject dbObj = DAOUtils.convertWSObjToDBObjViaJSON(userProfile,
					UserProfile.class);
			WriteResult wr = this.userProfileColl.insert(dbObj,
					this.defaultWriteConcern);
			insertedId = dbObj.get("_id").toString();

			userProfile.setUserId(insertedId);
			if (wr.getError() != null) {
				throw new DAONotSavedFault(
						"New user profile entry was not inserted "
								+ userProfile.getEmail() + " : "
								+ wr.getError());
			}
			// FIXME: This is to coverup of _id vs. userId mismatch. userId
			// should be made to PK
			BasicDBObject updatedUserId = new BasicDBObject("userId",
					insertedId);
			BasicDBObject updateQuery = new BasicDBObject("$set", updatedUserId);
			wr = this.userProfileColl.update(dbObj, updateQuery, false, false,
					this.defaultWriteConcern);
			if (wr.getError() != null || wr.getN() == 0) {
				throw new DAONotSavedFault(
						"(ID correction error) New user profile entry was not inserted  "
								+ userProfile.getUserId() + " : "
								+ wr.getError());
			}
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname. Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault(
					"Inserting newservice registry entry"
							+ userProfile.getEmail() + " failed, super duper",
					e);
		}
		return insertedId;
	}

	@Override
	public UserProfile findUserProfileByUserId(String userId)
			throws DAOGeneralSystemFault, DAONotFoundFault {
		DBObject dbObj = null;
		UserProfile userProfile = null;
		try {
			userProfileColl = mongoDB.getDBCollection();
			BasicDBObject userProfilePointer = new BasicDBObject("_id",
					new ObjectId(userId));
			dbObj = this.userProfileColl.findOne(userProfilePointer);

			if (dbObj != null) {
				userProfile = DAOUtils.convertDBObjToWSObjViaJSON(dbObj,
						UserProfile.class);
			} else {
				String dbIDStr = mongoDB.getDbCollectionName() + " at "
						+ mongoDB.getDbServerIP();
				throw new DAONotFoundFault("Requested user profile for "
						+ userId + " was not found " + dbIDStr);
			}
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault("Malformatted user id " + userId
					+ " ", e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Fetching user profile for "
					+ userId + " failed", e);
		}
		return userProfile;
	}

	@Override
	public UserProfile read(String userEmail) throws DAOGeneralSystemFault,
			DAONotFoundFault {
		UserProfile userProfile = null;
		try {
			if (userEmail == null) {
				return null;
			}

			BasicDBObject query = new BasicDBObject()
					.append("email", userEmail);
			DBCursor results = userProfileColl.find(query);
			if (results.hasNext()) {
				BasicDBObject dbobj = (BasicDBObject) results.next();
				UserProfile parsedProfile = null;
				parsedProfile = DAOUtils.convertDBObjToWSObjViaJSON(dbobj,
						UserProfile.class);
				return parsedProfile;
			}
		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE, "Failure while retrieving user profile "
					+ userEmail, e);
			throw new DAOGeneralSystemFault(
					"Failure while retrieving user profile " + userEmail, e);
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using user profile entry "
							+ userEmail + ".", e);
		}
		return userProfile;
	}

        // TODO: Agent-roles haven't been implemented here
	@Override
        public List<UserProfile> getAllByUser(String userProfileId) throws DAOGeneralSystemFault, DAONotFoundFault {
            HashMap<String, UserProfile> userProfiles = new HashMap<String, UserProfile>();

            MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
            List<UserGroup> userGroups = null;

            userGroups = userGroupDAO.getAllUserGroupsForUser(userProfileId);

            try {
                DBCursor results = userProfileColl.find();

                while (results.hasNext()) {
                    BasicDBObject dbobj = (BasicDBObject) results.next();
                    UserProfile userProfile = null;
                    userProfile = DAOUtils.convertDBObjToWSObjViaJSON(dbobj, UserProfile.class);
                    
                    for (UserGroup userGroup : userGroups) {
                        for (GroupRole groupRole : userGroup.getGroupRole()) {
                            if (userProfile.getUserId().equals(groupRole.getUserId())) {
                                userProfiles.put(userProfile.getUserId(), userProfile);
                                break;
                            }
                        }
                    }                    
                }
            } 
            catch (MongoException e) {
                LOGGER.log(Level.SEVERE, "Failure while retrieving user profiles", e);
                throw new DAOGeneralSystemFault("Failure while retrieving user profiles", e);
            }

            return new ArrayList<UserProfile>(userProfiles.values());
	}
        
        
	@Override
	public List<UserProfile> getAll() throws DAOGeneralSystemFault,
			DAONotFoundFault {
		List<UserProfile> userProfiles = new ArrayList<UserProfile>();
		try {
			DBCursor results = userProfileColl.find();
			while (results.hasNext()) {
				BasicDBObject dbobj = (BasicDBObject) results.next();
				UserProfile parsedProfile = null;
				parsedProfile = DAOUtils.convertDBObjToWSObjViaJSON(dbobj,
						UserProfile.class);
				userProfiles.add(parsedProfile);
			}
		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE, "Failure while retrieving user profiles",
					e);
			throw new DAOGeneralSystemFault(
					"Failure while retrieving user profiles", e);
		}
		return userProfiles;
	}

	@Override
	public boolean update(String userProfileId, UserProfile userProfile)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault {
		DBObject dbObj = null;
		if (userProfileId == null) {
			userProfileId = userProfile.getUserId();
		}
		if (userProfileId != null) {
			dbObj = this.userProfileColl.findOne(new ObjectId(userProfileId));
		}
		if (dbObj != null) {

			DBObject updatedUserProfileDB = DAOUtils
					.convertWSObjToDBObjViaJSON(userProfile, UserProfile.class);
			WriteResult wr = this.userProfileColl.update(dbObj,
					updatedUserProfileDB, false, false,
					this.defaultWriteConcern);

			if (wr.getError() != null || wr.getN() == 0) {
				throw new DAOUpdateFailedFault("User profile entry"
						+ userProfile.getUserId() + " update failed :"
						+ wr.getError());
			}
		}
		if (dbObj == null) {
			throw new DAONotFoundFault("User profile registry entry "
					+ userProfile.getUserId()
					+ " was not found in DB for updates");
		}
		return true;
	}

	@Override
	public boolean delete(String userProfileId) throws DAOGeneralSystemFault,
			DAONotFoundFault {
		DBObject dbObj = null;

		if (userProfileId != null) {
			dbObj = this.userProfileColl.findOne(new ObjectId(userProfileId));
		}
		if (dbObj == null) {
			throw new DAONotFoundFault("User profile entry " + userProfileId
					+ " to be deleted was not found in DB");
		}

		WriteResult wr = this.userProfileColl.remove(dbObj,
				this.defaultWriteConcern);
		if (wr.getError() != null || wr.getN() == 0) {
			throw new DAOGeneralSystemFault(wr.getError(), null);
		}

		return true;
	}

	@Override
	public boolean deleteUserProfileByEmail(String userEmail)
			throws DAOGeneralSystemFault, DAONotFoundFault {

		if (userEmail == null) {
			return false;
		}

		BasicDBObject query = new BasicDBObject().append("email", userEmail);
		DBCursor results = userProfileColl.find(query);
		if (results.hasNext()) {
			BasicDBObject dbObj = (BasicDBObject) results.next();
			if (dbObj == null) {
				throw new DAONotFoundFault("User profile entry " + userEmail
						+ " to be deleted was not found in DB");
			}

			WriteResult wr = this.userProfileColl.remove(dbObj,
					this.defaultWriteConcern);
			if (wr.getError() != null || wr.getN() == 0) {
				throw new DAOGeneralSystemFault(wr.getError(), null);
			}

			return true;

		} else {
			throw new DAONotFoundFault("User profile entry " + userEmail
					+ " to be deleted was not found in DB");
		}
	}

}
