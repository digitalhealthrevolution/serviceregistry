package fi.vtt.dsp.service.serviceregistry.impl.dao;

import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.impl.SpringApplicationContext;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.types.ObjectId;

public class MongoDBUserGroupDAO implements UserGroupDAO {
	private static final Logger LOGGER = Logger
			.getLogger(MongoDBUserGroupDAO.class.getName());
	private DBCollection userGroupDBCOllection = null;
	private MongoDBConnection mongoDBConnection;
	private WriteConcern defaultWriteConcern = WriteConcern.SAFE;

	public MongoDBUserGroupDAO() throws DAOGeneralSystemFault {
		mongoDBConnection = (MongoDBConnection) SpringApplicationContext
				.getBean("UGmongoDB");
		userGroupDBCOllection = mongoDBConnection.getDBCollection();
	}

	// Testing purposes
	public MongoDBUserGroupDAO(MongoDBConnection mongoDBConnection)
			throws DAOGeneralSystemFault {
		this.mongoDBConnection = mongoDBConnection;
		userGroupDBCOllection = mongoDBConnection.getDBCollection();
	}

	public List<UserGroup> getAllUserGroups() throws DAOGeneralSystemFault {
		List<UserGroup> userGroups = new ArrayList<UserGroup>();

		try {
			DBCursor dBCursor = userGroupDBCOllection.find();

			while (dBCursor.hasNext()) {
				BasicDBObject basicDBObject = (BasicDBObject) dBCursor.next();
				UserGroup userGroup = DAOUtils.convertDBObjToWSObjViaJSON(basicDBObject, UserGroup.class);

				userGroups.add(userGroup);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Cannot get all user-groups. ", e);
			throw new DAOGeneralSystemFault("Cannot get all user-groups. ", e);
		}

		return userGroups;
	}

	public String insertNewUserGroup(UserGroup userGroup)
			throws DAOGeneralSystemFault,
			DAONotSavedFault {
		DBObject existingDBObject;
		String insertedId = null;

		try {
			if (userGroup.getUserGroupId() != null) {
				BasicDBObject queryObject = new BasicDBObject();
				queryObject.append("userGroupId", userGroup.getUserGroupId());

				existingDBObject = userGroupDBCOllection.findOne(queryObject);

				if (existingDBObject != null) {
					throw new DAONotSavedFault(
							"UserGroup with same ID already exists!");
				}
			}

			DBObject dbObj = DAOUtils.convertWSObjToDBObjViaJSON(userGroup,
					UserGroup.class);
			WriteResult writeResult = this.userGroupDBCOllection.insert(dbObj,
					this.defaultWriteConcern);
			insertedId = dbObj.get("_id").toString();

			userGroup.setUserGroupId(insertedId);

			if (writeResult.getError() != null) {
				throw new DAONotSavedFault("Couldn't insert new user-group "
						+ userGroup.getUserGroupId() + " : "
						+ writeResult.getError());
			}

			// FIXME: This is to coverup of _id vs. userId mismatch. userId
			// should be made to PK
			BasicDBObject updatedUserGroupId = new BasicDBObject("userGroupId",
					insertedId);
			BasicDBObject updateQuery = new BasicDBObject("$set",
					updatedUserGroupId);
			writeResult = this.userGroupDBCOllection.update(dbObj, updateQuery,
					false, false, this.defaultWriteConcern);

			if (writeResult.getError() != null || writeResult.getN() == 0) {
				throw new DAONotSavedFault(
						"(ID correction error) New user-group was not inserted  "
								+ userGroup.getUserGroupId() + " : "
								+ writeResult.getError());
			}
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault("Error inserting user-group", e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Error inserting user-group", e);
		}

		return insertedId;
	}

	public boolean deleteUserGroup(String userGroupId)
			throws DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault {
		DBObject dBObject = null;

		if (userGroupId != null) {
			dBObject = userGroupDBCOllection.findOne(new ObjectId(userGroupId));
		}

		if (dBObject == null) {
			LOGGER.log(Level.WARNING, "Cannot find user-group to delete. ID: "
					+ userGroupId);
			throw new DAONotFoundFault("Cannot find user-group to delete. ID: "
					+ userGroupId);
		}

		WriteResult writeResult = userGroupDBCOllection.remove(dBObject,
				defaultWriteConcern);

		if (writeResult.getError() != null || writeResult.getN() == 0) {
			throw new DAOGeneralSystemFault(writeResult.getError(), null);
		}

		return true;
	}

	public boolean updateUserGroup(UserGroup userGroup)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault {
		DBObject dBObject = null;

		if (userGroup.getUserGroupId() != null) {
			dBObject = userGroupDBCOllection.findOne(new ObjectId(userGroup
					.getUserGroupId()));
		}

		if (dBObject == null) {
			LOGGER.log(
					Level.WARNING,
					"Cannot find existing user-group with id: "
							+ userGroup.getUserGroupId());
			throw new DAONotFoundFault(
					"Cannot find existing user-group with id: "
							+ userGroup.getUserGroupId());
		}

		DBObject updaterObject = DAOUtils.convertWSObjToDBObjViaJSON(userGroup,
				UserGroup.class);

		WriteResult writeResult = userGroupDBCOllection.update(dBObject,
				updaterObject, false, false, defaultWriteConcern);

		if (writeResult.getError() != null || writeResult.getN() == 0) {
			LOGGER.log(Level.SEVERE, "Error updating user-group. ID: "
					+ userGroup.getUserGroupId() + writeResult.getError());
			throw new DAOUpdateFailedFault("Error updating user-group. ID: "
					+ userGroup.getUserGroupId());
		}

		return true;
	}

	public List<GroupRole> getAllGroupRolesForUser(String userProfileId) throws DAOGeneralSystemFault {
        BasicDBObject dBObject = new BasicDBObject();
        dBObject.append("groupRole.userId", userProfileId);
        DBCursor dBCursor = userGroupDBCOllection.find(dBObject);
        List<GroupRole> groupRoles = new ArrayList<>();

        LOGGER.log(Level.INFO, "Number of rules fetched for user: " + dBCursor.count());

        while (dBCursor.hasNext()) {
            DBObject dBObjectIter = dBCursor.next();
            GroupRole groupRole = DAOUtils.convertDBObjToWSObjViaJSON(dBObjectIter, GroupRole.class);

            groupRoles.add(groupRole);
        }

        return groupRoles;
    }
	public UserGroup getUserGroupById(String userGroupId)
			throws DAOGeneralSystemFault, DAONotFoundFault {
		DBObject dBObject = null;

		if (userGroupId != null) {
			dBObject = userGroupDBCOllection.findOne(new ObjectId(userGroupId));
		}

		if (dBObject == null) {
			LOGGER.log(Level.WARNING,
					"Cannot find existing user-group with id: " + userGroupId);
			throw new DAONotFoundFault(
					"Cannot find existing user-group with id: " + userGroupId);
		}

		UserGroup userGroup = DAOUtils.convertDBObjToWSObjViaJSON(dBObject,
				UserGroup.class);

		return userGroup;
	}

	public List<UserGroup> getAllUserGroupsForUser(String userId) throws DAOGeneralSystemFault {  
        BasicDBObject dBObject = new BasicDBObject();
        dBObject.append("groupRole.userId", userId);
        DBCursor dBCursor = userGroupDBCOllection.find(dBObject);
        List<UserGroup> userGroups = new ArrayList<>();
        
        while (dBCursor.hasNext()) {
            DBObject dBObjectIter = dBCursor.next();
            UserGroup userGroup = DAOUtils.convertDBObjToWSObjViaJSON(dBObjectIter, UserGroup.class);
            
            userGroups.add(userGroup);
        }
        
        return userGroups;
    }
}
