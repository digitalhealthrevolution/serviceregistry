package fi.vtt.dsp.service.serviceregistry.auth.handlers;

import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import fi.vtt.dsp.service.serviceregistry.auth.token.AccessTokenFactory;

public class AccessTokenAuthenticationProvider implements AuthenticationProvider {

	private String sharedSecret;
	
	@Override
	public Authentication authenticate(Authentication authentication) {

		String userName = authentication.getName();
		String password = authentication.getCredentials().toString();

		AccessTokenFactory atFactory = new AccessTokenFactory(getSharedSecret());
		AccessToken at = atFactory.parse(userName, password);

		if (at.isValid() && at.isAuthentic()) {

			List<GrantedAuthority> grantedAuths = new ArrayList<>();
			grantedAuths.add(new SimpleGrantedAuthority(at.getAgent().getRole().toString()));

			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userName, password, grantedAuths);

			auth.setDetails(at.getAgent());

			return auth;

		} else {
			throw new BadCredentialsException("Invalid username or password");
		}
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	/**
	 * @return the sharedSecret
	 */
	public String getSharedSecret() {
		return sharedSecret;
	}

	/**
	 * @param sharedSecret the sharedSecret to set
	 */
	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

}
