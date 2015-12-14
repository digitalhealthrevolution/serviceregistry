package fi.vtt.dsp.service.serviceregistry.impl.dao;

import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.GroupRole;
import java.util.List;

public interface UserGroupDAO {
	public List<UserGroup> getAllUserGroups() throws DAOGeneralSystemFault;
	public String insertNewUserGroup(UserGroup userGroup)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault;
	public boolean deleteUserGroup(String userGroupId)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault;
	public boolean updateUserGroup(UserGroup userGroup)
			throws DAOUpdateFailedFault, DAOGeneralSystemFault,
			DAONotSavedFault, DAONotFoundFault;
	public List<GroupRole> getAllGroupRolesForUser(String userProfileId)
			throws DAOGeneralSystemFault, DAONotFoundFault;
	public UserGroup getUserGroupById(String userGroupId)
			throws DAOGeneralSystemFault, DAONotFoundFault;
	public List<UserGroup> getAllUserGroupsForUser(String userId)
			throws DAOGeneralSystemFault, DAONotFoundFault;
}
