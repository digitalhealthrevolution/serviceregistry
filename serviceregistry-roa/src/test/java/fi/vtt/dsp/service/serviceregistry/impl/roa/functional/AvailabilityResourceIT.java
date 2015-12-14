package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseResourceIT;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.serviceframework.common.ServiceAvailability;

public class AvailabilityResourceIT extends BaseResourceIT {

	@Test
	public void getAvailability() throws JsonParseException,
			JsonMappingException, IOException {
		// This test compares availability retrieved from instance to the one
		// fetched through direct URI
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
			Availability ava = sInst.getServiceAccessEndPoint()
					.getAvailability();

			WebClient client2 = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id
							+ "/serviceinstances/"
							+ sInst.getServiceInstanceId()
							+ "/serviceaccessendpoint/availability");
			Response r2 = client2.get();
			String responseBody2 = r2.readEntity(String.class);

			Availability ava2 = new Availability();
			if (responseBody2 != null && !responseBody2.equals("")) {
				ava2 = mapper.readValue(responseBody2, Availability.class);
			}

			Assert.assertNotNull(ava.getSelfReportedAvailability());
			Assert.assertNotNull(ava2.getSelfReportedAvailability());
			Assert.assertNotNull(ava.getInspectedAvailability());
			Assert.assertNotNull(ava2.getInspectedAvailability());

			Assert.assertEquals(ava.getSelfReportedAvailability()
					.getAverageExecutionTime(), ava2
					.getSelfReportedAvailability().getAverageExecutionTime());
			Assert.assertEquals(ava.getSelfReportedAvailability()
					.getAverageLatency(), ava2.getSelfReportedAvailability()
					.getAverageLatency());
			Assert.assertEquals(ava.getSelfReportedAvailability()
					.getMaxResponseTime(), ava2.getSelfReportedAvailability()
					.getMaxResponseTime());
			Assert.assertEquals(ava.getSelfReportedAvailability()
					.getAverageGoodput(), ava2.getSelfReportedAvailability()
					.getAverageGoodput());
			Assert.assertEquals(ava.getSelfReportedAvailability()
					.getTimeStamp(), ava2.getSelfReportedAvailability()
					.getTimeStamp());
			Assert.assertEquals(ava.getSelfReportedAvailability()
					.getUptimeHours(), ava2.getSelfReportedAvailability()
					.getUptimeHours());

			Assert.assertEquals(ava.getInspectedAvailability()
					.getAverageExecutionTime(), ava2.getInspectedAvailability()
					.getAverageExecutionTime());
			Assert.assertEquals(ava.getInspectedAvailability()
					.getAverageLatency(), ava2.getInspectedAvailability()
					.getAverageLatency());
			Assert.assertEquals(ava.getInspectedAvailability()
					.getMaxResponseTime(), ava2.getInspectedAvailability()
					.getMaxResponseTime());
			Assert.assertEquals(ava.getInspectedAvailability()
					.getAverageGoodput(), ava2.getInspectedAvailability()
					.getAverageGoodput());
			Assert.assertEquals(ava.getInspectedAvailability().getTimeStamp(),
					ava2.getInspectedAvailability().getTimeStamp());
			Assert.assertEquals(
					ava.getInspectedAvailability().getUptimeHours(), ava2
							.getInspectedAvailability().getUptimeHours());

			client2.close();
		}

		client.close();
	}

	@Test
	public void getInspectedAvailability() throws JsonParseException,
			JsonMappingException, IOException {
		// This test compares inspected availability retrieved from instance to
		// the one fetched through direct URI

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
			ServiceAvailability insAva = sInst.getServiceAccessEndPoint()
					.getAvailability().getInspectedAvailability();

			WebClient client2 = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id
							+ "/serviceinstances/"
							+ sInst.getServiceInstanceId()
							+ "/serviceaccessendpoint/availability/inspected");
			Response r2 = client2.get();
			String responseBody2 = r2.readEntity(String.class);

			ServiceAvailability insAva2 = new ServiceAvailability();
			if (responseBody2 != null && !responseBody2.equals("")) {
				insAva2 = mapper.readValue(responseBody2,
						ServiceAvailability.class);
			}
			Assert.assertEquals(insAva.getAverageExecutionTime(),
					insAva2.getAverageExecutionTime());
			Assert.assertEquals(insAva.getAverageLatency(),
					insAva2.getAverageLatency());
			Assert.assertEquals(insAva.getMaxResponseTime(),
					insAva2.getMaxResponseTime());
			Assert.assertEquals(insAva.getAverageGoodput(),
					insAva2.getAverageGoodput());
			Assert.assertEquals(insAva.getTimeStamp(), insAva2.getTimeStamp());
			Assert.assertEquals(insAva.getUptimeHours(),
					insAva2.getUptimeHours());

			client2.close();
		}

		client.close();
	}

	@Test
	public void getSelfReportedAvailability() throws JsonParseException,
			JsonMappingException, IOException {
		// This test compares self reported availability retrieved from instance
		// to the one fetched through direct URI
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
			ServiceAvailability selfAva = sInst.getServiceAccessEndPoint()
					.getAvailability().getSelfReportedAvailability();

			WebClient client2 = this
					.setUpJSONClient("resourcedirectory/v1/serviceregistrations/"
							+ id
							+ "/serviceinstances/"
							+ sInst.getServiceInstanceId()
							+ "/serviceaccessendpoint/availability/selfreported");
			Response r2 = client2.get();
			String responseBody2 = r2.readEntity(String.class);

			ServiceAvailability selfAva2 = new ServiceAvailability();
			if (responseBody2 != null && !responseBody2.equals("")) {
				selfAva2 = mapper.readValue(responseBody2,
						ServiceAvailability.class);
			}

			Assert.assertEquals(selfAva.getAverageExecutionTime(),
					selfAva2.getAverageExecutionTime());
			Assert.assertEquals(selfAva.getAverageLatency(),
					selfAva2.getAverageLatency());
			Assert.assertEquals(selfAva.getMaxResponseTime(),
					selfAva2.getMaxResponseTime());
			Assert.assertEquals(selfAva.getAverageGoodput(),
					selfAva2.getAverageGoodput());
			Assert.assertEquals(selfAva.getTimeStamp(), selfAva2.getTimeStamp());
			Assert.assertEquals(selfAva.getUptimeHours(),
					selfAva2.getUptimeHours());

			client2.close();
		}

		client.close();
	}

}
