package fi.vtt.dsp.service.serviceregistry.impl.dao;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TimeZone;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.datatype.DatatypeFactory;

import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;

import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.data.ComponentProperty;
import fi.vtt.dsp.service.serviceregistry.common.data.ComponentSpecification;
import fi.vtt.dsp.service.serviceregistry.common.data.DataStructureSpecification;
import fi.vtt.dsp.service.serviceregistry.common.data.Dataset;
import fi.vtt.dsp.service.serviceregistry.common.data.Distribution;
import fi.vtt.dsp.service.serviceregistry.common.data.ServiceDataDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.AvailabilityDeclaration;
import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.description.HumanReadableDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.ServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.common.description.UserRating;
import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.common.instance.AvailabilityRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.BindingRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.serviceframework.common.ServiceAvailability;
import org.apache.cxf.common.util.StringUtils;

public final class DAOUtils {

	private static final Logger LOGGER = Logger.getLogger(DAOUtils.class.getName());
    public static ObjectMapper mapper = new ObjectMapper();
        
	private DAOUtils() {

		// HOX: This makes conversion to skip unknown fields usually caused by
		// DB schema changes for development time
		mapper.configure(
				org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
				false);		
	}

	public static long getCurrentDateAsUnixEpoch() {
		return System.currentTimeMillis();
	}

	public static XMLGregorianCalendar getCurrentDateAsXMLGregorianCalendarUTC()
			throws DAOGeneralSystemFault {
		GregorianCalendar gCal = (GregorianCalendar) GregorianCalendar
				.getInstance(TimeZone.getTimeZone("UTC"));
		XMLGregorianCalendar cal = null;
		try {
			cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
		} catch (DatatypeConfigurationException e) {
			throw new DAOGeneralSystemFault(
					"Converting current date to XML gregorian calendar failed",
					e);
		}
		return cal;
	}

