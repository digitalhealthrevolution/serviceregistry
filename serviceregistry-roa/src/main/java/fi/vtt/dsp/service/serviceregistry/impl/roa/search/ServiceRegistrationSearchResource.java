package fi.vtt.dsp.service.serviceregistry.impl.roa.search;

import fi.vtt.dsp.service.serviceregistry.auth.token.Agent;
import fi.vtt.dsp.service.serviceregistry.auth.token.AgentType;
import java.beans.Introspector;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.apache.cxf.jaxrs.ext.search.SearchCondition;
import org.apache.cxf.jaxrs.ext.search.SearchContext;
import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserGroup;
import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceDiscoveryException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationDoesNotExistException;
import fi.vtt.dsp.service.serviceregistry.common.exceptions.ServiceRegistrationException;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserGroupDAO;
import fi.vtt.dsp.service.serviceregistry.impl.resourceframework.ServiceRegistryItemCollectionResource;
import fi.vtt.dsp.serviceframework.common.Binding;
import javax.ws.rs.PathParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

@Path("/resourcedirectory/v1/serviceregistrations/search")
public class ServiceRegistrationSearchResource
		extends
			ServiceRegistryItemCollectionResource<ServiceRegistryEntry> {

	public ServiceRegistrationSearchResource() {
		super(ServiceRegistryEntry.class);
	}

	private static final Logger LOGGER = Logger
			.getLogger(ServiceRegistrationSearchResource.class.getName());
	private Map<String, String> beanPropertiesMap = new HashMap<String, String>();

	@PostConstruct
	private void setupSearchResource() {
		LOGGER.log(Level.INFO,
				"ServiceRegistrationSearchResource building bean property map");
		classGraphCrawl(ServiceRegistryEntry.class, null, beanPropertiesMap);
		classGraphCrawl(ServiceInstance.class, null, beanPropertiesMap);
		classGraphCrawl(Binding.class, null, beanPropertiesMap);
		classGraphCrawl(TechnicalServiceDescription.class, null,
				beanPropertiesMap);
		classGraphCrawl(Dependency.class, null, beanPropertiesMap);
		classGraphCrawl(UserFeedback.class, null, beanPropertiesMap);
	}

        // TODO: Would make more sense to do searching in mongo...
        @GET
        @Produces("application/json")        
	@Path("/listbytitle/{servicetitle}")
	@PreAuthorize("@serviceRegistrationSearchAuth.canSearch(authentication)")
	public Response getServicesByTitle(@PathParam("servicetitle") String serviceTitle) {
            Response response;

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = null;
            Agent agent = null;

            if (auth != null) {
                try {
                    agent = (Agent) auth.getDetails();
                    userId = agent.getId();
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error getting userId", e);
                }
            }
            
            try {
                MongoDBServiceRegistryDAO serviceRegistryDAO = new MongoDBServiceRegistryDAO();

                List<ServiceRegistryEntry> serviceRegistryEntries = serviceRegistryDAO.getAll();
                List<ServiceRegistryEntry> servicesWithMatchingTitle = new ArrayList<ServiceRegistryEntry>();

                LOGGER.log(Level.INFO, "Search title: " + serviceTitle);
                
                for (ServiceRegistryEntry serviceRegistryEntry : serviceRegistryEntries) {
                    if (serviceRegistryEntry.getServiceDescription().getServiceDescriptionTitle().equals(serviceTitle) 
                            && isUserAuthorizedToAccessService(userId, serviceRegistryEntry)) {
                        LOGGER.log(Level.INFO, "Match found!");
                        servicesWithMatchingTitle.add(serviceRegistryEntry);
                    }
                }

                response = Response.ok(servicesWithMatchingTitle).build();
            }
            catch (Exception e) {
                if (e instanceof DAONotFoundFault) {
                    ServiceRegistrationDoesNotExistException ex = new ServiceRegistrationDoesNotExistException();
                    ex.setExceptionReason(e.getMessage());
                    ex.setExceptionCode("5014");
                    response = this.convertExceptionToResponse(ex, e);
                } 
                else {
                    ServiceRegistrationException ex = new ServiceRegistrationException();
                    ex.setExceptionReason(e.getMessage());
                    ex.setExceptionCode("5015");
                    response = this.convertExceptionToResponse(ex, e);
                }
            }

            return response;
	}
        
	private static void classGraphCrawl(Class classy, String rootClassName,
			Map<String, String> beanPropertiesMap) {
		if (rootClassName == null) {
			rootClassName = new String();
		}
		for (Method method : classy.getMethods()) {
			if ((method.getName().startsWith("get") || method.getName()
					.startsWith("is"))
					&& (method.getReturnType().getName()
							.startsWith("fi.vtt.dsp")
							|| method.getReturnType().getSimpleName()
									.equals("List")
							|| method.getReturnType().getSimpleName()
									.equals("String")
							|| method.getReturnType().getSimpleName()
									.equals("int")
							|| method.getReturnType().getSimpleName()
									.equals("long") || method.getReturnType()
							.getSimpleName().equals("boolean"))) {
				if (method.getReturnType().getSimpleName().equals("String")
						|| method.getReturnType().getSimpleName().equals("int")
						|| method.getReturnType().getSimpleName()
								.equals("long")
						|| method.getReturnType().getSimpleName()
								.equals("boolean")) {
					String paramName = null;
					if (method.getName().startsWith("get")) {
						paramName = method.getName().substring(3,
								(method.getName().length()));
					}
					if (method.getName().startsWith("is")) {
						paramName = method.getName().substring(2,
								(method.getName().length()));
					}
					String prefix = new String();
					if (!rootClassName.isEmpty()) {
						prefix = rootClassName + ".";
					}
					if (beanPropertiesMap.containsKey(Introspector
							.decapitalize(paramName))) {
						String[] alternativePrefixParts = Introspector
								.decapitalize(prefix).split("[.]");
						beanPropertiesMap
								.put(alternativePrefixParts[alternativePrefixParts.length - 1]
										+ "."
										+ Introspector.decapitalize(paramName),
										Introspector.decapitalize(prefix)
												+ Introspector
														.decapitalize(paramName));
					} else {
						beanPropertiesMap.put(
								Introspector.decapitalize(paramName),
								Introspector.decapitalize(prefix)
										+ Introspector.decapitalize(paramName));
					}
				}
				if (method.getReturnType().getName().startsWith("fi.vtt.dsp")) {
					String prefix = new String();
					if (!rootClassName.isEmpty()) {
						prefix = rootClassName + ".";
					}
					classGraphCrawl(
							method.getReturnType(),
							Introspector.decapitalize(prefix)
									+ Introspector.decapitalize(method
											.getName()
											.substring(3,
													(method.getName().length()))),
							beanPropertiesMap);
				}				

			}
		}
	}

	@GET
	@Path("/map")
	@Produces(MediaType.TEXT_PLAIN)
	public Response showBeanPropertiesMap() {
		if (this.beanPropertiesMap == null) {
			this.beanPropertiesMap = new HashMap<String, String>();
			this.setupSearchResource();
		}
		String retString = new String();
		for (String key : beanPropertiesMap.keySet()) {
			retString = new java.lang.StringBuilder(retString).append(key)
					.append("=").append(beanPropertiesMap.get(key))
					.append("\n").toString();
		}
		return Response.ok(retString).build();
	}

	@GET
	@Produces("application/json")
	@Path("/bindings")
	public Response searchBindings(@Context UriInfo uriInfo,
			@Context SearchContext searchContext) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;
		try {
			MultivaluedMap<String, String> queryParams = uriInfo
					.getQueryParameters();
			List<ServiceRegistryEntry> sRegEntries = this.readListOfItems(null,
					null, queryParams.entrySet(), uriInfo);
			List<Binding> bindingList = new ArrayList<Binding>();

			for (ServiceRegistryEntry sRegEntry : sRegEntries) {
				for (ServiceInstance sI : sRegEntry.getServiceInstance()) {
					if (sI.getServiceAccessEndPoint() != null) {
						bindingList.addAll(sI.getServiceAccessEndPoint()
								.getBinding());
					}
				}
			}

			resp = Response.ok(
					bindingList.toArray(new Binding[bindingList.size()]))
					.build();
		} catch (Exception e) {
			ServiceDiscoveryException ex = new ServiceDiscoveryException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("9002");
			resp = this.convertExceptionToResponse(ex, e);
		}
		return resp;
	}

	@GET
	@Produces("application/json")
	@PreAuthorize("@serviceRegistrationSearchAuth.canSearch(authentication)")
	public Response searchRegistrations(@Context UriInfo uriInfo,
			@Context SearchContext searchContext) {
		// TODO: HATEOAS linking, see ItemCollectionResource
		Response resp = null;

		Authentication auth = SecurityContextHolder.getContext()
				.getAuthentication();
		String userId = null;
		Agent agent = null;

		if (auth != null) {
			try {
				agent = (Agent) (Object) auth.getDetails();
				userId = agent.getId();
			} catch (Exception e) {
			}
		}

		try {
			List<ServiceRegistryEntry> resultSet = new ArrayList<ServiceRegistryEntry>();
			MultivaluedMap<String, String> queryParams = uriInfo
					.getQueryParameters();
			List<ServiceRegistryEntry> sRegEntry = null;
                                             
			if (userId != null && agent != null
					&& agent.getType() == AgentType.USER) {
				sRegEntry = this.readListOfItems(null, null,
						queryParams.entrySet(), uriInfo, userId);
			} else {
				sRegEntry = this.readListOfItems(null, null,
						queryParams.entrySet(), uriInfo);
			}

			beanPropertiesMap.put("sid", "serviceId");
			SearchCondition<ServiceRegistryEntry> conditionSRE = searchContext
					.getCondition(ServiceRegistryEntry.class, beanPropertiesMap);

			if (conditionSRE != null) {
				resultSet = conditionSRE.findAll(sRegEntry);
				LOGGER.log(Level.INFO, "conditionSRE search");
			} else {
				LOGGER.log(Level.INFO, "Going through other stuff");
				// Technical description
				SearchCondition<TechnicalServiceDescription> conditionTSC = searchContext
						.getCondition(TechnicalServiceDescription.class);
				if (conditionTSC != null) {
					for (ServiceRegistryEntry sRegE : sRegEntry) {
						List<TechnicalServiceDescription> tList = conditionTSC
								.findAll(sRegE.getServiceDescription()
										.getTechnicalServiceDescription());
						if (!tList.isEmpty()) {
							resultSet.add(sRegE);
						}
					}
				}

				// Dependency
				SearchCondition<Dependency> conditionDEP = searchContext
						.getCondition(Dependency.class);
				if (conditionDEP != null) {
					for (ServiceRegistryEntry sRegE : sRegEntry) {
						List<Dependency> dList = conditionDEP.findAll(sRegE
								.getServiceDescription().getDependency());
						if (!dList.isEmpty()) {
							resultSet.add(sRegE);
						}
					}
				}

				// UserFeedback
				SearchCondition<UserFeedback> conditionUFB = searchContext
						.getCondition(UserFeedback.class);
				if (conditionUFB != null) {
					for (ServiceRegistryEntry sRegE : sRegEntry) {
						List<UserFeedback> uList = conditionUFB.findAll(sRegE
								.getServiceDescription().getUserFeedback());
						if (!uList.isEmpty()) {
							resultSet.add(sRegE);
						}
					}
				}

				// Search in Service instances
				SearchCondition<ServiceInstance> conditionSI = searchContext
						.getCondition(ServiceInstance.class);
				if (conditionSI != null) {
					for (ServiceRegistryEntry sRegE : sRegEntry) {
						List<ServiceInstance> sIList = conditionSI
								.findAll(sRegE.getServiceInstance());
						if (!sIList.isEmpty()) {
							resultSet.add(sRegE);
						}
					}
				} else {
					LOGGER.log(Level.INFO, "bindings search?");
					// Bindings
					SearchCondition<Binding> conditionSIB = searchContext
							.getCondition(Binding.class);
					if (conditionSIB != null) {
						for (ServiceRegistryEntry sRegE : sRegEntry) {
							for (ServiceInstance sI : sRegE
									.getServiceInstance()) {
								List<Binding> bList = conditionSIB.findAll(sI
										.getServiceAccessEndPoint()
										.getBinding());
								if (!bList.isEmpty()) {
									resultSet.add(sRegE);
								}
							}
						}
					}

					// Availability
					SearchCondition<ServiceInstance> conditionSIA = searchContext
							.getCondition(ServiceInstance.class,
									beanPropertiesMap);
					if (conditionSIA != null) {
						for (ServiceRegistryEntry sRegE : sRegEntry) {
							for (ServiceInstance sI : sRegE
									.getServiceInstance()) {
								List<ServiceInstance> matchingAvailability = conditionSIA
										.findAll(sRegE.getServiceInstance());
								if (!matchingAvailability.isEmpty()) {
									resultSet.add(sRegE);
								}
							}
						}
					}
				}
			}
			LOGGER.log(Level.FINE,
					"Search condition " + searchContext.getSearchExpression()
							+ " resulted a set of " + resultSet.size());
			resp = Response
					.ok(resultSet.toArray(new ServiceRegistryEntry[resultSet
							.size()])).build();
		} catch (Exception e) {
			ServiceDiscoveryException ex = new ServiceDiscoveryException();
			ex.setExceptionReason(e.getMessage());
			ex.setExceptionCode("9003");
			resp = this.convertExceptionToResponse(ex, e);
                        LOGGER.log(Level.SEVERE, "Error while searching ", e);
		}
		return resp;
	}

    public boolean isUserAuthorizedToAccessService(String userId, ServiceRegistryEntry service) {
        
        if (StringUtils.isEmpty(userId)) {
            if (service.getServiceDescription().getOwnerGroup().equals("public")) {
                return true;
            }
            else {
                return false;
            }
        }
        
		if (service.getServiceDescription().getCreatedByUserId().equals(userId)) {
			LOGGER.log(Level.INFO,
					"Service is private, user is owner, returning true");
			return true;
		}

		if (service.getServiceDescription().getOwnerGroup().equals("public")) {
			LOGGER.log(Level.INFO, "Service is public, returning true");
			return true;
		} else if (service.getServiceDescription().getOwnerGroup()
				.equals("private")) {
			if (userId == null) {
				LOGGER.log(Level.INFO, "Private, userId null, returning false");
				return false;
			}
		} else if (service.getServiceDescription().getOwnerGroup()
				.equals("group")) {
			LOGGER.log(Level.INFO, "Service is group-owned");
			try {
				MongoDBUserGroupDAO userGroupDAO = new MongoDBUserGroupDAO();
				List<UserGroup> userGroups = null;

				userGroups = userGroupDAO.getAllUserGroupsForUser(userId);

				for (UserGroup userGroup : userGroups) {
					for (String serviceId : userGroup
							.getServiceRegistryEntryId()) {
						if (service.getServiceId().equals(serviceId)) {
							LOGGER.log(Level.INFO,
									"Service is group-owned, user if in group, returning true");
							return true;
						}
					}
				}

			} catch (Exception e) {
				LOGGER.log(Level.SEVERE,
						"Error checking access right for canGet", e);
				return false;
			}
		}

		LOGGER.log(Level.INFO,
				"User has no right to see the group, returning false");
		return false;
	}
}
