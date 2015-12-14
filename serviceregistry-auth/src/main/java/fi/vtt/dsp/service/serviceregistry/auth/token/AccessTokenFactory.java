package fi.vtt.dsp.service.serviceregistry.auth.token;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

public final class AccessTokenFactory {

	private static final Logger LOG = Logger.getLogger(AccessTokenFactory.class.getName());

	public static final String ADMIN_ID = "ADMIN-AGENT";

	private final String SHARED_SECRET;

	public AccessTokenFactory(String sharedSecret) {
		this.SHARED_SECRET = sharedSecret;
	}

	private String getAdminId(String id) {
		return id + "_" + ADMIN_ID;
	}

	private String encode(String data) throws NoSuchAlgorithmException,
			InvalidKeyException, IllegalStateException,
			UnsupportedEncodingException {

		Mac sha256HMAC = Mac.getInstance("HmacSHA256");
		SecretKeySpec key = new SecretKeySpec(SHARED_SECRET.getBytes("UTF8"), "HmacSHA256");
		sha256HMAC.init(key);
		return Base64.encodeBase64String(sha256HMAC.doFinal(data.getBytes("UTF8")));
	}

	private String generateSignature(String id, AgentType type, AgentRole role) {

		StringBuilder signatureBuilder = new StringBuilder();

		signatureBuilder.append(type);
		signatureBuilder.append(":");
		if (role.equals(AgentRole.ROLE_ADMIN)) {
			signatureBuilder.append(getAdminId(id));
		} else {
			signatureBuilder.append(id);
		}
		String signature = null;
		try {
			signature = encode(signatureBuilder.toString());
		} catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException | UnsupportedEncodingException e) {
			LOG.log(Level.SEVERE, "Error while generating access token signature: " + e.getMessage());
		}

		return signature;
	}

	private String parseAgentId(String basicAuthUsername) {
		return basicAuthUsername.substring(basicAuthUsername.indexOf('-') + 1);
	}

	private AgentType parseAgentType(String basicAuthUsername) {
		String type = basicAuthUsername.substring(0, basicAuthUsername.indexOf('-'));
		AgentType agentType = null;
		try {
			agentType = AgentType.valueOf(type.toUpperCase());
		} 
		catch(IllegalArgumentException e) {}
		return agentType;
	}

	private AgentRole parseAgentRole(String agentId, AgentType agentType, String basicAuthPassword) {
		String adminSignature = generateSignature(agentId, agentType, AgentRole.ROLE_ADMIN);
		if (adminSignature.equals(basicAuthPassword)) {
			return AgentRole.ROLE_ADMIN;
		} else {
			return AgentRole.ROLE_REGISTERED;
		}
	}

	public AccessToken create(String id, AgentType type, AgentRole role) {
		return new AccessTokenImpl(new Agent(id, type, role));
	}

	public AccessToken parse(String basicAuthUsername, String basicAuthPassword) {

		AccessToken token = null;
		if(StringUtils.isBlank(basicAuthUsername) || StringUtils.isBlank(basicAuthPassword)) {
			token = new AccessTokenImpl(null, null);
		} else {
			String agentId = parseAgentId(basicAuthUsername);
			AgentType agentType = parseAgentType(basicAuthUsername);
			AgentRole agentRole = parseAgentRole(agentId, agentType, basicAuthPassword);
			if( agentId != null && agentType != null && agentRole != null ) {
				Agent agent = new Agent(agentId, agentType, agentRole);	
				token = new AccessTokenImpl(agent, basicAuthPassword);
			} else {
				token = new AccessTokenImpl(null, null);				
			}
		}
		return token;
	}
	
	private class AccessTokenImpl implements AccessToken {

		private final Agent agent;
		private final String signature;
	
		public AccessTokenImpl(Agent agent) {
			this.agent = agent;
			this.signature = generateSignature(agent.getId(), agent.getType(), agent.getRole());			
		}
		public AccessTokenImpl(Agent agent, String signature) {
			this.agent = agent;
			this.signature = signature;
		}
		
		@Override
		public String getBasicAuthUsername() {

			if (this.agent == null) {
				return null;
			}

			StringBuilder builder = new StringBuilder();
			builder.append(this.agent.getType().toString().toLowerCase());
			builder.append("-");
			builder.append(this.agent.getId());
			return builder.toString();
		}

		@Override
		public String getBasicAuthPassword() {
			return this.signature;
		}

		@Override
		public boolean isValid() {
			return (this.agent != null && !StringUtils.isEmpty(this.signature));
		}

		@Override
		public boolean isAuthentic() {
			if (this.isValid()) {
				String expectedSignature = generateSignature(agent.getId(), agent.getType(), agent.getRole());
				return expectedSignature.equals(this.signature);
			} else {
				return false;
			}
		}

		@Override
		public Agent getAgent() {
			return agent;
		}
	}

}
