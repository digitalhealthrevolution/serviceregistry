package fi.vtt.dsp.service.serviceregistry.impl.handlers;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceInstanceDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.ServiceRegistryDAO;
import fi.vtt.dsp.serviceframework.common.Binding;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.jaxrs.client.WebClient;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;

public class BindingsUpdater {
	private static final Logger LOGGER = Logger.getLogger(BindingsUpdater.class
			.getName());

	public BindingsUpdater() {
	}

	public static void postUpdatedBindings(final String serviceId, final String instanceId) {

		Thread updaterThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					MongoDBServiceInstanceDAO serviceInstanceDAO = new MongoDBServiceInstanceDAO();
					ServiceInstance servInst = serviceInstanceDAO
							.findServiceInstance(serviceId, instanceId);
					
					if (servInst.getBindingRequestEndPoint().getBindingRequestURI() == null || servInst.getBindingRequestEndPoint().getBindingRequestURI().equals("")) {
						return;
					}
					
					List<Binding> bindings = servInst
							.getServiceAccessEndPoint().getBinding();

					List<Object> providers = new ArrayList<>();
					JacksonJaxbJsonProvider jacksonJaxbJsonProvider = new JacksonJaxbJsonProvider();
					providers.add(jacksonJaxbJsonProvider);
					JAXRSClientFactoryBean clientFactoryBean = new JAXRSClientFactoryBean();
					String bindingRequestUri = servInst.getBindingRequestEndPoint().getBindingRequestURI();
					if( StringUtils.isNotEmpty(bindingRequestUri) ) {
						clientFactoryBean.setAddress(bindingRequestUri);
						clientFactoryBean.setProviders(providers);

						WebClient client = clientFactoryBean.createWebClient();
						client.type(MediaType.APPLICATION_JSON_TYPE).accept(
								MediaType.APPLICATION_JSON_TYPE);

						ObjectMapper mapper = new ObjectMapper();

						Response response = client.post(mapper
								.writeValueAsString(bindings));
					} else {
						LOGGER.log(Level.FINE, "No binding request URI, skipping bindings update");						
					}					
                                                                                
				} catch (java.net.UnknownHostException e) {
					LOGGER.log(Level.FINE, "Unknown host ");
				}
				catch (Exception e) {
					LOGGER.log(Level.FINE, "Error posting updated bindings "
							+ e.getMessage());
				}
			}
		});

		updaterThread.start();
	}

	public static void removeOrphanBindings(final String serviceId) {
		try {
			ServiceRegistryDAO serviceRegistryDAO = new MongoDBServiceRegistryDAO();
			ServiceRegistryEntry serviceRegistryEntry = serviceRegistryDAO
					.findServiceRegistryEntry(serviceId);

			for (ServiceInstance serviceInstance : serviceRegistryEntry
					.getServiceInstance()) {
				removeOrphanBindings(serviceId,
						serviceInstance.getServiceInstanceId());
			}
		} catch (Exception e) {
			LOGGER.log(Level.FINE,
					"Error removing orphan bindings " + e.getMessage());
		}
	}

	public static void removeOrphanBindings(final String serviceId, final String instanceId) {
        try {
            ServiceRegistryDAO serviceRegistryDAO = new MongoDBServiceRegistryDAO();
            MongoDBServiceInstanceDAO serviceInstanceDAO = new MongoDBServiceInstanceDAO();
            ServiceInstance toBeDeletedServiceInstance = serviceInstanceDAO.findServiceInstance(serviceId, instanceId);
            List<ServiceRegistryEntry> serviceRegistryEntries = serviceRegistryDAO.getAll();
            
            for (ServiceRegistryEntry serviceRegistryEntry : serviceRegistryEntries) {
                for (ServiceInstance serviceInstance : serviceRegistryEntry.getServiceInstance()) {
                    List<Binding> bindingsToBeDeleted = new ArrayList<>();
                    
                    if (serviceInstance.getServiceAccessEndPoint() != null && serviceInstance.getServiceAccessEndPoint().getBinding() != null) {
                        
                        for (Binding binding : serviceInstance.getServiceAccessEndPoint().getBinding()) {
                            if (binding.getBoundByServiceInstanceId().equals(toBeDeletedServiceInstance.getServiceInstanceId())) {
                                bindingsToBeDeleted.add(binding);
                                LOGGER.log(Level.FINE, "Found orphan. Binding: " + binding.getBindingId());
                            }
                        }

                        if (bindingsToBeDeleted.size() > 0) {
                            for (Binding binding : bindingsToBeDeleted) {
                                serviceInstance.getServiceAccessEndPoint().getBinding().remove(binding);
                            }

                            serviceInstanceDAO.updateServiceInstance(serviceRegistryEntry.getServiceId(), serviceInstance.getServiceInstanceId(), serviceInstance);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            LOGGER.log(Level.FINE, "Error removing orphan bindings " + e.getMessage());
        }
    }
}
