package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseResourceIT;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;

public class BindingsResourceIT extends BaseResourceIT {

	@Test
	public void createBinding() throws DatatypeConfigurationException,
			IOException {

		WebClient client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id + "/serviceinstances");
		Response r = client.get();
		Assert.assertEquals(200, r.getStatus());
		String responseBody = r.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();

		ServiceInstance[] sInstArr = mapper.readValue(responseBody,
				ServiceInstance[].class);
		if (sInstArr.length == 1) {
			ServiceInstance sInst = sInstArr[0];

			Binding b = new Binding();
			b.setBoundByServiceId("1");
			b.setBoundByServiceInstanceId("2");
			b.setRequestedByUserId("3");
			b.setAuthorizedByUserId("4");

			b.setRequestedOnDate(System.currentTimeMillis() / 1000);
			b.setModifiedOnDate(System.currentTimeMillis() / 1000);

			// b.setRequestedOnDate(DatatypeFactory.newInstance().newXMLGregorianCalendar());
			// b.setModifiedOnDate(DatatypeFactory.newInstance().newXMLGregorianCalendar());
			b.setStatusRequested(true);
			b.setStatusAuthorized(true);
			b.setStatusPending(true);
			b.setStatusActive(true);

			client = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id
							+ "/serviceinstances/"
							+ sInst.getServiceInstanceId()
							+ "/serviceaccessendpoint/bindings");
			r = client.post(b);
			Assert.assertEquals(201, r.getStatus());

			String bindingURI = r.getLocation().toString();
			Assert.assertNotNull(bindingURI);
			String bindingId = bindingURI
					.substring(bindingURI.lastIndexOf("/") + 1);
			client.close();

			client = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id
							+ "/serviceinstances/"
							+ sInst.getServiceInstanceId()
							+ "/serviceaccessendpoint/bindings/" + bindingId);
			r = client.get();
			Assert.assertEquals(200, r.getStatus());
			responseBody = r.readEntity(String.class);
			Binding b2 = mapper.readValue(responseBody, Binding.class);
			Assert.assertNotNull(b2);
			client = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id
							+ "/serviceinstances/"
							+ sInst.getServiceInstanceId()
							+ "/serviceaccessendpoint/bindings/" + bindingId);
			r = client.delete();
			Assert.assertEquals(200, r.getStatus());
		}
	}

	@Test
	public void getBinding() throws JsonParseException, JsonMappingException,
			IOException {
		// This test compares binding retrieved from instance to the one fetched
		// through direct URI
		WebClient client = this
				.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
						+ id + "/serviceinstances");
		Response r = client.get();
		Assert.assertEquals(200, r.getStatus());
		String responseBody = r.readEntity(String.class);
		ObjectMapper mapper = new ObjectMapper();

		ServiceInstance[] sInstArr = mapper.readValue(responseBody,
				ServiceInstance[].class);
		if (sInstArr.length == 1) {
			ServiceInstance sInst = sInstArr[0];
			List<Binding> bindings = sInst.getServiceAccessEndPoint()
					.getBinding();

			WebClient client2 = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id
							+ "/serviceinstances/"
							+ sInst.getServiceInstanceId()
							+ "/serviceaccessendpoint/bindings");
			Response r2 = client2.get();
			String responseBody2 = r2.readEntity(String.class);

			List<Binding> bindings2 = Arrays.asList(mapper.readValue(
					responseBody2, Binding[].class));
			Assert.assertEquals(bindings.size(), bindings2.size());
			client2.close();
		}
		client.close();
	}
}
