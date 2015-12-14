package fi.vtt.dsp.service.serviceregistry.impl.dao;

import fi.vtt.dsp.service.serviceregistry.TestData;
import fi.vtt.dsp.service.serviceregistry.TestDb;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.serviceframework.common.Binding;

public class TestDataSetter {

	private MongoDBConnection dbRegEntryConn;
	private MongoDBConnection dbUPConn;

	private void setDBRegEntryConnection() {
		dbRegEntryConn = TestDb.getDbConnection("serviceregistry");
	}

	private void setDBUPConnection() {
		dbUPConn = TestDb.getDbConnection("serviceregistry.userprofiles");
	}

	public void cleanRegistryEntries() throws DAOGeneralSystemFault {
		if (dbRegEntryConn == null) {
			setDBRegEntryConnection();
		}
		TestDb.cleanEntries(dbRegEntryConn);
	}

	public void cleanUserProfiles() throws DAOGeneralSystemFault {
		if (dbUPConn == null) {
			setDBUPConnection();
		}
		TestDb.cleanEntries(dbUPConn);
	}

	public String setValidUserProfile1() throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotSavedFault, DAONotFoundFault {
		if (dbUPConn == null) {
			setDBUPConnection();
		}
		MongoDBUserProfileDAO uPDAO = new MongoDBUserProfileDAO(dbUPConn);
		return uPDAO.create(TestData.getUserProfile1());

	}

	public String setValidUserProfile2() throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotSavedFault, DAONotFoundFault {
		// MongoDBUserProfilesDAO should be modified to allow direct setting of
		// mongodbconnection
		if (dbUPConn == null) {
			setDBUPConnection();
		}
		MongoDBUserProfileDAO uPDAO = new MongoDBUserProfileDAO(dbUPConn);
		String uID = uPDAO.create(TestData.getUserProfile2());
		return uID;
	}

	public String setValidUserProfile3() throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotSavedFault, DAONotFoundFault {
		// MongoDBUserProfilesDAO should be modified to allow direct setting of
		// mongodbconnection
		if (dbUPConn == null) {
			setDBUPConnection();
		}
		MongoDBUserProfileDAO uPDAO = new MongoDBUserProfileDAO(dbUPConn);
		String uID = uPDAO.create(TestData.getUserProfile3());
		return uID;
	}

	public ServiceRegistryEntry setValidRegistryEntries()
			throws DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault,
			DAOUpdateFailedFault {
		if (dbRegEntryConn == null) {
			setDBRegEntryConnection();
		}

		// Get a valid user id
		String validUserId1 = this.setValidUserProfile3();
		String validUserId2 = this.setValidUserProfile2();

		ServiceRegistryEntry dSPTestService = TestData
				.getEntryForDSPTestService();
		dSPTestService.getServiceDescription().setCreatedByUserId(validUserId1);
		dSPTestService.getServiceDescription()
				.setModifiedByUserId(validUserId2);

		MongoDBServiceRegistryDAO sRegDAO = new MongoDBServiceRegistryDAO(
				dbRegEntryConn, dbUPConn);

		for (ServiceInstance sI : dSPTestService.getServiceInstance()) {
			sI.setCreatedByUserId(validUserId1);
			if (sI.getServiceInstanceId().equals("13565799")) {
				sI.setCreatedByUserId(validUserId1);
				for (Binding bI : sI.getServiceAccessEndPoint().getBinding()) {
					bI.setAuthorizedByUserId(validUserId1);
					bI.setBoundByServiceInstanceId("13565789");
				}
			}
			if (sI.getServiceInstanceId().equals("13565789")) {
				sI.setCreatedByUserId(validUserId2);
			}
		}

		String insertedServiceId = sRegDAO
				.insertNewServiceRegistryEntry(dSPTestService);

		for (ServiceInstance sI : dSPTestService.getServiceInstance()) {
			if (sI.getServiceInstanceId().equals("13565799")) {
				for (Binding bI : sI.getServiceAccessEndPoint().getBinding()) {
					bI.setBoundByServiceId(insertedServiceId);
				}
			}
		}

		sRegDAO.updateServiceRegistryEntry(insertedServiceId, dSPTestService);
		return dSPTestService;
	}
}
