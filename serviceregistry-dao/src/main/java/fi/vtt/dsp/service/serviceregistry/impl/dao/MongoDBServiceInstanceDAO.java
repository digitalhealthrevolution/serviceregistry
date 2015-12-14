package fi.vtt.dsp.service.serviceregistry.impl.dao;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBList;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;

import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.common.instance.AvailabilityRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.BindingRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.SpringApplicationContext;
import fi.vtt.dsp.serviceframework.common.Binding;

public class MongoDBServiceInstanceDAO implements ServiceInstanceDAO {

	private static final Logger LOGGER = Logger
			.getLogger(MongoDBServiceInstanceDAO.class.getName());

	private DBCollection serviceRegistryEntryColl = null;
	private MongoDBConnection mongoDBConn;
	// To access user profiles in DAOUtils
	private MongoDBConnection UPmongoDBConn;

	public MongoDBServiceInstanceDAO() throws DAOGeneralSystemFault {
		if (mongoDBConn == null) {
			mongoDBConn = (MongoDBConnection) SpringApplicationContext
					.getBean("mongoDB");
			UPmongoDBConn = (MongoDBConnection) SpringApplicationContext
					.getBean("UPmongoDB");
		}
		// TODO: Is this reasonable, fetch new collection everytime DAO method
		// is invoked?
		serviceRegistryEntryColl = mongoDBConn.getDBCollection();
	}

