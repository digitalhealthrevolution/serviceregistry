package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseResourceIT;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;

public class DependenciesResourceIT extends BaseResourceIT {

	@Test
	public void createDependency() throws IOException {

		Dependency d = new Dependency();
		d.setDependsOnServiceId("1");

		WebClient client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id + "/servicedescription/dependencies");
		Response r = client.post(d);
		Assert.assertEquals(201, r.getStatus());

		String dependencyURI = r.getLocation().toString();
		Assert.assertNotNull(dependencyURI);
		String dependencyId = dependencyURI.substring(dependencyURI
				.lastIndexOf("/") + 1);
		client.close();

		client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id + "/servicedescription/dependencies");
		r = client.get();
		Assert.assertEquals(200, r.getStatus());
		String responseBody = r.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		Dependency[] dependencyArr = mapper.readValue(responseBody,
				Dependency[].class);
		Assert.assertTrue(dependencyArr.length > 0);

		client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id + "/servicedescription/dependencies/"
						+ dependencyId);
		r = client.get();
		Assert.assertEquals(200, r.getStatus());
		responseBody = r.readEntity(String.class);
		d = mapper.readValue(responseBody, Dependency.class);
		Assert.assertNotNull(d);

		d.setDependsOnServiceId("2");
		client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id + "/servicedescription/dependencies/"
						+ dependencyId);
		r = client.put(d);
		Assert.assertEquals(200, r.getStatus());

		client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id + "/servicedescription/dependencies/"
						+ dependencyId);
		r = client.delete();
		Assert.assertEquals(200, r.getStatus());
	}
}
