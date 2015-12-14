package fi.vtt.dsp.service.serviceregistry.auth.token;

import java.io.Serializable;

public class Agent implements Serializable {

	private static final long serialVersionUID = -4230005666391591382L;

	private final String id;
	private final AgentType type;
	private final AgentRole role;

	public Agent() {
		this.id = "";
		this.type = AgentType.UNDEFINED;
		this.role = AgentRole.ROLE_GUEST;
	}

	public Agent(String id, AgentType type, AgentRole role) {
		if (id != null) {
			this.id = id;
		} else {
			this.id = "";
		}
		if (type != null) {
			this.type = type;
		} else {
			this.type = AgentType.UNDEFINED;
		}
		if (role != null) {
			this.role = role;
		} else {
			this.role = AgentRole.ROLE_GUEST;
		}
	}

	public String getId() {
		return id;
	}

	public AgentType getType() {
		return type;
	}

	public AgentRole getRole() {
		return role;
	}
}
