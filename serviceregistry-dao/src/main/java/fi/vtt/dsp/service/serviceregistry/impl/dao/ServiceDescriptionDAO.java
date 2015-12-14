package fi.vtt.dsp.service.serviceregistry.impl.dao;

import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;

public interface ServiceDescriptionDAO {

	TechnicalServiceDescription findTechnicalServiceDescription(
			String serviceId, String techServDescId)
			throws DAOGeneralSystemFault, DAONotFoundFault;

	String insertNewTechnicalServiceDescription(String serviceId,
			TechnicalServiceDescription techServDesc)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault;

	String updateTechnicalServiceDescription(String serviceId,
			String techServDescId, TechnicalServiceDescription techServDesc)
			throws DAOGeneralSystemFault, DAOUpdateFailedFault,
			DAONotFoundFault;

	String deleteTechnicalServiceDescription(String serviceId,
			String techServDescId) throws DAOUpdateFailedFault,
			DAOGeneralSystemFault, DAONotFoundFault;

	Dependency findDependency(String serviceId, String dependencyId)
			throws DAOGeneralSystemFault, DAONotFoundFault;

	String insertNewDependency(String serviceId, Dependency servDep)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault;

	String updateDependency(String serviceId, String servDepId,
			Dependency servDep) throws DAOGeneralSystemFault,
			DAOUpdateFailedFault, DAONotFoundFault;

	String deleteDependency(String serviceId, String techServDescId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotFoundFault;

	String insertNewUserFeedback(String serviceId, UserFeedback uFeedback)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault;

	UserFeedback findUserFeedback(String serviceId, String uFBId)
			throws DAOGeneralSystemFault, DAONotFoundFault;
	
	String deleteUserFeedback(String serviceId, String uFBId)
			throws DAOGeneralSystemFault, DAONotFoundFault, DAOUpdateFailedFault;
        
        String updateUserFeedback(String serviceId, String userFeedbackId, UserFeedback userFeedback)
            throws DAOGeneralSystemFault, DAOUpdateFailedFault, DAONotFoundFault;
}