	@Override
	public ServiceInstance findServiceInstance(String serviceId,
			String instanceId) throws DAOGeneralSystemFault, DAONotFoundFault {
		ServiceInstance retServInst = null;
		DBObject dbObj = null;
		try {
			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject arrayPointer = new BasicDBObject("serviceInstanceId",
					instanceId);
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					arrayPointer);
			serviceRegistryEntryPointer.append("serviceInstance", arrayQuery);

			// Find out if service registry entry with service instance exists
			LOGGER.log(
					Level.FINE,
					"MongoDBServiceRegistryDAO.findServiceInstance with query {0}",
					serviceRegistryEntryPointer.toString());
			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			if (dbObj != null) {
				BasicDBList serviceInstanceList = (BasicDBList) ((BasicDBObject) dbObj)
						.get("serviceInstance");
				for (Object servInst : serviceInstanceList) {
					if (((BasicDBObject) servInst).get("serviceInstanceId")
							.equals(instanceId)) {
						// Look for matching service registry entry with
						// matching service instance
						retServInst = DAOUtils.convertDBObjToWSObjViaJSON(
								(DBObject) servInst, ServiceInstance.class);
						break;
					}
				}
			}
			if (dbObj == null) {
				throw new DAONotFoundFault("No service instance " + instanceId
						+ " was found in service registry entry " + serviceId);
			}
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE, "Error finding service instance "
					+ instanceId + " in service registry entry " + serviceId, e);
			throw new DAOGeneralSystemFault(
					"Failure while retrieving service instance " + instanceId
							+ " in service registry entry " + serviceId, e);
		}
		return retServInst;
	}

	@Override
	public String insertNewServiceInstance(String serviceId,
			ServiceInstance servInst) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault {
		DBObject dbObj = null;
		String insertedServiceInstanceId = "";

		// Check if the *ByUserId userid is valid, throws exception
		// DAONotSavedFault
		DAOUtils.checkUserIdValidityInServiceInstance(UPmongoDBConn, servInst);

		try {

			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject itemIdToBeInserted = new BasicDBObject(
					"serviceInstanceId", servInst.getServiceInstanceId());
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					itemIdToBeInserted);
			serviceRegistryEntryPointer.append("serviceInstance", arrayQuery);

			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			if (dbObj != null) {
				throw new DAOUpdateFailedFault("Service registry entry "
						+ serviceId + " already has service instance with "
						+ servInst.getServiceInstanceId());
			}

			// Setting current time as creation and modification dates
			long timestamp = DAOUtils.getCurrentDateAsUnixEpoch();
			servInst.setCreatedOnDate(timestamp);
			servInst.setModifiedOnDate(timestamp);
			BasicDBObject arrayInsertion = new BasicDBObject("serviceInstance",
					DAOUtils.convertWSObjToDBObjViaJSON(servInst,
							ServiceInstance.class));
			WriteResult wr = this.serviceRegistryEntryColl.update(
					new BasicDBObject("_id", new ObjectId(serviceId)),
					new BasicDBObject("$addToSet", arrayInsertion));

			if (wr != null) {
				if (wr.getError() != null) {
					throw new DAOUpdateFailedFault(
							"Service instance was not inserted in service registry entry "
									+ serviceId + " : " + wr.getError());
				}
				if (wr.getN() == 0) {
					throw new DAONotFoundFault("Service instance "
							+ servInst.getServiceInstanceId()
							+ " or service registry entry " + serviceId
							+ " was not found");
				}

			}

			insertedServiceInstanceId = servInst.getServiceInstanceId();
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault(
					"Failure inserting service instance "
							+ servInst.getServiceInstanceId()
							+ " in service registry entry " + serviceId + " : "
							+ e.getMessage(), e);
		}
		return insertedServiceInstanceId;
	}

	@Override
	public String updateServiceInstance(String serviceId, String servInstId,
			ServiceInstance updatedServiceInstance)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault, DAONotSavedFault {
		DBObject dbObj = null;
		String updatedServiceInstanceId = "";

		// FIXME: Maybe this one should be checked if same in the beginning
		updatedServiceInstance.setServiceInstanceId(servInstId);
		// Check if the *ByUserId userid is valid, throws exception
		// DAONotSavedFault
		DAOUtils.checkUserIdValidityInServiceInstance(UPmongoDBConn,
				updatedServiceInstance);
		try {
			dbObj = this.serviceRegistryEntryColl.findOne(new ObjectId(
					serviceId));

			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject itemToBeUpdatedPointer = new BasicDBObject(
					"serviceInstanceId", servInstId);
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					itemToBeUpdatedPointer);
			serviceRegistryEntryPointer.append("serviceInstance", arrayQuery);

			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			if (dbObj == null) {
				throw new DAONotFoundFault("Service registry entry "
						+ serviceId + " with dependency " + servInstId
						+ " to be manipulated was not found in DB");
			}

			// Preserve existing created on date
			updatedServiceInstance.setCreatedOnDate(this.findServiceInstance(
					serviceId, servInstId).getCreatedOnDate());
			// Set modified on date
			updatedServiceInstance.setModifiedOnDate(DAOUtils
					.getCurrentDateAsUnixEpoch());

			BasicDBObject updatedServiceInstanceDB = (BasicDBObject) DAOUtils
					.convertWSObjToDBObjViaJSON(updatedServiceInstance,
							ServiceInstance.class);

			BasicDBObject arrayUpdate = new BasicDBObject("serviceInstance.$",
					updatedServiceInstanceDB);

			WriteResult wr = null;

			BasicDBObject servRegEntryPointerWithArrayItem = serviceRegistryEntryPointer
					.append("serviceInstance.serviceInstanceId", servInstId);

			wr = this.serviceRegistryEntryColl.update(
					servRegEntryPointerWithArrayItem, new BasicDBObject("$set",
							arrayUpdate), true, false);

			if (wr != null && (wr.getError() != null || wr.getN() == 0)) {
				throw new DAOUpdateFailedFault("Service registry entry "
						+ serviceId + " with service instance " + servInstId
						+ " update failed");
			}
			updatedServiceInstanceId = updatedServiceInstance
					.getServiceInstanceId();
		} catch (IllegalArgumentException e) {
			LOGGER.log(
					Level.SEVERE,
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			throw new DAOGeneralSystemFault("Service dependency " + servInstId
					+ " update failed in service registry entry " + serviceId,
					e);
		}
		return updatedServiceInstanceId;

	}

	@Override
	public String deleteServiceInstance(String serviceId, String servInstId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault {
		String retString = "";
		BasicDBObject serviceRegistryEntryPointer = new BasicDBObject("_id",
				new ObjectId(serviceId));
		BasicDBObject itemToBeDeletedPointer = new BasicDBObject(
				"serviceInstanceId", servInstId);
		BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
				itemToBeDeletedPointer);
		serviceRegistryEntryPointer.append("serviceInstance", arrayQuery);

		try {
			BasicDBObject arrayManipulatedPointer = new BasicDBObject(
					"serviceInstance", itemToBeDeletedPointer);
			WriteResult wr = null;
			wr = this.serviceRegistryEntryColl.update(
					serviceRegistryEntryPointer, new BasicDBObject("$pull",
							arrayManipulatedPointer));

			if (wr != null) {
				if (wr.getError() != null) {
					throw new DAOUpdateFailedFault(
							"Service instance was not deleted in service registry entry "
									+ serviceId + " : " + wr.getError());
				}
				if (wr.getN() == 0) {
					throw new DAONotFoundFault("Service instance " + servInstId
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
					+ servInstId + " in service registry entry " + serviceId
					+ " : " + e.getMessage(), e);
		}
		return retString;
	}

	@Override
	public AvailabilityRequestEndPoint findAvailabilityRequestEndPoint(
			String serviceId, String instanceId) throws DAOGeneralSystemFault,
			DAONotFoundFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		return matchingServiceInstance.getAvailabilityRequestEndPoint();
	}

	@Override
	public String updateAvailabilityRequestEndPoint(String serviceId,
			String instanceId, AvailabilityRequestEndPoint avaEP)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault, DAONotSavedFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		matchingServiceInstance.setAvailabilityRequestEndPoint(avaEP);
		return this.updateServiceInstance(serviceId, instanceId,
				matchingServiceInstance);
	}

	@Override
	public String deleteAvailabilityRequestEndPoint(String serviceId,
			String instanceId) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		matchingServiceInstance.setAvailabilityRequestEndPoint(null);
		String updatedSIID = this.updateServiceInstance(serviceId, instanceId,
				matchingServiceInstance);
		if (updatedSIID.isEmpty()) {
			return "0";
		} else {
			return "1";
		}
	}

	@Override
	public BindingRequestEndPoint findBindingRequestEndPoint(String serviceId,
			String instanceId) throws DAOGeneralSystemFault, DAONotFoundFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		return matchingServiceInstance.getBindingRequestEndPoint();
	}

	@Override
	public String updateBindingRequestEndPoint(String serviceId,
			String instanceId, BindingRequestEndPoint bindEP)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault, DAONotSavedFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		matchingServiceInstance.setBindingRequestEndPoint(bindEP);
		return this.updateServiceInstance(serviceId, instanceId,
				matchingServiceInstance);
	}

	@Override
	public String deleteBindingRequestEndPoint(String serviceId,
			String instanceId) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		matchingServiceInstance.setBindingRequestEndPoint(null);
		String updatedSIId = this.updateServiceInstance(serviceId, instanceId,
				matchingServiceInstance);
		if (updatedSIId.isEmpty()) {
			return "0";
		} else {
			return "1";
		}
	}

	@Override
	public ServiceAccessEndPoint findServiceAccessEndPoint(String serviceId,
			String instanceId) throws DAOGeneralSystemFault, DAONotFoundFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		return matchingServiceInstance.getServiceAccessEndPoint();
	}

	@Override
	public String updateServiceAccessEndPoint(String serviceId,
			String instanceId, ServiceAccessEndPoint saEP)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault, DAONotSavedFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		matchingServiceInstance.setServiceAccessEndPoint(saEP);
		return this.updateServiceInstance(serviceId, instanceId,
				matchingServiceInstance);
	}

	@Override
	public String deleteServiceAccessEndPoint(String serviceId,
			String instanceId) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault {
		ServiceInstance matchingServiceInstance = this.findServiceInstance(
				serviceId, instanceId);
		matchingServiceInstance.setServiceAccessEndPoint(null);
		return this.updateServiceInstance(serviceId, instanceId,
				matchingServiceInstance);
	}

	@Override
	public Binding findBinding(String serviceId, String instanceId,
			String bindingId) throws DAOGeneralSystemFault, DAONotFoundFault {
		Binding retBinding = null;
		DBObject dbObj = null;
		try {
			BasicDBObject serviceRegistryEntryPointer = new BasicDBObject(
					"_id", new ObjectId(serviceId));
			BasicDBObject arrayPointer = new BasicDBObject("serviceInstanceId",
					instanceId);
			BasicDBObject arrayQuery = new BasicDBObject("$elemMatch",
					arrayPointer);
			serviceRegistryEntryPointer.append("serviceInstance", arrayQuery);

			dbObj = this.serviceRegistryEntryColl
					.findOne(serviceRegistryEntryPointer);

			if (dbObj != null) {
				BasicDBList serviceInstanceList = (BasicDBList) ((BasicDBObject) dbObj)
						.get("serviceInstance");

				for (Object servInst : serviceInstanceList) {
					if (((BasicDBObject) servInst).get("serviceInstanceId")
							.equals(instanceId)) {
						BasicDBObject servInstDB = (BasicDBObject) servInst;
						BasicDBObject saEP = (BasicDBObject) servInstDB
								.get("serviceAccessEndPoint");
						BasicDBList bindingList = (BasicDBList) (saEP
								.get("binding"));
						for (Object bindObj : bindingList) {
							if (((BasicDBObject) bindObj).get("bindingId")
									.equals(bindingId)) {
								retBinding = DAOUtils
										.convertDBObjToWSObjViaJSON(
												(DBObject) bindObj,
												Binding.class);
								break;
							}
						}

					}
				}
			}
			if (dbObj == null || retBinding == null) {
				throw new DAONotFoundFault("No binding " + bindingId
						+ " was found in service registry entry " + serviceId
						+ " and service instance " + instanceId);
			}
		} catch (IllegalArgumentException e) {
			throw new DAOGeneralSystemFault(
					"Malformatted id/fieldname while using service registry entry "
							+ serviceId
							+ ". Look closer for the ids/fieldnames, you nincumpoop!",
					e);
		} catch (MongoException e) {
			LOGGER.log(Level.SEVERE, "Failure while retrieving binding "
					+ bindingId + " in service instance " + instanceId
					+ " and service registry entry " + serviceId, e);
			throw new DAOGeneralSystemFault("Failure while retrieving binding "
					+ bindingId + " in service instance " + instanceId
					+ " and service registry entry " + serviceId, e);
		}
		return retBinding;
	}

	@Override
	public String insertNewBinding(String serviceId, String instanceId,
			Binding newBinding) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault {

		// Check if the *ByUserId userid is valid, throws exception
		// DAONotSavedFault
		DAOUtils.checkUserIdValidityInBinding(UPmongoDBConn, newBinding);

		String newBindingId = "";
		ServiceInstance servInst = this.findServiceInstance(serviceId,
				instanceId);
		ServiceAccessEndPoint servAEP = servInst.getServiceAccessEndPoint();
		
		if (servAEP != null) {
			List<Binding> bindingList = servAEP.getBinding();
			if (newBinding.getBindingId() != null) {
				for (Binding binding : bindingList) {
					if (binding.getBindingId()
							.equals(newBinding.getBindingId())) {
						throw new DAOUpdateFailedFault(
								"Service registry entry " + serviceId
										+ " with service instance "
										+ instanceId + " already has binding "
										+ newBinding.getBindingId());
					}
				}
			}
			if (newBinding.getBindingId() == null) {
				newBinding
						.setBindingId(Integer.toString(newBinding.hashCode()));
			} else if (newBinding.getBindingId().isEmpty()) {
				newBinding
						.setBindingId(Integer.toString(newBinding.hashCode()));
			}
			// Setting current time as request and modification dates
			long timestamp = DAOUtils.getCurrentDateAsUnixEpoch();	
			newBinding.setRequestedOnDate(timestamp);
			newBinding.setModifiedOnDate(timestamp);
			
			newBindingId = newBinding.getBindingId();
			bindingList.add(newBinding);
			this.updateServiceAccessEndPoint(serviceId, instanceId, servAEP);
		} else {
			throw new DAONotFoundFault("Service instance " + instanceId
					+ " or service registry entry " + serviceId
					+ " was not found");
		}
		return newBindingId;
	}

	@Override
	public String updateBinding(String serviceId, String instanceId,
			String bindingId, Binding updatedBinding)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault, DAONotSavedFault {

		// Check if the *ByUserId userid is valid, throws exception
		// DAONotSavedFault
		DAOUtils.checkUserIdValidityInBinding(UPmongoDBConn, updatedBinding);

		String updatedBindingId = "";
		ServiceInstance servInst = this.findServiceInstance(serviceId,
				instanceId);
		ServiceAccessEndPoint servAEP = servInst.getServiceAccessEndPoint();
		
		if (updatedBinding.getBindingId() == null) {
			updatedBinding.setBindingId(bindingId);
		} else if (updatedBinding.getBindingId().isEmpty()) {
			updatedBinding.setBindingId(bindingId);
		}

		// Setting current time as modification date
		updatedBinding.setModifiedOnDate(DAOUtils.getCurrentDateAsUnixEpoch());
			
		if (servAEP != null) {
			List<Binding> bindingList = servAEP.getBinding();
			int i = 0;
			boolean matchFound = false;
			for (Binding binding : bindingList) {
				if (binding.getBindingId().equals(bindingId)) {
					bindingList.set(i, updatedBinding);
					matchFound = true;
					break;
				}
				i++;
			}
			if (matchFound) {
				updatedBindingId = bindingId;
				this.updateServiceAccessEndPoint(serviceId, instanceId, servAEP);
			}
			if (!matchFound) {
				throw new DAONotFoundFault("Service registry entry "
						+ serviceId + " with service instance " + instanceId
						+ " and binding " + bindingId + " was not found");
			}
		} else {
			throw new DAONotFoundFault("Service instance " + instanceId
					+ " or service registry entry " + serviceId
					+ " was not found");
		}
		return updatedBindingId;
	}

	@Override
	public String deleteBinding(String serviceId, String instanceId,
			String bindingId) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotFoundFault, DAONotSavedFault {
		String deletionStatus = "0";
		ServiceInstance servInst = this.findServiceInstance(serviceId,
				instanceId);
		ServiceAccessEndPoint servAEP = servInst.getServiceAccessEndPoint();

		if (servAEP != null) {
			List<Binding> bindingList = servAEP.getBinding();
			int i = 0;
			boolean matchFound = false;
			for (Binding binding : bindingList) {
				if (binding.getBindingId().equals(bindingId)) {
					bindingList.remove(i);
					matchFound = true;
					break;
				}
				i++;
			}
			if (matchFound) {
				this.updateServiceAccessEndPoint(serviceId, instanceId, servAEP);
				deletionStatus = "1";
			}
			if (!matchFound) {
				throw new DAONotFoundFault("Service registry entry "
						+ serviceId + " with service instance " + instanceId
						+ " and binding " + bindingId + " was not found");
			}
		} else {
			throw new DAONotFoundFault("Service instance " + instanceId
					+ " or service registry entry " + serviceId
					+ " was not found");
		}
		return deletionStatus;
	}

	@Override
	public Availability findAvailability(String serviceId, String instanceId)
			throws DAOGeneralSystemFault, DAONotFoundFault {
		ServiceAccessEndPoint saEP = this.findServiceAccessEndPoint(serviceId,
				instanceId);
		return saEP.getAvailability();
	}

	@Override
	public String updateAvailability(String serviceId, String instanceId,
			Availability servAvailability) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault, DAONotSavedFault {
		ServiceAccessEndPoint saEP = this.findServiceAccessEndPoint(serviceId,
				instanceId);

		if (servAvailability.getSelfReportedAvailability() != null) {
			saEP.getAvailability().setSelfReportedAvailability(
					servAvailability.getSelfReportedAvailability());
		}

		if (servAvailability.getInspectedAvailability() != null) {
			saEP.getAvailability().setInspectedAvailability(
					servAvailability.getInspectedAvailability());
		}

		return this.updateServiceAccessEndPoint(serviceId, instanceId, saEP);
	}

	@Override
	public String deleteAvailability(String serviceId, String instanceId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault, DAONotSavedFault {
		ServiceAccessEndPoint saEP = this.findServiceAccessEndPoint(serviceId,
				instanceId);
		// HOX: Inspected availability is not set in the web service interface
		saEP.getAvailability().setSelfReportedAvailability(null);
		String updatedId = this.updateServiceAccessEndPoint(serviceId,
				instanceId, saEP);
		if (updatedId.isEmpty()) {
			return "0";
		} else {
			return "1";
		}
	}

}