	public static XMLGregorianCalendar getTimeInMillisAsXMLGregorianCalendarUTC(
			long timeMillis) throws DAOGeneralSystemFault {
		GregorianCalendar gCal = (GregorianCalendar) GregorianCalendar
				.getInstance(TimeZone.getTimeZone("UTC"));
		gCal.setTimeInMillis(timeMillis);
		XMLGregorianCalendar cal = null;
		try {
			cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gCal);
		} catch (DatatypeConfigurationException e) {
			throw new DAOGeneralSystemFault(
					"Converting current date to XML gregorian calendar failed",
					e);
		}
		return cal;
	}

	// TODO: Figure out how to instantiate the instance variables in XSD to Java
	// generation plugin for constructors or getter methods
	public static Object fixMissingDefaultValues(Object obj) {
		if (obj instanceof ServiceRegistryEntry) {
			ServiceRegistryEntry regEntry = (ServiceRegistryEntry) obj;
			if (regEntry.getServiceDescription() == null) {
				regEntry.setServiceDescription((ServiceDescription) fixMissingDefaultValues(new ServiceDescription()));
			}
		}

		if (obj instanceof ServiceDescription) {
			ServiceDescription servDesc = (ServiceDescription) obj;
			if (servDesc.getAvailabilityDeclaration() == null) {
				servDesc.setAvailabilityDeclaration((AvailabilityDeclaration) fixMissingDefaultValues(new AvailabilityDeclaration()));
			} else {
				servDesc.setAvailabilityDeclaration((AvailabilityDeclaration) fixMissingDefaultValues(servDesc
						.getAvailabilityDeclaration()));
			}
			if (servDesc.getHumanReadableDescription() == null) {
				servDesc.setHumanReadableDescription(new HumanReadableDescription());
			}
			if (servDesc.getUserRating() == null) {
				servDesc.setUserRating(new UserRating());
			}
		}

		if (obj instanceof ServiceInstance) {
			ServiceInstance servInst = (ServiceInstance) obj;
			if (servInst.getAvailabilityRequestEndPoint() == null) {
				servInst.setAvailabilityRequestEndPoint(new AvailabilityRequestEndPoint());
			}
			if (servInst.getBindingRequestEndPoint() == null) {
				servInst.setBindingRequestEndPoint(new BindingRequestEndPoint());
			}
			if (servInst.getServiceAccessEndPoint() == null) {
				servInst.setServiceAccessEndPoint((ServiceAccessEndPoint) fixMissingDefaultValues(new ServiceAccessEndPoint()));
			} else {
				servInst.setServiceAccessEndPoint((ServiceAccessEndPoint) fixMissingDefaultValues(servInst
						.getServiceAccessEndPoint()));
			}

		}

		if (obj instanceof ServiceAccessEndPoint) {
			ServiceAccessEndPoint servAP = (ServiceAccessEndPoint) obj;
			if (servAP.getAvailability() == null) {
				servAP.setAvailability((Availability) fixMissingDefaultValues(new Availability()));
			}
		}

		if (obj instanceof AvailabilityDeclaration) {
			AvailabilityDeclaration avaDec = (AvailabilityDeclaration) obj;
			if (avaDec.getDeclaredAvailability() == null) {
				avaDec.setDeclaredAvailability(new ServiceAvailability());
			}
		}

		if (obj instanceof Availability) {
			Availability ava = (Availability) obj;
			if (ava.getInspectedAvailability() == null) {
				ava.setInspectedAvailability(new ServiceAvailability());
			}
			if (ava.getSelfReportedAvailability() == null) {
				ava.setSelfReportedAvailability(new ServiceAvailability());
			}
		}

		return obj;
	}

	@SuppressWarnings("unchecked")
	public static <T> T convertDBObjToWSObjViaJSON(DBObject dbObj, Class<T> t)
			throws DAOGeneralSystemFault {
		T retWSObject = null;
		try {
			// A minor hack to replace _id tag with serviceId :D
			if (dbObj.containsField("_id")) {
				ObjectId dbId = (ObjectId) dbObj.get("_id");
				dbObj.removeField("_id");
				if (dbObj.containsField("serviceid")) {
					dbObj.put("serviceid", dbId.toString());
				}
				if (dbObj.containsField("userId")) {
					dbObj.put("userId", dbId.toString());
				}

			}
			retWSObject = mapper.readValue(dbObj.toString(), t);
			retWSObject = (T) fixMissingDefaultValues(retWSObject);
		} catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cannot convert db-object to ws-object", e);
			throw new DAOGeneralSystemFault(
					"Conversion from DB object to WS object failed", e);
		}
		return retWSObject;
	}

	private static Object fixIds(Object obj) {
		if (obj instanceof ServiceDataDescription) {
			ServiceDataDescription castedObj = (ServiceDataDescription) obj;
			if (StringUtils.isEmpty(castedObj.getDataDescriptionId())) {
				castedObj.setDataDescriptionId(Integer.toString(obj.hashCode()));
			}
			for( Dataset ds : castedObj.getDataset() ) {
				fixIds(ds);
			}
		}
		if (obj instanceof Dataset) {
			Dataset castedObj = (Dataset) obj;
			if (StringUtils.isEmpty(castedObj.getDatasetId())) {
				castedObj.setDatasetId(Integer.toString(obj.hashCode()));
			}
			for( DataStructureSpecification spec : castedObj.getStructure() ) {
				fixIds(spec);
			}
			for( Distribution dist : castedObj.getDistribution() ) {
				fixIds(dist);
			}
		}
		if (obj instanceof DataStructureSpecification) {
			DataStructureSpecification castedObj = (DataStructureSpecification) obj;
			if (StringUtils.isEmpty(castedObj.getDataStructureDefinitionId())) {
				castedObj.setDataStructureDefinitionId(Integer.toString(obj.hashCode()));
			}
			for( ComponentSpecification cSpec : castedObj.getComponent() ) {
				fixIds(cSpec);
			}
		}
		if (obj instanceof ComponentSpecification) {
			ComponentSpecification castedObj = (ComponentSpecification) obj;
			if (StringUtils.isEmpty(castedObj.getComponentSpecificationId())) {
				castedObj.setComponentSpecificationId(Integer.toString(obj.hashCode()));
			}
			for( ComponentProperty cProp : castedObj.getComponentProperty() ) {
				fixIds(cProp);
			}
		}
		if (obj instanceof ComponentProperty) {
			ComponentProperty castedObj = (ComponentProperty) obj;
			if (StringUtils.isEmpty(castedObj.getComponentPropertyId())) {
				castedObj.setComponentPropertyId(Integer.toString(obj.hashCode()));
			}
		}
		if (obj instanceof Distribution) {
			Distribution castedObj = (Distribution) obj;
			if (StringUtils.isEmpty(castedObj.getDistributionId())) {
				castedObj.setDistributionId(Integer.toString(obj.hashCode()));
			}
		}
		if (obj instanceof TechnicalServiceDescription) {
			TechnicalServiceDescription castedObj = (TechnicalServiceDescription) obj;
			if (castedObj.getTechnicalDescriptionId() == null) {
				castedObj.setTechnicalDescriptionId(Integer.toString(obj
						.hashCode()));
			} else if (castedObj.getTechnicalDescriptionId().isEmpty()) {
				castedObj.setTechnicalDescriptionId(Integer.toString(obj
						.hashCode()));
			}
		}
		if (obj instanceof Dependency) {
			Dependency castedObj = (Dependency) obj;
			if (castedObj.getDependencyId() == null) {
				castedObj.setDependencyId(Integer.toString(obj.hashCode()));
			} else if (castedObj.getDependencyId().isEmpty()) {
				castedObj.setDependencyId(Integer.toString(obj.hashCode()));
			}
		}
		if (obj instanceof Binding) {
			Binding castedObj = (Binding) obj;
			if (castedObj.getBindingId() == null) {
				castedObj.setBindingId(Integer.toString(obj.hashCode()));
			} else if (castedObj.getBindingId().isEmpty()) {
				castedObj.setBindingId(Integer.toString(obj.hashCode()));
			}
		}
		if (obj instanceof ServiceInstance) {
			ServiceInstance castedObj = (ServiceInstance) obj;
			if (castedObj.getServiceInstanceId() == null) {
				castedObj
						.setServiceInstanceId(Integer.toString(obj.hashCode()));
			} else if (castedObj.getServiceInstanceId().isEmpty()) {
				castedObj
						.setServiceInstanceId(Integer.toString(obj.hashCode()));
			}
			ServiceAccessEndPoint saep = castedObj.getServiceAccessEndPoint();
			if (saep != null) {
				for (Binding bind : saep.getBinding()) {
					fixIds(bind);
				}
			}
		}
		if (obj instanceof ServiceRegistryEntry) {
			ServiceDescription desc = ((ServiceRegistryEntry) obj)
					.getServiceDescription();
			if (desc != null) {
				for (UserFeedback uFB : desc.getUserFeedback()) {
					fixIds(uFB);
				}

				for (TechnicalServiceDescription techDesc : desc.getTechnicalServiceDescription()) {
					fixIds(techDesc);
				}
				for (ServiceDataDescription dataDesc : desc.getServiceDataDescription()) {
					fixIds(dataDesc);
				}
				for (Dependency dep : desc.getDependency()) {
					fixIds(dep);
				}
				for (ServiceInstance servInst : ((ServiceRegistryEntry) obj)
						.getServiceInstance()) {
					fixIds(servInst);
				}
			}
		}
		if (obj instanceof UserFeedback) {
			UserFeedback castedObj = (UserFeedback) obj;
			if (castedObj.getUserFeedbackId() == null) {
				castedObj.setUserFeedbackId(Integer.toString(obj.hashCode()));
			} else if (castedObj.getUserFeedbackId().isEmpty()) {
				castedObj.setUserFeedbackId(Integer.toString(obj.hashCode()));
			}
		}
		return obj;
	}

	@SuppressWarnings("unchecked")
	public static <T> DBObject convertWSObjToDBObjViaJSON(Object obj, Class<T> t)
			throws DAOGeneralSystemFault {
		DBObject dbObject = null;
		try {
			fixIds(obj);
			dbObject = (DBObject) JSON
					.parse(mapper.writeValueAsString((T) obj));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.toString(), e);
			throw new DAOGeneralSystemFault(
					"Conversion from WS object to DB object failed", e);
		}
		return dbObject;
	}

	public static List<String> parseKeywordQueryParams(String queryParams) {
		List<String> retParamsList = new ArrayList();
		String[] params = queryParams.split("[\\{,\\}\\W]");

		for (String param : params) {
			if (param.length() != 0) {
				retParamsList.add(param);
			}
		}
		return retParamsList;
	}

	public static boolean checkUserIdValidityInBinding(
			MongoDBConnection uPmongoDBConn, Binding bI)
			throws DAONotFoundFault, DAONotSavedFault, DAOGeneralSystemFault,
			DAOUpdateFailedFault {
		boolean validity = false;

		MongoDBUserProfileDAO uProfDA = new MongoDBUserProfileDAO(uPmongoDBConn);

		List<String> validableUsersId = new ArrayList();

		if (bI != null) {
			if (bI.getAuthorizedByUserId() != null) {
				if (!validableUsersId.contains(bI.getAuthorizedByUserId())) {
					validableUsersId.add(bI.getAuthorizedByUserId());
				}
			}
			if (bI.getRequestedByUserId() != null) {
				if (!validableUsersId.contains(bI.getRequestedByUserId())) {
					validableUsersId.add(bI.getRequestedByUserId());
				}
			}			
		}

		// Check DB if the user Id's are valid, i.e. exists
		for (String userId : validableUsersId) {
			uProfDA.findUserProfileByUserId(userId);
			validity = true;
		}
		return validity;
	}

	public static boolean checkUserIdValidityInServiceInstance(
			MongoDBConnection uPmongoDBConn, ServiceInstance sI)
			throws DAONotFoundFault, DAONotSavedFault, DAOGeneralSystemFault,
			DAOUpdateFailedFault {
		boolean validity = false;

		MongoDBUserProfileDAO uProfDA = new MongoDBUserProfileDAO(uPmongoDBConn);
		List<String> validableUsersId = new ArrayList();

		if (sI != null) {
			if (sI.getCreatedByUserId() == null) {
				throw new DAONotSavedFault(
						"CreatedByUserId parameter in the service instance id "
								+ sI.getServiceInstanceId()
								+ " cannot be empty");
			} else {
				if (!validableUsersId.contains(sI.getCreatedByUserId())) {
					validableUsersId.add(sI.getCreatedByUserId());
				}
				if (sI.getModifiedByUserId() != null) {
					if (!validableUsersId.contains(sI.getModifiedByUserId())) {
						validableUsersId.add(sI.getModifiedByUserId());
					}
				}
				if (sI.getServiceAccessEndPoint() != null) {
					for (Binding bI : sI.getServiceAccessEndPoint().getBinding()) {
						if (!StringUtils.isEmpty(bI.getAuthorizedByUserId())) {
							if (!validableUsersId.contains(bI
									.getAuthorizedByUserId())) {
								validableUsersId
										.add(bI.getAuthorizedByUserId());
							}
							if (bI.getRequestedByUserId() != null) {
								if (!validableUsersId.contains(bI
										.getRequestedByUserId())) {
									validableUsersId.add(bI
											.getRequestedByUserId());
								}
							}
						}
					}
				}
			}
		}

		// Check DB if the user Id's are valid, i.e. exists
		for (String userId : validableUsersId) {
			uProfDA.findUserProfileByUserId(userId);
			validity = true;
		}
		return validity;
	}

	public static boolean checkUserIdValidityInServiceRegistryEntry(
			MongoDBConnection uPmongoDBConn, ServiceRegistryEntry sRegEntr)
			throws DAONotFoundFault, DAONotSavedFault, DAOGeneralSystemFault,
			DAOUpdateFailedFault {
		boolean validity = false;

		MongoDBUserProfileDAO uProfDA = new MongoDBUserProfileDAO(uPmongoDBConn);
		List<String> validableUsersId = new ArrayList();

		if (sRegEntr.getServiceDescription() != null) {
			if (sRegEntr.getServiceDescription().getCreatedByUserId() == null) {
				throw new DAONotSavedFault(
						"CreatedByUserId parameter in the service registry entry cannot be empty");
			} else {
				if (!validableUsersId.contains(sRegEntr.getServiceDescription()
						.getCreatedByUserId())) {
					validableUsersId.add(sRegEntr.getServiceDescription()
							.getCreatedByUserId());
				}
				if (sRegEntr.getServiceDescription().getModifiedByUserId() != null) {
					if (!validableUsersId.contains(sRegEntr
							.getServiceDescription().getModifiedByUserId())) {
						validableUsersId.add(sRegEntr.getServiceDescription()
								.getModifiedByUserId());
					}
				}
			}
		}

		if (sRegEntr.getServiceInstance().size() > 0) {
			for (ServiceInstance sI : sRegEntr.getServiceInstance()) {
				if (sI.getCreatedByUserId() == null) {
					throw new DAONotSavedFault(
							"CreatedByUserId parameter in the service instance id "
									+ sI.getServiceInstanceId()
									+ " cannot be empty");
				} else {
					if (!validableUsersId.contains(sI.getCreatedByUserId())) {
						validableUsersId.add(sI.getCreatedByUserId());
					}
					if (sI.getModifiedByUserId() != null) {
						if (!validableUsersId
								.contains(sI.getModifiedByUserId())) {
							validableUsersId.add(sI.getModifiedByUserId());
						}
					}
					if (sI.getServiceAccessEndPoint() != null) {
						for (Binding bI : sI.getServiceAccessEndPoint()
								.getBinding()) {
							if (!StringUtils.isEmpty(bI.getAuthorizedByUserId())) {
								if (!validableUsersId.contains(bI
										.getAuthorizedByUserId())) {
									validableUsersId.add(bI
											.getAuthorizedByUserId());
								}
								if (bI.getRequestedByUserId() != null) {
									if (!validableUsersId.contains(bI
											.getRequestedByUserId())) {
										validableUsersId.add(bI
												.getRequestedByUserId());
									}
								}
							}
						}
					}
				}
			}
		}

		// Check DB if the user Id's are valid, i.e. exists
		for (String userId : validableUsersId) {
			uProfDA.findUserProfileByUserId(userId);
			validity = true;
		}
		return validity;
	}

}
