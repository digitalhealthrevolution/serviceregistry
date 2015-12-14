package fi.vtt.dsp.service.serviceregistry.auth.resource;

import org.springframework.security.core.Authentication;

public class ServiceRegistrationSearchResourceAuthorization {
	// Everybody can search. What is retured is determined in the actual
	// search-method
	public boolean canSearch(Authentication authentication) {
		return true;
	}
}
