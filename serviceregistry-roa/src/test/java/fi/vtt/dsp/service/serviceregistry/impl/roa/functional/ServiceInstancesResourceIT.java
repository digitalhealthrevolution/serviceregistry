package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseResourceIT;
import java.io.IOException;
import java.util.List;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;

public class ServiceInstancesResourceIT extends BaseResourceIT {

	@Test
	public void getAllServiceInstancesForAService() throws JsonParseException,
			JsonMappingException, IOException {
		WebClient client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id + "/serviceinstances");
		Response r = client.get();
		Assert.assertEquals(200, r.getStatus());
		String responseBody = r.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();
		List<ServiceInstance> sInsts = mapper.readValue(responseBody,
				List.class);
		Assert.assertTrue(sInsts.size() > 0);
		client.close();
	}
}
