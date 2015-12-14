package fi.vtt.dsp.service.serviceregistry.auth.resource;

import fi.vtt.dsp.service.serviceregistry.auth.ResourceAuthorization;
import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryLogEntry;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceRegistryDAO;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.security.core.Authentication;

public class LogResourceAuthorization extends ResourceAuthorization {
	private static final Logger LOGGER = Logger.getLogger(LogResourceAuthorization.class.getName());
    private ServiceRegistryLogEntry logItem;
    final private ServiceRegistryDAO serviceRegistryDAO;
    private String serviceId;
    
    public LogResourceAuthorization() throws DAOGeneralSystemFault {
		super();
        serviceRegistryDAO = new MongoDBServiceRegistryDAO();
	}
    
    public LogResourceAuthorization set(ServiceRegistryLogEntry logItem) {
		this.logItem = logItem;
		return this;
	}
    
    public LogResourceAuthorization set(String serviceId) {
		this.serviceId = serviceId;
		return this;
	}
    
	public boolean canGet(Authentication authentication) {
		Agent agent = getAgent(authentication);
        String agentId = agent.getId();
        
        if (agent.getRole() == AgentRole.ROLE_ADMIN) {
            return true;
        }
        else if (agent.getRole() == AgentRole.ROLE_GUEST) {
            return false;
        }
        
        // Role is registered
        
        try {
            if (agent.getType() == AgentType.USER && 
                    isUserAuthorizedToAccessService(agentId, serviceRegistryDAO.findServiceRegistryEntry(serviceId))) {
                return true;
            }
        }
        catch (DAOGeneralSystemFault | DAONotFoundFault e) {
            LOGGER.log(Level.SEVERE, "Unable to check if agent is allowed to send logs", e);
            return false;
        }
        
        try {
            if (agent.getType() == AgentType.SERVICE && agentId.equals(serviceId)) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to check if agent is allowed to send logs", e);
            return false;
        }
        
        try {
            if (agent.getType() == AgentType.SERVICE_INSTANCE && agentId.equals(serviceId)) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to check if agent is allowed to send logs", e);
            return false;
        }
        
        return false;
	}
    
    public boolean canInsert(Authentication authentication) {
        Agent agent = getAgent(authentication);
        String agentId = agent.getId();
        
        if (agent.getRole() == AgentRole.ROLE_ADMIN) {
            return true;
        }
        else if (agent.getRole() == AgentRole.ROLE_GUEST) {
            return false;
        }
        
        // Role is registered
        
        try {
            if (agent.getType() == AgentType.USER && 
                    isUserAuthorizedToModifyService(agentId, serviceRegistryDAO.findServiceRegistryEntry(logItem.getHostingServiceId()))) {
                return true;
            }
        }
        catch (DAOGeneralSystemFault | DAONotFoundFault e) {
            LOGGER.log(Level.SEVERE, "Unable to check if agent is allowed to send logs", e);
            return false;
        }
        
        try {
            if (agent.getType() == AgentType.SERVICE && agentId.equals(logItem.getHostingServiceId())) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to check if agent is allowed to send logs", e);
            return false;
        }
        
        try {
            if (agent.getType() == AgentType.SERVICE_INSTANCE && agentId.equals(logItem.getHostingInstanceId())) {
                return true;
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to check if agent is allowed to send logs", e);
            return false;
        }
        
        return false;
    }
}
