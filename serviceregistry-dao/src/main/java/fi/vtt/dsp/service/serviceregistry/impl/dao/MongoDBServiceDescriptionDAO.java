package fi.vtt.dsp.service.serviceregistry.impl.dao;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.impl.SpringApplicationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MongoDBServiceDescriptionDAO implements ServiceDescriptionDAO {

	private static final Logger LOGGER = Logger
			.getLogger(MongoDBServiceDescriptionDAO.class.getName());
	private DBCollection serviceRegistryEntryColl = null;
	private MongoDBConnection mongoDB;

	public MongoDBServiceDescriptionDAO() throws DAOGeneralSystemFault {
		if (mongoDB == null) {
			mongoDB = (MongoDBConnection) SpringApplicationContext
					.getBean("mongoDB");
		}
		// TODO: Is this reasonable, fetch new collection everytime DAO method
		// is invoked?
		serviceRegistryEntryColl = mongoDB.getDBCollection();
	}

	@Override
	public TechnicalServiceDescription findTechnicalServiceDescription(
			String serviceId, String technicaldescriptionid)
			throws DAOGeneralSystemFault, DAONotFoundFault {
		TechnicalServiceDescription retTechDescr = null;
		DBObject dbObj = null;
		try {
			if ((serviceId != null) && (technicaldescriptionid != null)) {
				BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
						"_id", new ObjectId(serviceId));
				BasicDBObject arrayPointer = new BasicDBObject(
						"technicalDescriptionId", technicaldescriptionid);
				BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
						arrayPointer);
				serviceRegistryEntryPointer.append(
						"serviceDescription.technicalServiceDescription",
						arrayQuery);
				// Find out if service registry entry with technical service
				// description exists already
				dbObj = this.serviceRegistryEntryColl
						.findOne(serviceRegistryEntryPointer);

				if (dbObj != null) {
					BasicDBList techDescList = (BasicDBList) ((BasicDBObject) dbObj
							.get("serviceDescription"))
							.get("technicalServiceDescription");

					for (Object techDesc : techDescList) {
						if (((BasicDBObject) techDesc).get(
								"technicalDescriptionId").equals(
								technicaldescriptionid))
						{
							// Matching technical description found
							retTechDescr = DAOUtils.convertDBObjToWSObjViaJSON(
									(DBObject) techDesc,
									TechnicalServiceDescription.class);
							break;
						}
					}
				}
				if (dbObj == null) {
					throw new DAONotFoundFault(
							"No technical service description "
									+ technicaldescriptionid
									+ " was found in service registry entry "
									+ serviceId);
				}
			}
		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE, "Error finding technical description.", e);
			throw new DAOGeneralSystemFault(
					"Failure while retrieving technical service description "
							+ technicaldescriptionid
							+ " in service registry entry " + serviceId, e);
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		}
		return retTechDescr;
	}

	@Override
	public String insertNewTechnicalServiceDescription(String serviceId,
			TechnicalServiceDescription techDesc) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault {
		String newTechnicalServiceDescriptionId = "";
		DBObject dbObj = null;

		try {
			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject itemToBeInserted = new BasicDBObject(
					"technicalDescriptionId",
					techDesc.getTechnicalDescriptionId());
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					itemToBeInserted);
			serviceRegistryEntryPointer.append(
					"serviceDescription.technicalServiceDescription",
					arrayQuery);

			// Find out if service registry entry with technical service
			// description exists already
			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			// If it does - do not insert techncial service description anymore
			if (dbObj != null) {
				throw new DAONotSavedFault(
						"Service registry entry "
								+ serviceId
								+ " already has a technical service description with id "
								+ techDesc.getTechnicalDescriptionId());
			}
			// If it doesn't - insert technical service description

			BasicDBObject arrayInsertion = new BasicDBObject(
					"serviceDescription.technicalServiceDescription",
					DAOUtils.convertWSObjToDBObjViaJSON(techDesc,
							TechnicalServiceDescription.class));
			WriteResult wr = this.serviceRegistryEntryColl.update(
					new BasicDBObject("_id", new ObjectId(serviceId)),
					new BasicDBObject("$addToSet", arrayInsertion));

			if (wr != null) {
				if (wr.getError() != null) {
					throw new DAOUpdateFailedFault(
							"Technical service description was not inserted in the service registry entry "
									+ serviceId + " : " + wr.getError());
				}
				if (wr.getN() == 0) {
					throw new DAONotFoundFault("Service registry entry id "
							+ serviceId + " was not found");
				}
			}

			updateModifiedOnDateInServiceDescription(serviceId);
			newTechnicalServiceDescriptionId = techDesc
					.getTechnicalDescriptionId();

		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE,
					"Error inserting technical service description.", e);
			throw new DAOGeneralSystemFault(
					"Failure inserting new technical service description "
							+ techDesc.getTechnicalDescriptionId()
							+ " in service registry entry " + serviceId, e);
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		}
		return newTechnicalServiceDescriptionId;
	}

	public String updateTechnicalServiceDescription(String serviceId,
			String techServDescId,
			TechnicalServiceDescription updatedTechnicalServiceDescription)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault {
		DBObject dbObj = null;
		String updatedTechnicalServiceDescriptionId = "";

		// FIXME: Maybe this one should be checked if same in the beginning
		updatedTechnicalServiceDescription
				.setTechnicalDescriptionId(techServDescId);

		try {
			dbObj = this.serviceRegistryEntryColl.findOne(new ObjectId(
					serviceId));

			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject itemToBeUpdatedPointer = new BasicDBObject(
					"technicalDescriptionId", techServDescId);
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					itemToBeUpdatedPointer);
			serviceRegistryEntryPointer.append(
					"serviceDescription.technicalServiceDescription",
					arrayQuery);

			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			if (dbObj == null) {
				throw new DAONotFoundFault("Service registry entry "
						+ serviceId + " with technical service description "
						+ techServDescId + " to be updated was not found in DB");
			}

			BasicDBObject arrayUpdate = new BasicDBObject(
					"serviceDescription.technicalServiceDescription.$",
					(BasicDBObject) DAOUtils.convertWSObjToDBObjViaJSON(
							updatedTechnicalServiceDescription,
							TechnicalServiceDescription.class));

			WriteResult wr = null;

			BasicDBObject servRegEntryPointerWithArrayItem = serviceRegistryEntryPointer
					.append("serviceDescription.technicalServiceDescription.technicalDescriptionId",
							techServDescId);

			wr = this.serviceRegistryEntryColl.update(
					servRegEntryPointerWithArrayItem, new BasicDBObject("$set",
							arrayUpdate), true, false);

			if (wr != null && (wr.getError() != null || wr.getN() == 0)) {
				throw new DAOUpdateFailedFault("Service registry entry "
						+ serviceId + " with technical service description "
						+ techServDescId + " update failed");
			}
			updateModifiedOnDateInServiceDescription(serviceId);
			updatedTechnicalServiceDescriptionId = updatedTechnicalServiceDescription
					.getTechnicalDescriptionId();

		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Technical service description  "
					+ techServDescId
					+ " update failed in service registry entry " + serviceId,
					e);
		}
		return updatedTechnicalServiceDescriptionId;
	}

	@Override
	public String deleteTechnicalServiceDescription(String serviceId,
			String techServDescId) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotFoundFault {
		String retString = "";
		BasicDBObject serviceRegistryEntryPointer = new BasicDBObject("_id",
				new ObjectId(serviceId));
		BasicDBObject itemToBeDeletedPointer = new BasicDBObject(
				"technicalDescriptionId", techServDescId);
		BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
				itemToBeDeletedPointer);
		serviceRegistryEntryPointer.append(
				"serviceDescription.technicalServiceDescription", arrayQuery);

		try {
			BasicDBObject arrayManipulatedPointer = new BasicDBObject(
					"serviceDescription.technicalServiceDescription",
					itemToBeDeletedPointer);

			WriteResult wr = null;
			wr = this.serviceRegistryEntryColl.update(
					serviceRegistryEntryPointer, new BasicDBObject("$pull",
							arrayManipulatedPointer));

			if (wr != null) {
				if (wr.getError() != null) {
					throw new DAOUpdateFailedFault(
							"Technical service description was not deleted in service registry entry "
									+ serviceId + " : " + wr.getError());
				}
				if (wr.getN() == 0) {
					throw new DAONotFoundFault("Technical service description "
							+ techServDescId + " or service registry entry "
							+ serviceId + " was not found");
				}

				retString = Integer.toString(wr.getN());
			}

		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault(
					"Failure deleting technical service description "
							+ techServDescId + " in service registry entry "
							+ serviceId + " : " + e.getMessage(), e);
		}
		return retString;
	}

	public Dependency findDependency(String serviceId, String dependencyId)
			throws DAOGeneralSystemFault, DAONotFoundFault {
		Dependency retTechDescr = null;
		DBObject dbObj = null;
		try {
			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject arrayPointer = new BasicDBObject("dependencyId",
					dependencyId);
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					arrayPointer);
			serviceRegistryEntryPointer.append("serviceDescription.dependency",
					arrayQuery);

			// Find out if service registry entry with technical service
			// description exists already
			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			if (dbObj != null) {
				BasicDBList techDescList = (BasicDBList) ((BasicDBObject) dbObj
						.get("serviceDescription")).get("dependency");

				for (Object techDesc : techDescList) {
					if (((BasicDBObject) techDesc).get("dependencyId").equals(
							dependencyId))
					{
						// Matching technical description found
						retTechDescr = DAOUtils.convertDBObjToWSObjViaJSON(
								(DBObject) techDesc, Dependency.class);
						break;
					}
				}
			}
			if (dbObj == null) {
				throw new DAONotFoundFault("No dependency " + dependencyId
						+ " was found in service registry entry " + serviceId);
			}
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE, "Error finding dependency. Service ID: \""
					+ serviceId + "\" Dependecy ID: \"" + dependencyId + "\"",
					e);
			throw new DAOGeneralSystemFault(
					"Failure while retrieving dependency " + dependencyId
							+ " in service registry entry " + serviceId, e);
		}
		return retTechDescr;
	}

	@Override
	public String insertNewDependency(String serviceId, Dependency servDep)
			throws DAOGeneralSystemFault, DAONotSavedFault,
			DAOUpdateFailedFault, DAONotFoundFault {
		DBObject dbObj = null;
		String newDependencyId = "";
		try {
			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject itemIdToBeInserted = new BasicDBObject(
					"dependencyId", servDep.getDependencyId());
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					itemIdToBeInserted);

			serviceRegistryEntryPointer.append("serviceDescription.dependency",
					arrayQuery);
			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			if (dbObj != null) {
				throw new DAONotSavedFault("Service registry entry "
						+ serviceId + " already has dependency with "
						+ servDep.getDependencyId());
			}

			BasicDBObject arrayInsertion = new BasicDBObject(
					"serviceDescription.dependency",
					DAOUtils.convertWSObjToDBObjViaJSON(servDep,
							Dependency.class));
			WriteResult wr = this.serviceRegistryEntryColl.update(
					new BasicDBObject("_id", new ObjectId(serviceId)),
					new BasicDBObject("$addToSet", arrayInsertion));

			if (wr != null) {
				if (wr.getError() != null) {
					throw new DAOUpdateFailedFault(
							"Dependency was not updated in service registry entry "
									+ serviceId + " : " + wr.getError());
				}
				if (wr.getN() == 0) {
					throw new DAONotFoundFault("Dependency " + servDep
							+ " or service registry entry " + serviceId
							+ " was not found");
				}
			}

			updateModifiedOnDateInServiceDescription(serviceId);
			newDependencyId = servDep.getDependencyId();
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Failure updating dependency "
					+ servDep.getDependencyId() + " in service registry entry "
					+ serviceId + " : " + e.getMessage(), e);
		}
		return newDependencyId;
	}

	public String updateDependency(String serviceId, String servDepId,
			Dependency updatedServiceDependency) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault {
		DBObject dbObj = null;
		String updatedDependencyId = "";

		// FIXME: Maybe this one should be checked if same in the beginning
		updatedServiceDependency.setDependencyId(servDepId);

		try {
			dbObj = this.serviceRegistryEntryColl.findOne(new ObjectId(
					serviceId));

			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject itemToBeUpdatedPointer = new BasicDBObject(
					"dependencyId", servDepId);
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					itemToBeUpdatedPointer);
			serviceRegistryEntryPointer.append("serviceDescription.dependency",
					arrayQuery);

			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			if (dbObj == null) {
				throw new DAONotFoundFault("Service registry entry "
						+ serviceId + " with dependency " + servDepId
						+ " to be manipulated was not found in DB");
			}

			BasicDBObject arrayUpdate = new BasicDBObject(
					"serviceDescription.dependency.$",
					DAOUtils.convertWSObjToDBObjViaJSON(
							updatedServiceDependency, Dependency.class));

			WriteResult wr = null;

			BasicDBObject servRegEntryPointerWithArrayItem = serviceRegistryEntryPointer
					.append("serviceDescription.dependency.dependencyId",
							servDepId);

			wr = this.serviceRegistryEntryColl.update(
					servRegEntryPointerWithArrayItem, new BasicDBObject("$set",
							arrayUpdate), true, false);

			if (wr != null && (wr.getError() != null || wr.getN() == 0)) {
				throw new DAOUpdateFailedFault("Service registry entry "
						+ serviceId + " with dependency " + servDepId
						+ " update failed");
			}

			updateModifiedOnDateInServiceDescription(serviceId);
			updatedDependencyId = updatedServiceDependency.getDependencyId();

		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Dependency " + servDepId
					+ " update failed in service registry entry " + serviceId,
					e);
		}
		return updatedDependencyId;
	}

	public String deleteDependency(String serviceId, String dependencyId)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault {
		String retString = "";
		BasicDBObject serviceRegistryEntryPointer = new BasicDBObject("_id",
				new ObjectId(serviceId));
		BasicDBObject itemToBeDeletedPointer = new BasicDBObject(
				"dependencyId", dependencyId);
		BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
				itemToBeDeletedPointer);
		serviceRegistryEntryPointer.append("serviceDescription.dependency",
				arrayQuery);

		try {
			BasicDBObject arrayManipulatedPointer = new BasicDBObject(
					"serviceDescription.dependency", itemToBeDeletedPointer);

			WriteResult wr = null;
			wr = this.serviceRegistryEntryColl.update(
					serviceRegistryEntryPointer, new BasicDBObject("$pull",
							arrayManipulatedPointer));

			if (wr != null) {
				if (wr.getError() != null) {
					throw new DAOUpdateFailedFault(
							"Dependency was not deleted in service registry entry "
									+ serviceId + " : " + wr.getError());
				}
				if (wr.getN() == 0) {
					throw new DAONotFoundFault("Dependency " + dependencyId
							+ " or service registry entry " + serviceId
							+ " was not found");
				}

				retString = Integer.toString(wr.getN());
			}

		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Failure deleting dependency "
					+ dependencyId + " in service registry entry " + serviceId
					+ " : " + e.getMessage(), e);
		}
		return retString;
	}

	@Override
	public String insertNewUserFeedback(String serviceId, UserFeedback uFeedback)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault {
		String newUserFeedbackId = "";
		DBObject dbObj = null;
		try {
			uFeedback.setCreatedOnDate(DAOUtils.getCurrentDateAsUnixEpoch());
			
			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject itemToBeInserted = new BasicDBObject(
					"userFeedbackId", uFeedback.getUserFeedbackId());
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					itemToBeInserted);
			serviceRegistryEntryPointer.append(
					"serviceDescription.userFeedback", arrayQuery);

			// Find out if service registry entry with technical service
			// description exists already
			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			// If it does - do not insert user feedback description anymore
			if (dbObj != null) {
				throw new DAONotSavedFault("Service registry entry "
						+ serviceId + " already has a user feedback with id "
						+ uFeedback.getUserFeedbackId());
			}
			// If it doesn't - insert technical service description
			BasicDBObject arrayInsertion = new BasicDBObject(
					"serviceDescription.userFeedback",
					DAOUtils.convertWSObjToDBObjViaJSON(uFeedback,
							UserFeedback.class));
			WriteResult wr = this.serviceRegistryEntryColl.update(
					new BasicDBObject("_id", new ObjectId(serviceId)),
					new BasicDBObject("$addToSet", arrayInsertion));

			if (wr != null) {
				if (wr.getError() != null) {
					throw new DAOUpdateFailedFault(
							"User feedback was not inserted in the service registry entry "
									+ serviceId + " : " + wr.getError());
				}
				if (wr.getN() == 0) {
					throw new DAONotFoundFault("Service registry entry id "
							+ serviceId + " was not found");
				}
			}
			new MongoDBServiceRegistryDAO().updateUserRating(serviceId);
			newUserFeedbackId = uFeedback.getUserFeedbackId();
		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE, "Error inserting new user feedback.", e);
			throw new DAOGeneralSystemFault(
					"Failure inserting new user feedback "
							+ uFeedback.getUserFeedbackId()
							+ " in service registry entry " + serviceId, e);
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		}
		return newUserFeedbackId;
	}

	@Override
	public UserFeedback findUserFeedback(String serviceId, String uFBId)
			throws DAOGeneralSystemFault, DAONotFoundFault {
		UserFeedback retTechDescr = null;
		DBObject dbObj = null;
		try {
			if ((serviceId != null) && (uFBId != null)) {
				BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
						"_id", new ObjectId(serviceId));
				BasicDBObject arrayPointer = new BasicDBObject(
						"userFeedbackId", uFBId);
				BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
						arrayPointer);
				serviceRegistryEntryPointer.append(
						"serviceDescription.userFeedback", arrayQuery);

				// Find out if service registry entry with technical service
				// description exists already
				dbObj = this.serviceRegistryEntryColl
						.findOne(serviceRegistryEntryPointer);

				if (dbObj != null) {
					BasicDBList techDescList = (BasicDBList) ((BasicDBObject) dbObj
							.get("serviceDescription")).get("userFeedback");

					for (Object techDesc : techDescList) {
						if (((BasicDBObject) techDesc).get("userFeedbackId")
								.equals(uFBId))
						{
							// Matching technical description found
							retTechDescr = DAOUtils.convertDBObjToWSObjViaJSON(
									(DBObject) techDesc, UserFeedback.class);
							break;
						}
					}
				}
				if (dbObj == null) {
					throw new DAONotFoundFault("No user feedback " + uFBId
							+ " was found in service registry entry "
							+ serviceId);
				}
			}
		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE, "Error finding user feedback", e);
			throw new DAOGeneralSystemFault(
					"Failure while retrieving user feedback " + uFBId
							+ " in service registry entry " + serviceId, e);
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		}
		return retTechDescr;
	}

	private void updateModifiedOnDateInServiceDescription(String servRegEntryId) {
		if (servRegEntryId != null) {
			BasicDBObject updatedCurrentTime = new BasicDBObject(
					"serviceDescription.modifiedOnDate", Long.toString(System
							.currentTimeMillis()));
			BasicDBObject updateCommand = new BasicDBObject("$set",
					updatedCurrentTime);
			this.serviceRegistryEntryColl.update(new BasicDBObject("_id",
					new ObjectId(servRegEntryId)), updateCommand);
		}
	}

	public String deleteUserFeedback(String serviceId, String uFBId)
			throws DAOGeneralSystemFault, DAONotFoundFault, DAOUpdateFailedFault {
	
		String retString = "";
		BasicDBObject serviceRegistryEntryPointer = new BasicDBObject("_id",
				new ObjectId(serviceId));
		BasicDBObject itemToBeDeletedPointer = new BasicDBObject(
				"userFeedbackId", uFBId);
		BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
				itemToBeDeletedPointer);
		serviceRegistryEntryPointer.append("serviceDescription.userFeedback",
				arrayQuery);

		try {
			BasicDBObject arrayManipulatedPointer = new BasicDBObject(
					"serviceDescription.userFeedback", itemToBeDeletedPointer);

			WriteResult wr = null;
			wr = this.serviceRegistryEntryColl.update(
					serviceRegistryEntryPointer, new BasicDBObject("$pull",
							arrayManipulatedPointer));

			if (wr != null) {
				if (wr.getError() != null) {
					throw new DAOUpdateFailedFault(
							"Userfeedback was not deleted in service registry entry "
									+ serviceId + " : " + wr.getError());
				}
				if (wr.getN() == 0) {
					throw new DAONotFoundFault("UserFeedback " + uFBId
							+ " or service registry entry " + serviceId
							+ " was not found");
				}

				retString = Integer.toString(wr.getN());
			}

		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Failure deleting feedback "
					+ uFBId + " in service registry entry " + serviceId
					+ " : " + e.getMessage(), e);
		}
                
                return retString;
	}
        
    public String updateUserFeedback(String serviceId, String userFeedbackId, UserFeedback userFeedback)
            throws DAOGeneralSystemFault, DAOUpdateFailedFault, DAONotFoundFault {
        DBObject dbObj = null;
        String updatedUserFeedbackId = "";

        userFeedback.setUserFeedbackId(userFeedbackId);

        try {
            dbObj = this.serviceRegistryEntryColl.findOne(new ObjectId(serviceId));

            BasicDBObject serviceRegistryEntryPointer = new BasicDBObject("_id", new ObjectId(serviceId));
            BasicDBObject itemToBeUpdatedPointer = new BasicDBObject("userFeedbackId", userFeedbackId);
            BasicDBObject arrayQuery = new BasicDBObject("$elemMatch", itemToBeUpdatedPointer);
            serviceRegistryEntryPointer.append("serviceDescription.userFeedback", arrayQuery);

            dbObj = this.serviceRegistryEntryColl.findOne(serviceRegistryEntryPointer);

            if (dbObj == null) {
                throw new DAONotFoundFault("Service registry entry "
                        + serviceId + " with user-feedback "
                        + userFeedbackId + " to be updated was not found in DB");
            }

            BasicDBObject arrayUpdate = new BasicDBObject("serviceDescription.userFeedback.$",
                    (BasicDBObject) DAOUtils.convertWSObjToDBObjViaJSON(userFeedback, UserFeedback.class));
            WriteResult wr = null;
            BasicDBObject servRegEntryPointerWithArrayItem = serviceRegistryEntryPointer
                    .append("serviceDescription.userFeedback.userFeedbackId", userFeedbackId);

            wr = this.serviceRegistryEntryColl.update(servRegEntryPointerWithArrayItem, new BasicDBObject("$set",
                    arrayUpdate), true, false);

            if (wr != null && (wr.getError() != null || wr.getN() == 0)) {
                throw new DAOUpdateFailedFault("Service registry entry "
                        + serviceId + " with user-feedback "
                        + userFeedbackId + " update failed");
            }

            updateModifiedOnDateInServiceDescription(serviceId);
            updatedUserFeedbackId = userFeedback.getUserFeedbackId();

        } catch (IllegalArgumentException e) {
            throw new DAOGeneralSystemFault(
                    "Malformatted id/fieldname while using service registry entry "
                    + serviceId
                    + ". Look closer for the ids/fieldnames, you nincumpoop!",
                    e);
        } catch (MongoException e) {
            throw new DAOGeneralSystemFault("User-feedback "
                    + userFeedbackId
                    + " update failed in service registry entry " + serviceId,
                    e);
        }
        
        return updatedUserFeedbackId;
    }
}
