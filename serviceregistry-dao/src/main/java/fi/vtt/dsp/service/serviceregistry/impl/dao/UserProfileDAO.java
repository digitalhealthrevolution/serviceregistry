/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vtt.dsp.service.serviceregistry.impl.dao;

import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import java.util.List;

/**
 *
 * @author JLJUHANI
 */
public interface UserProfileDAO {

	String create(UserProfile userProfile) throws DAOUpdateFailedFault, DAOGeneralSystemFault, DAONotSavedFault, DAONotFoundFault;

	boolean delete(String userProfileId) throws DAOGeneralSystemFault, DAONotFoundFault;

	boolean deleteUserProfileByEmail(String userEmail) throws DAOGeneralSystemFault, DAONotFoundFault;

	UserProfile findUserProfileByUserId(String userId) throws DAOGeneralSystemFault, DAONotFoundFault;

	List<UserProfile> getAll() throws DAOGeneralSystemFault, DAONotFoundFault;

	List<UserProfile> getAllByUser(String userProfileId) throws DAOGeneralSystemFault, DAONotFoundFault;

	UserProfile read(String userEmail) throws DAOGeneralSystemFault, DAONotFoundFault;

	boolean update(String userProfileId, UserProfile userProfile) throws DAOGeneralSystemFault, DAOUpdateFailedFault, DAONotFoundFault;
	
}
