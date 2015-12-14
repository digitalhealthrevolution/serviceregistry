package fi.vtt.dsp.service.serviceregistry.auth;

import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import static org.junit.Assert.*;

import org.junit.Test;

import fi.vtt.dsp.service.serviceregistry.auth.token.AccessTokenFactory;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;

public class AccessTokenUT {

	private final String SHARED_SECRET = "test-secret";
	
	@Test
	public void testAccessTokenCreation() {
		String id = "123456";
		AgentType type = AgentType.SERVICE;
		String password = "password";

		String userName = type.toString().toLowerCase() + "-" + id;

		AccessTokenFactory tokenFactory = new AccessTokenFactory(SHARED_SECRET);
		AccessToken token = tokenFactory.parse(userName, password);

		assertNotEquals(null, token);
		assertEquals(userName, token.getBasicAuthUsername());
		assertEquals(password, token.getBasicAuthPassword());
	}

	@Test
	public void testNullAccessToken() {
		AccessTokenFactory tokenFactory = new AccessTokenFactory(SHARED_SECRET);
		AccessToken token = tokenFactory.parse(null, null);		

		assertNotEquals(null, token);
		assertFalse(token.isValid());
		assertFalse(token.isAuthentic());
	}

	@Test
	public void testEmptyAccessToken() {
		AccessTokenFactory tokenFactory = new AccessTokenFactory(SHARED_SECRET);
		AccessToken token = tokenFactory.parse("", "");		

		assertNotEquals(null, token);
		assertFalse(token.isValid());
		assertFalse(token.isAuthentic());
	}

	@Test
	public void testInvalidAccessToken() {

		AccessTokenFactory tokenFactory = new AccessTokenFactory(SHARED_SECRET);
		AccessToken token = tokenFactory.parse("invalidType-id", "signature");		

		assertNotEquals(null, token);
		assertFalse(token.isValid());
		assertFalse(token.isAuthentic());
	}

	@Test
	public void testUnauthenticAccessToken() {

		String id = "123456";
		AgentType type = AgentType.SERVICE;
		String invalidSignature = "invalidSignature";

		String userName = type.toString().toLowerCase() + "-" + id;

		AccessTokenFactory tokenFactory = new AccessTokenFactory(SHARED_SECRET);
		AccessToken token = tokenFactory.parse(userName, invalidSignature);		

		assertTrue(token.isValid());
		assertFalse(token.isAuthentic());
		assertNotEquals(null, token.getAgent());
		assertNotEquals(null, token.getBasicAuthUsername());
		assertNotEquals(null, token.getBasicAuthPassword());

		assertEquals(id, token.getAgent().getId());
		assertEquals(type, token.getAgent().getType());
		assertEquals(AgentRole.ROLE_REGISTERED, token.getAgent().getRole());

	}

	@Test
	public void testNormalUserAccessToken() {
		testGeneratedAccessToken("123456", AgentType.USER,
				AgentRole.ROLE_REGISTERED);
	}

	@Test
	public void testAdminUserAccessToken() {
		testGeneratedAccessToken("123456", AgentType.USER, AgentRole.ROLE_ADMIN);
	}

	@Test
	public void testNormalServiceAccessToken() {
		testGeneratedAccessToken("123456", AgentType.SERVICE,
				AgentRole.ROLE_REGISTERED);
	}

	@Test
	public void testAdminServiceAccessToken() {
		testGeneratedAccessToken("123456", AgentType.SERVICE,
				AgentRole.ROLE_ADMIN);
	}

	@Test
	public void testNormalServiceInstanceAccessToken() {
		testGeneratedAccessToken("123456", AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_REGISTERED);
	}

	@Test
	public void testAdminServiceInstanceAccessToken() {
		testGeneratedAccessToken("123456", AgentType.SERVICE_INSTANCE,
				AgentRole.ROLE_ADMIN);
	}

	private void testGeneratedAccessToken(String id, AgentType type,
			AgentRole role) {

		AccessTokenFactory tokenFactory = new AccessTokenFactory(SHARED_SECRET);
		AccessToken generated = tokenFactory.create(id, type, role);		

		String userName = generated.getBasicAuthUsername();
		String password = generated.getBasicAuthPassword();

		AccessToken parsed = tokenFactory.parse(userName, password);

		assertTrue(generated.isValid());
		assertTrue(generated.isAuthentic());
		assertNotEquals(null, generated.getAgent());

		assertEquals(id, generated.getAgent().getId());
		assertEquals(type, generated.getAgent().getType());
		assertEquals(role, generated.getAgent().getRole());

		assertTrue(parsed.isValid());
		assertTrue(parsed.isAuthentic());
		assertNotEquals(null, parsed.getAgent());

		assertEquals(generated.getAgent().getId(), parsed.getAgent().getId());
		assertEquals(generated.getAgent().getType(), parsed.getAgent()
				.getType());
		assertEquals(generated.getAgent().getRole(), parsed.getAgent()
				.getRole());

		assertEquals(userName, parsed.getBasicAuthUsername());
		assertEquals(password, parsed.getBasicAuthPassword());

	}

}
