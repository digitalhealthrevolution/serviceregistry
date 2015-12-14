package fi.vtt.dsp.service.serviceregistry.impl.roa.functional;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.roa.BaseAuthIT;
import fi.vtt.dsp.service.serviceregistry.impl.roa.TestAccessTokenFactory;
import fi.vtt.dsp.serviceframework.common.Binding;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class BindingPushIT extends BaseAuthIT {
	private static final Logger LOGGER = Logger.getLogger(BindingPushIT.class.getName());
	ServiceRegistryEntry serviceRegistryEntry;
	ServiceInstance serviceInstance;
	Binding binding;
	UserProfile userProfile;
	private static boolean gotBinding = false;
	
	static class MyHandler implements HttpHandler {
        public void handle(HttpExchange httpExchange) throws IOException {
			String response = "Just a response...";
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
			String line;
			StringBuilder stringBuilder = new StringBuilder();

			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}

			String message = stringBuilder.toString();
			
			try {
				ObjectMapper objectMapper = new ObjectMapper();
				Binding[] bindings = objectMapper.readValue(message, Binding[].class);
				
				if (bindings.length == 1) {
					gotBinding = true;
				}
			}
			catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error getting bindings ", e);
			}
			
            httpExchange.sendResponseHeaders(200, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
	
	@Test
	public void test_createBinding() {
		try {
			userProfile = createUserProfile();
			serviceRegistryEntry = createService(userProfile.getUserId());
			serviceInstance = createServiceInstance(userProfile.getUserId(), serviceRegistryEntry.getServiceId());
			serviceInstance.getBindingRequestEndPoint().setBindingRequestURI("http://localhost:61234");
			serviceInstance = updateServiceInstance(serviceRegistryEntry, serviceInstance);
			
			HttpServer server = HttpServer.create(new InetSocketAddress(61234), 0);
			server.createContext("/", new MyHandler());
			server.setExecutor(null); // creates a default executor
			server.start();
			
			Binding b = new Binding();

			b.setBoundByServiceId("1");
			b.setBoundByServiceInstanceId("2");
			b.setRequestedByUserId(userProfile.getUserId());
			b.setAuthorizedByUserId(userProfile.getUserId());
			b.setRequestedOnDate(System.currentTimeMillis() / 1000);
			b.setModifiedOnDate(System.currentTimeMillis() / 1000);
			b.setStatusRequested(true);
			b.setStatusAuthorized(true);
			b.setStatusPending(true);
			b.setStatusActive(true);

			LOGGER.log(Level.FINE, "Sending binding");
			WebClient client = setupJSONClient("serviceregistrations/"
							+ serviceRegistryEntry.getServiceId()
							+ "/serviceinstances/"
							+ serviceInstance.getServiceInstanceId()
							+ "/serviceaccessendpoint/bindings", TestAccessTokenFactory.getAdminAgentAT(userProfile));
			Response r = client.post(b);
			Assert.assertEquals(201, r.getStatus());
			LOGGER.log(Level.FINE, "Binding sent");
			
			// A bit nasty way to test this...
			Thread.sleep(1000);			
			Assert.assertEquals(true, gotBinding);
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Something went wrong testing binding-push", e);
			Assert.fail();
		}
	}
}
