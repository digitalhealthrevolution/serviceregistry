package fi.vtt.dsp.service.serviceregistry.impl.roa;

import fi.vtt.dsp.service.serviceregistry.TestProperty;
import fi.vtt.dsp.service.serviceregistry.TestProperties;
import fi.vtt.dsp.service.serviceregistry.auth.token.AccessToken;
import java.util.ArrayList;
import java.util.List;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.http.MediaType;
import fi.vtt.dsp.service.serviceregistry.auth.token.AccessTokenFactory;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentRole;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.impl.dao.TestDataSetter;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotSavedFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUpdateFailedFault;

public abstract class BaseResourceIT {

	protected static String serviceRegistryBaseURL;
	protected String id = "";

	@BeforeClass
	public static void setupRegistryURL() {
		serviceRegistryBaseURL = TestProperties.get(TestProperty.SERVICE_URL);
	}

	@Before
	public void setDB() throws DAOGeneralSystemFault, DAONotSavedFault,
			DAONotFoundFault, DAOUpdateFailedFault {
		TestDataSetter tDSetter = new TestDataSetter();
		tDSetter.cleanUserProfiles();
		tDSetter.cleanRegistryEntries();
		ServiceRegistryEntry sRegEntr = tDSetter.setValidRegistryEntries();
		id = sRegEntr.getServiceId();
	}

	public WebClient setUpJSONClient(String path) {
		List<Object> providers = new ArrayList<>();
		providers.add(new JacksonJaxbJsonProvider());
		AccessTokenFactory atFactory = new AccessTokenFactory(TestProperties.get(TestProperty.AUTH_SECRET));
		AccessToken token = atFactory.create("123456", AgentType.USER,
				AgentRole.ROLE_ADMIN);
		WebClient client = WebClient.create(serviceRegistryBaseURL, providers,
				token.getBasicAuthUsername(), token.getBasicAuthPassword(),
				null);
		client.path(path);
		client.type(MediaType.APPLICATION_JSON_VALUE).accept(
				MediaType.APPLICATION_JSON_VALUE);
		return client;
	}
}
