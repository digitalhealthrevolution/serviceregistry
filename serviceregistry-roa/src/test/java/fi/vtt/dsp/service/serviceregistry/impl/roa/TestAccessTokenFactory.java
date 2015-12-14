/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.TestProperty;
import fi.vtt.dsp.service.serviceregistry.TestProperties;
import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import fi.vtt.dsp.service.serviceregistry.auth.token.AccessTokenFactory;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;

/**
 *
 * @author JLJUHANI
 */
public class TestAccessTokenFactory {

	public final static AccessTokenFactory AT_FACTORY;
	
	static {
		AT_FACTORY = new AccessTokenFactory(TestProperties.get(TestProperty.AUTH_SECRET));
	}
	
	public static AccessToken getAdminUserAt(String userId) {
		return AT_FACTORY.create(userId, AgentType.USER, AgentRole.ROLE_ADMIN);		
	}
	
	public static AccessToken getInvalidAccessToken() {
		return AT_FACTORY.parse("invalidtype-123456", "invalidsignature");
	}
	
	public static AccessToken getInauthenticAccessToken() {
		return AT_FACTORY.parse("service-123456", "invalidsignature");
	}

	public static AccessToken getAdminAgentAT(UserProfile profile) {
		return AT_FACTORY.create(profile.getUserId(), AgentType.USER, AgentRole.ROLE_ADMIN);
	}

	public static AccessToken getAdminAgentAT(ServiceRegistryEntry service) {
		return AT_FACTORY.create(service.getServiceId(), AgentType.SERVICE, AgentRole.ROLE_ADMIN);
	}

	public static AccessToken getAdminAgentAT(ServiceInstance inst) {
		return AT_FACTORY.create(inst.getServiceInstanceId(), AgentType.SERVICE_INSTANCE, AgentRole.ROLE_ADMIN);
	}

	public static AccessToken getGuestAgentAT(UserProfile profile) {
		return AT_FACTORY.create(profile.getUserId(), AgentType.UNDEFINED, AgentRole.ROLE_GUEST);
	}

	public static AccessToken getRegisteredAgentAT(UserProfile profile) {
		return AT_FACTORY.create(profile.getUserId(), AgentType.USER, AgentRole.ROLE_REGISTERED);
	}

	public static AccessToken getRegisteredAgentAT(ServiceRegistryEntry service) {
		return AT_FACTORY.create(service.getServiceId(), AgentType.SERVICE, AgentRole.ROLE_REGISTERED);
	}

	public static AccessToken getRegisteredAgentAT(ServiceInstance inst) {
		return AT_FACTORY.create(inst.getServiceInstanceId(), AgentType.SERVICE_INSTANCE, AgentRole.ROLE_REGISTERED);
	}
	
}
