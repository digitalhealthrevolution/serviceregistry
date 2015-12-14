package fi.vtt.dsp.service.serviceregistry;

import fi.vtt.dsp.service.serviceregistry.common.*;
import fi.vtt.dsp.service.serviceregistry.common.description.AvailabilityDeclaration;
import fi.vtt.dsp.service.serviceregistry.common.description.Dependency;
import fi.vtt.dsp.service.serviceregistry.common.description.HumanReadableDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.ServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.TechnicalServiceDescription;
import fi.vtt.dsp.service.serviceregistry.common.description.UserFeedback;
import fi.vtt.dsp.service.serviceregistry.common.instance.Availability;
import fi.vtt.dsp.service.serviceregistry.common.instance.AvailabilityRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.BindingRequestEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceParameter;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOUtils;
import fi.vtt.dsp.serviceframework.common.Binding;
import fi.vtt.dsp.serviceframework.common.ServiceAvailability;
import java.util.Random;

public class TestData {
	public static Random random = new Random();

	public static final String OVERLONG_RANDOM_STRING = "4304r5f8nsobv545o8nwekljrnvlaw4n598nvnlkndlnfkjnvlawk4n5ovnawn4lk5jvnalw45v"
			+ "4304r5f8nsobv545o8nwekljrnvlaw4n598nvnlkndlnfkjnvlawk4n5ovnawn4lk5jvnalw45v"
			+ "4304r5f8nsobv545o8nwekljrnvlaw4n598nvnlkndlnfkjnvlawk4n5ovnawn4lk5jvnalw45v"
			+ "4304r5f8nsobv545o8nwekljrnvlaw4n598nvnlkndlnfkjnvlawk4n5ovnawn4lk5jvnalw45v";

	private final static String TEST_BASE_URL = "http://localhost:8080";

	private TestData() {
	}

	public static ServiceRegistryEntry getServiceTestEntry() {
		ObjectFactory of = new ObjectFactory();
		ServiceRegistryEntry sRegEntry = of.createServiceRegistryEntry();

		ServiceDescription sDesc = new ServiceDescription();

		sDesc.setMaturity("test");
		sDesc.setServiceDescriptionTitle("Test service for testing functionalities of the Digital Service Registry including service registry");
		sDesc.setServiceDescriptionVersion("1.0");
		sDesc.setServiceIconURI("http://does.not.exist");
		sDesc.setServiceProviderId("VTT-000000000001");
		sDesc.getKeywords().add("test");
		sDesc.getKeywords().add("dsp");
		sDesc.getKeywords().add("registry");
		sDesc.getKeywords().add("service");
		sDesc.setOwnerGroup("public");

		AvailabilityDeclaration avDec = new AvailabilityDeclaration();

		ServiceAvailability declaredAvail = new ServiceAvailability();
		declaredAvail.setAverageExecutionTime(0);
		declaredAvail.setAverageGoodput(1);
		declaredAvail.setAverageLatency(1);
		declaredAvail.setMaxResponseTime(1);

		avDec.setDeclaredAvailability(declaredAvail);
		sDesc.setAvailabilityDeclaration(avDec);

		HumanReadableDescription huuman = new HumanReadableDescription();
		huuman.setHumanReadableDescription("This is a testing service for testing purposes. This service is testing features of the Digital Service Platform including the digital service registry and does not provide any value");
		huuman.setHumanReadableDescriptionURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/humandescription");
		sDesc.setHumanReadableDescription(huuman);

		TechnicalServiceDescription techDesc1 = new TechnicalServiceDescription();
		techDesc1.setTechnicalDescriptionURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/?_wadl");
		techDesc1.setTechnicalServiceAccessProtocol("REST");
		techDesc1.getImplementedByServiceInstanceId().add("3453535");

		sDesc.getTechnicalServiceDescription().add(techDesc1);

		sRegEntry.setServiceDescription(sDesc);

		ServiceInstance sInst = new ServiceInstance();
		sInst.setHostingEntity("VTT-ICARE project");
		sInst.setServiceInstanceVersion("1.0-SNAPSHOT");

		AvailabilityRequestEndPoint avaReqEP = new AvailabilityRequestEndPoint();
		avaReqEP.setAvailabilityRequestURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/platform/availability");
		sInst.setAvailabilityRequestEndPoint(avaReqEP);

		BindingRequestEndPoint bindReqEP = new BindingRequestEndPoint();
		bindReqEP.setBindingRequestURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/platform/bindings");
		sInst.setBindingRequestEndPoint(bindReqEP);

		fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint sAEP = new fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint();
		sAEP.setServiceAccessURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/hello/echo");

		sInst.setServiceAccessEndPoint(sAEP);

		ServiceAvailability selfRepAva = new ServiceAvailability();
		selfRepAva.setAverageExecutionTime(0);
		selfRepAva.setAverageGoodput(0);
		selfRepAva.setAverageLatency(0);
		selfRepAva.setMaxResponseTime(0);

		sRegEntry.getServiceInstance().add(sInst);
		return sRegEntry;
	}

	public static ServiceRegistryEntry getSample1RegEntry() {
		int averageGoodput = 100000;
		int averageLatency = 2;
		int maxResponseTime = 2;

		fi.vtt.dsp.service.serviceregistry.common.ObjectFactory of = new fi.vtt.dsp.service.serviceregistry.common.ObjectFactory();
		ServiceRegistryEntry sRegEntry = of.createServiceRegistryEntry();

		ServiceDescription sDesc = new ServiceDescription();

		sDesc.setMaturity("ripe");
		sDesc.setServiceDescriptionTitle("Über cool super-service");
		sDesc.setServiceDescriptionVersion("0.00000001 Omega");
		sDesc.setServiceIconURI("http://does.not.exist");
		sDesc.setServiceProviderId("000000000001");
		sDesc.getKeywords().add("cool");
		sDesc.getKeywords().add("selfmade");
		sDesc.getKeywords().add("testing");
		sDesc.getKeywords().add("whocares");
		sDesc.setOwnerGroup("public");

		AvailabilityDeclaration avDec = new AvailabilityDeclaration();

		ServiceAvailability declaredAvail = new ServiceAvailability();
		declaredAvail.setAverageExecutionTime(0);
		declaredAvail.setAverageGoodput(averageGoodput);
		declaredAvail.setAverageLatency(averageLatency);
		declaredAvail.setMaxResponseTime(maxResponseTime);

		avDec.setDeclaredAvailability(declaredAvail);
		sDesc.setAvailabilityDeclaration(avDec);

		HumanReadableDescription huuman = new HumanReadableDescription();
		huuman.setHumanReadableDescription("LOREM IPSUM SIG TRANSIT GLORIA MUNDI EX NIHILO NIHIL FIT AD ASTRA Ja höpölöpö sekä kissa vieköön LOREM IPSUM SIG TRANSIT GLORIA MUNDI EX NIHILO NIHIL FIT AD ASTRA Ja höpölöpö sekä kissa vieköön");
		huuman.setHumanReadableDescriptionURI("http://super.duper.li");
		sDesc.setHumanReadableDescription(huuman);

		TechnicalServiceDescription techDesc1 = new TechnicalServiceDescription();
		techDesc1.setTechnicalDescriptionURI("http://here.and.there");
		techDesc1.setTechnicalServiceAccessProtocol("REST");
		techDesc1.getImplementedByServiceInstanceId().add("3453535");

		TechnicalServiceDescription techDesc2 = new TechnicalServiceDescription();
		techDesc1.setTechnicalDescriptionURI("http://here2.and2.there2");
		techDesc1.setTechnicalServiceAccessProtocol("SOAP");
		techDesc1.getImplementedByServiceInstanceId().add("3453535");

		sDesc.getTechnicalServiceDescription().add(techDesc1);
		sDesc.getTechnicalServiceDescription().add(techDesc2);

		Dependency dep1 = new Dependency();
		dep1.setDependsOnServiceId("353535335");

		Dependency dep2 = new Dependency();
		dep2.setDependencyId("");
		dep2.setDependsOnServiceId("99999999");

		sDesc.getDependency().add(dep1);
		sDesc.getDependency().add(dep2);

		sRegEntry.setServiceDescription(sDesc);

		ServiceInstance sInst = new ServiceInstance();
		sInst.setHostingEntity("Mie Ite");
		sInst.setServiceInstanceVersion("Joku hehkee versio");

		AvailabilityRequestEndPoint avaReqEP = new AvailabilityRequestEndPoint();
		avaReqEP.setAvailabilityRequestURI("http://diipa.daapa.doop");
		sInst.setAvailabilityRequestEndPoint(avaReqEP);

		BindingRequestEndPoint bindReqEP = new BindingRequestEndPoint();
		bindReqEP.setBindingRequestURI("http://darkside.moon.org");
		sInst.setBindingRequestEndPoint(bindReqEP);

		fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint sAEP = new fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint();
		sAEP.setServiceAccessURI("http://serve.yourself.org");

		Binding bind1 = new Binding();
		bind1.setBindingId("active");
		bind1.setBoundByServiceId("00");

		Binding bind2 = new Binding();
		bind2.setBindingId("active");
		bind2.setBoundByServiceId("01");

		sAEP.getBinding().add(bind1);
		sAEP.getBinding().add(bind2);

		Availability serviceInstanceAvailability = new Availability();
		ServiceAvailability selfRepAva = new ServiceAvailability();
		selfRepAva.setAverageExecutionTime(0);
		selfRepAva.setAverageGoodput(0);
		selfRepAva.setAverageLatency(0);
		selfRepAva.setMaxResponseTime(0);

		serviceInstanceAvailability.setSelfReportedAvailability(selfRepAva);
		sAEP.setAvailability(serviceInstanceAvailability);
		sInst.setServiceAccessEndPoint(sAEP);

		AvailabilityRequestEndPoint aReqEP = new AvailabilityRequestEndPoint();
		aReqEP.setAvailabilityRequestURI("http://anybody.at.home");
		sInst.setAvailabilityRequestEndPoint(aReqEP);

		BindingRequestEndPoint bReqEP = new BindingRequestEndPoint();
		bReqEP.setBindingRequestURI("http://anybody.boundto.me");
		sInst.setBindingRequestEndPoint(bReqEP);

		sRegEntry.getServiceInstance().add(sInst);

		return sRegEntry;
	}

	public static ServiceRegistryEntry getSample2RegEntry() {
		int averageGoodput = 100000;
		int averageLatency = 2;
		int maxResponseTime = 2;

		fi.vtt.dsp.service.serviceregistry.common.ObjectFactory of = new fi.vtt.dsp.service.serviceregistry.common.ObjectFactory();
		ServiceRegistryEntry sRegEntry = of.createServiceRegistryEntry();

		ServiceDescription sDesc = new ServiceDescription();

		sDesc.setMaturity("ripe");
		sDesc.setServiceDescriptionTitle("The best of all services");
		sDesc.setServiceDescriptionVersion("0.00000002 Beta");
		sDesc.setServiceIconURI("http://does.not.exist");
		sDesc.setServiceProviderId("000000000002");
		sDesc.setOwnerGroup("public");

		AvailabilityDeclaration avDec = new AvailabilityDeclaration();

		ServiceAvailability declaredAvail = new ServiceAvailability();
		declaredAvail.setAverageExecutionTime(0);
		declaredAvail.setAverageGoodput(averageGoodput);
		declaredAvail.setAverageLatency(averageLatency);
		declaredAvail.setMaxResponseTime(maxResponseTime);

		avDec.setDeclaredAvailability(declaredAvail);
		sDesc.setAvailabilityDeclaration(avDec);

		HumanReadableDescription huuman = new HumanReadableDescription();
		huuman.setHumanReadableDescription("LOREM IPSUM SIG TRANSIT GLORIA MUNDI EX NIHILO NIHIL FIT AD ASTRA Ja höpölöpö sekä kissa vieköön LOREM IPSUM SIG TRANSIT GLORIA MUNDI EX NIHILO NIHIL FIT AD ASTRA Ja höpölöpö sekä kissa vieköön");
		huuman.setHumanReadableDescription("http://super.duper.li");
		sDesc.setHumanReadableDescription(huuman);

		sRegEntry.setServiceDescription(sDesc);

		ServiceInstance sInst = new ServiceInstance();
		sInst.setHostingEntity("Mie Ite");
		sInst.setServiceInstanceId("001");
		sInst.setServiceInstanceId("Joku hehkee versio");

		AvailabilityRequestEndPoint avaReqEP = new AvailabilityRequestEndPoint();
		avaReqEP.setAvailabilityRequestURI("http://diipa.daapa.doop");
		sInst.setAvailabilityRequestEndPoint(avaReqEP);

		BindingRequestEndPoint bindReqEP = new BindingRequestEndPoint();
		bindReqEP.setBindingRequestURI("http://darkside.moon.org");
		sInst.setBindingRequestEndPoint(bindReqEP);

		fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint sAEP = new fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint();
		sAEP.setServiceAccessURI("http://serve.yourself.org");
		fi.vtt.dsp.service.serviceregistry.common.instance.Availability serviceInstanceAvailability = new fi.vtt.dsp.service.serviceregistry.common.instance.Availability();
		ServiceAvailability selfRepAva = new ServiceAvailability();
		selfRepAva.setAverageExecutionTime(0);
		selfRepAva.setAverageGoodput(0);
		selfRepAva.setAverageLatency(0);
		selfRepAva.setMaxResponseTime(0);
		serviceInstanceAvailability.setSelfReportedAvailability(selfRepAva);
		sInst.setServiceAccessEndPoint(sAEP);

		sAEP.setAvailability(serviceInstanceAvailability);
		sInst.setServiceAccessEndPoint(sAEP);

		sRegEntry.getServiceInstance().add(sInst);

		ServiceInstance sInst2 = new ServiceInstance();
		sInst2.setHostingEntity("Mie Ite");
		sInst2.setServiceInstanceId("001");
		sInst2.setServiceInstanceId("Joku hehkee versio");

		AvailabilityRequestEndPoint avaReqEP2 = new AvailabilityRequestEndPoint();
		avaReqEP2.setAvailabilityRequestURI("http://diipa.daapa.doop");
		sInst2.setAvailabilityRequestEndPoint(avaReqEP2);

		BindingRequestEndPoint bindReqEP2 = new BindingRequestEndPoint();
		bindReqEP2.setBindingRequestURI("http://darkside.moon.org");
		sInst2.setBindingRequestEndPoint(bindReqEP);

		fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint sAEP2 = new fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint();
		sAEP2.setServiceAccessURI("http://serve.yourself.org");
		fi.vtt.dsp.service.serviceregistry.common.instance.Availability serviceInstanceAvailability2 = new fi.vtt.dsp.service.serviceregistry.common.instance.Availability();
		ServiceAvailability selfRepAva2 = new ServiceAvailability();
		selfRepAva2.setAverageExecutionTime(0);
		selfRepAva2.setAverageGoodput(0);
		selfRepAva2.setAverageLatency(0);
		selfRepAva2.setMaxResponseTime(0);
		serviceInstanceAvailability2.setSelfReportedAvailability(selfRepAva2);
		sInst2.setServiceAccessEndPoint(sAEP2);

		sAEP2.setAvailability(serviceInstanceAvailability2);
		sInst2.setServiceAccessEndPoint(sAEP2);

		sRegEntry.getServiceInstance().add(sInst2);

		return sRegEntry;
	}

	public static UserProfile getUserProfile1() {
		UserProfile up = new UserProfile();
		up.setEmail("dude1.test@test.com");
		up.setFirstName("Dude");
		up.setScreenName("");
		up.setMiddleNames("Dudeson");
		up.setLastName("Test");
		up.setCountryCode("FI");
		up.setPreferredLanguage("finnish");
		up.setOrganization("VTT");
		up.setOrganizationalUnit("KIPS");
		up.setTelephone("+358401234123");
		return up;
	}

	public static UserProfile getUserProfile2() {
		UserProfile up = new UserProfile();
		up.setEmail("dude2.test@test.com");
		up.setFirstName("Dude");
		up.setScreenName("");
		up.setMiddleNames("Dudeson");
		up.setLastName("Test");
		up.setCountryCode("FI");
		up.setPreferredLanguage("finnish");
		up.setOrganization("VTT");
		up.setOrganizationalUnit("KIPS");
		up.setTelephone("+358401234123");
		return up;
	}

	public static UserProfile getUserProfile3() {
		UserProfile up = new UserProfile();
		up.setEmail("dude3.test@test.com");
		up.setFirstName("Dude");
		up.setScreenName("");
		up.setMiddleNames("Danger");
		up.setLastName("Test");
		up.setCountryCode("FI");
		up.setPreferredLanguage("Finglish");
		up.setOrganization("VTT");
		up.setOrganizationalUnit("KIPS");
		up.setTelephone("+358-EAT-P00P");
		return up;
	}

	public static ServiceRegistryEntry getEntryForDSPTestService() {
		fi.vtt.dsp.service.serviceregistry.common.ObjectFactory of = new fi.vtt.dsp.service.serviceregistry.common.ObjectFactory();
		ServiceRegistryEntry sRegEntry = of.createServiceRegistryEntry();

		ServiceDescription sDesc = new ServiceDescription();

		sDesc.setMaturity("ripe");
		sDesc.setServiceDescriptionTitle("Test service for testing functionalities of the Digital Service Registry including service registry");
		sDesc.setServiceDescriptionVersion("1.0");
		sDesc.setServiceIconURI("http://t3.gstatic.com/images?q=tbn:ANd9GcT0XfMvymAQKUVmC_iUmHPVoAOY613IPsGXDNFEBU4uK08ZXqOhIrRiz48");
		sDesc.setOwnerGroup("public");

		AvailabilityDeclaration avDec = new AvailabilityDeclaration();

		ServiceAvailability declaredAvail = new ServiceAvailability();
		declaredAvail.setAverageExecutionTime(0);
		declaredAvail.setAverageGoodput(1);
		declaredAvail.setAverageLatency(1);
		declaredAvail.setMaxResponseTime(1);

		avDec.setDeclaredAvailability(declaredAvail);
		sDesc.setAvailabilityDeclaration(avDec);

		TechnicalServiceDescription techDesc1 = new TechnicalServiceDescription();
		techDesc1.setTechnicalDescriptionURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/?_wadl");
		techDesc1.setTechnicalServiceAccessProtocol("REST");
		techDesc1.getImplementedByServiceInstanceId().add("13565789");

		sDesc.getTechnicalServiceDescription().add(techDesc1);

		HumanReadableDescription huDesc = new HumanReadableDescription();
		huDesc.setHumanReadableDescriptionURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/humandescription");
		huDesc.setHumanReadableDescription("This super awsome service provides unbelievable mindblowing ECHO service you have never seen before");

		sDesc.setHumanReadableDescription(huDesc);

		sRegEntry.setServiceDescription(sDesc);

		ServiceInstance sInst = new ServiceInstance();
		sInst.setHostingEntity("VTT-ICARE project");
		sInst.setServiceInstanceVersion("1.0-SNAPSHOT");
		sInst.setServiceInstanceId("13565789");

		AvailabilityRequestEndPoint avaReqEP = new AvailabilityRequestEndPoint();
		avaReqEP.setAvailabilityRequestURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/platform/availability");                
		sInst.setAvailabilityRequestEndPoint(avaReqEP);

		BindingRequestEndPoint bindReqEP = new BindingRequestEndPoint();
		bindReqEP.setBindingRequestURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/platform/bindings");
		sInst.setBindingRequestEndPoint(bindReqEP);

		fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint sAEP = new fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint();
		sAEP.setServiceAccessURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/hello/echo");
                Availability availability = new Availability();
                ServiceAvailability serviceAvailability = new ServiceAvailability();
                serviceAvailability.setAverageExecutionTime(100);
                serviceAvailability.setAverageGoodput(100);
                serviceAvailability.setAverageLatency(100);
                serviceAvailability.setMaxResponseTime(100);
                serviceAvailability.setServiceActive(true);
                serviceAvailability.setUptimeHours(3);
                availability.setInspectedAvailability(serviceAvailability);
                sAEP.setAvailability(availability);
		sInst.setServiceAccessEndPoint(sAEP);

		// 13565799
		ServiceInstance sInst2 = new ServiceInstance();
		sInst2.setHostingEntity("VTT-ICARE project");
		sInst2.setServiceInstanceVersion("1.0-SNAPSHOT");
		sInst2.setServiceInstanceId("13565799");

		AvailabilityRequestEndPoint avaReqEP2 = new AvailabilityRequestEndPoint();
		avaReqEP2
				.setAvailabilityRequestURI(TEST_BASE_URL
						+ "/DSPTestServiceQueried-0.0.1-SNAPSHOT/platform/availability");
		sInst2.setAvailabilityRequestEndPoint(avaReqEP2);

		BindingRequestEndPoint bindReqEP2 = new BindingRequestEndPoint();
		bindReqEP2.setBindingRequestURI(TEST_BASE_URL
				+ "/DSPTestServiceQueried-0.0.1-SNAPSHOT/platform/bindings");
		sInst2.setBindingRequestEndPoint(bindReqEP2);

		fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint sAEP2 = new fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint();
		sAEP2.setServiceAccessURI(TEST_BASE_URL
				+ "/DSPTestServiceQueried-0.0.1-SNAPSHOT/hello/echo");

		Binding boundBy13565789 = new Binding();
		boundBy13565789.setBoundByServiceId("5360a31745ce43e22002eb27");
		boundBy13565789.setBoundByServiceInstanceId("13565799");
		boundBy13565789.setStatusActive(true);

		sAEP2.getBinding().add(boundBy13565789);
		sInst2.setServiceAccessEndPoint(sAEP2);

		sRegEntry.getServiceInstance().add(sInst);
		sRegEntry.getServiceInstance().add(sInst2);
		return sRegEntry;
	}

	public static ServiceRegistryEntry getEntryForDSPTestServiceQueried() {
		fi.vtt.dsp.service.serviceregistry.common.ObjectFactory of = new fi.vtt.dsp.service.serviceregistry.common.ObjectFactory();
		ServiceRegistryEntry sRegEntry = of.createServiceRegistryEntry();

		ServiceDescription sDesc = new ServiceDescription();

		sDesc.setMaturity("ripe");
		sDesc.setServiceDescriptionTitle("Test service for testing functionalities of the Digital Service Registry including service registry");
		sDesc.setServiceDescriptionVersion("1.0");
		sDesc.setServiceIconURI("http://t3.gstatic.com/images?q=tbn:ANd9GcT0XfMvymAQKUVmC_iUmHPVoAOY613IPsGXDNFEBU4uK08ZXqOhIrRiz48");
		sDesc.setOwnerGroup("public");

		TechnicalServiceDescription techDesc1 = new TechnicalServiceDescription();
		techDesc1.setTechnicalDescriptionURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/?_wad");
		techDesc1.setTechnicalServiceAccessProtocol("REST");
		techDesc1.getImplementedByServiceInstanceId().add("13565789");

		sDesc.getTechnicalServiceDescription().add(techDesc1);

		sRegEntry.setServiceDescription(sDesc);

		ServiceInstance sInst = new ServiceInstance();
		sInst.setHostingEntity("VTT-ICARE project");
		sInst.setServiceInstanceVersion("1.0-SNAPSHOT");
		sInst.setServiceInstanceId("13565789");

		AvailabilityRequestEndPoint avaReqEP = new AvailabilityRequestEndPoint();
		avaReqEP.setAvailabilityRequestURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/platform/availability");
		sInst.setAvailabilityRequestEndPoint(avaReqEP);

		BindingRequestEndPoint bindReqEP = new BindingRequestEndPoint();
		bindReqEP.setBindingRequestURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/platform/bindings");
		sInst.setBindingRequestEndPoint(bindReqEP);

		fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint sAEP = new fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint();
		sAEP.setServiceAccessURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/hello/echo");
		sInst.setServiceAccessEndPoint(sAEP);
		sRegEntry.getServiceInstance().add(sInst);
		return sRegEntry;
	}

	public static UserProfile getUserProfile() {
		UserProfile up = new UserProfile();
		up.setEmail("test." + System.currentTimeMillis() + random.nextInt()
				+ "@vtt.fi");
		up.setFirstName("devaaja");
		up.setMiddleNames("guru");
		up.setScreenName("mjister");
		up.setLastName("icare");
		up.setCountryCode("FI");
		up.setPreferredLanguage("fi");
		up.setOrganization("VTT");
		up.setOrganizationalUnit("KIPS");
		up.setTelephone("+358443094");
		return up;
	}

	public static ServiceRegistryEntry getServiceRegistryEntry() {

		ServiceRegistryEntry sRegEntry = new ServiceRegistryEntry();

		ServiceDescription sDesc = new ServiceDescription();

		sDesc.setMaturity("test");
		sDesc.setServiceDescriptionTitle("Test service for testing functionalities of the Digital Service Registry including service registry");
		sDesc.setServiceDescriptionVersion("1.0");
		sDesc.setServiceIconURI("http://does.not.exist");
		sDesc.setServiceProviderId("VTT-123456");
		sDesc.getKeywords().add("test");
		sDesc.getKeywords().add("dsp");
		sDesc.getKeywords().add("registry");
		sDesc.getKeywords().add("service");
		sDesc.setOwnerGroup("public");

		sDesc.getDependency().add(getDependency());
		sDesc.setAvailabilityDeclaration(getAvailabilityDeclaration());
		sDesc.setHumanReadableDescription(getHumanReadableDescription());
		sDesc.getTechnicalServiceDescription().add(
				getTechnicalServiceDescription());

		sRegEntry.setServiceDescription(sDesc);

		return sRegEntry;
	}

	public static Dependency getDependency() {
		Dependency d = new Dependency();
		d.setDependsOnServiceId("123456789012345678901234");
		return d;
	}

	public static ServiceAvailability getServiceAvailability() {
		ServiceAvailability declaredAvail = new ServiceAvailability();
		declaredAvail.setAverageExecutionTime(0);
		declaredAvail.setAverageGoodput(1);
		declaredAvail.setAverageLatency(1);
		declaredAvail.setMaxResponseTime(1);
		return declaredAvail;
	}

	public static AvailabilityDeclaration getAvailabilityDeclaration() {
		AvailabilityDeclaration avDec = new AvailabilityDeclaration();
		avDec.setDeclaredAvailability(getServiceAvailability());
		return avDec;
	}

	public static HumanReadableDescription getHumanReadableDescription() {
		HumanReadableDescription huuman = new HumanReadableDescription();
		huuman.setHumanReadableDescription("This is a testing service for testing purposes. This service is testing features of the Digital Service Platform including the digital service registry and does not provide any value");
		huuman.setHumanReadableDescriptionURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/humandescription");
		return huuman;
	}

	public static TechnicalServiceDescription getTechnicalServiceDescription() {
		TechnicalServiceDescription techDesc1 = new TechnicalServiceDescription();
		techDesc1.setTechnicalDescriptionURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/?_wadl");
		techDesc1.setTechnicalServiceAccessProtocol("REST");
		techDesc1.getImplementedByServiceInstanceId().add("3453535");
		techDesc1.setTechnicalDescriptionId(Long.toString(System
				.currentTimeMillis()));
		return techDesc1;
	}

	public static UserFeedback getUserFeedback(UserProfile user) {
		UserFeedback f = new UserFeedback();
		f.setFeedback("feedback");
		f.setProvidedByUserId(user.getUserId());
		f.setUserRating(80);
		return f;
	}

	public static ServiceInstance getServiceInstance() {

		ServiceInstance sInst = new ServiceInstance();
		sInst.setHostingEntity("VTT-ICARE project");
		sInst.setServiceInstanceVersion("1.0-SNAPSHOT");

		sInst.setAvailabilityRequestEndPoint(getAvailabilityRequestEndPoint());
		sInst.setBindingRequestEndPoint(getBindingRequestEndPoint());
		sInst.setServiceAccessEndPoint(getServiceAccessEndPoint());

		return sInst;
	}

	public static AvailabilityRequestEndPoint getAvailabilityRequestEndPoint() {
		AvailabilityRequestEndPoint avaReqEP = new AvailabilityRequestEndPoint();
		avaReqEP.setAvailabilityRequestURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/platform/availability");
		return avaReqEP;
	}

	public static BindingRequestEndPoint getBindingRequestEndPoint() {
		BindingRequestEndPoint bindReqEP = new BindingRequestEndPoint();
		bindReqEP.setBindingRequestURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/platform/bindings");
		return bindReqEP;
	}

	public static ServiceAccessEndPoint getServiceAccessEndPoint() {
		ServiceAccessEndPoint sAEP = new ServiceAccessEndPoint();
		sAEP.setServiceAccessURI(TEST_BASE_URL
				+ "/DSPTestService-0.0.1-SNAPSHOT/hello/echo");
		sAEP.setAvailability(getAvailability());
		return sAEP;
	}

	public static Availability getAvailability() {
		Availability a = new Availability();
		a.setInspectedAvailability(getServiceAvailability());
		a.setSelfReportedAvailability(getServiceAvailability());
		return a;
	}

	public static Binding getBinding(ServiceRegistryEntry boundByService,
			ServiceInstance boundByInstance) {

		String boundByServiceId = boundByService.getServiceId();
		String boundByServiceInstanceId = boundByInstance
				.getServiceInstanceId();
		String requestedByUserId = boundByInstance.getCreatedByUserId();

		if (boundByServiceId == null || boundByServiceInstanceId == null
				|| requestedByUserId == null) {
			throw new IllegalArgumentException();
		}

		Binding b = new Binding();
		b.setBoundByServiceId(boundByServiceId);
		b.setBoundByServiceInstanceId(boundByServiceInstanceId);
		b.setRequestedByUserId(requestedByUserId);
		b.setStatusActive(true);
		b.setStatusAuthorized(false);
		b.setStatusRequested(true);
		b.setStatusPending(true);

		return b;
	}

	public static Binding getBinding(String serviceDescriptionId,
			ServiceInstance boundByInstance) {

		String boundByServiceId = serviceDescriptionId;// boundByService.boundByService.getServiceId();
		String boundByServiceInstanceId = boundByInstance
				.getServiceInstanceId();
		String requestedByUserId = boundByInstance.getCreatedByUserId();

		if (boundByServiceId == null || boundByServiceInstanceId == null
				|| requestedByUserId == null) {
			throw new IllegalArgumentException();
		}

		Binding b = new Binding();
		b.setBoundByServiceId(boundByServiceId);
		b.setBoundByServiceInstanceId(boundByServiceInstanceId);
		b.setRequestedByUserId(requestedByUserId);
		b.setStatusActive(true);
		b.setStatusAuthorized(false);
		b.setStatusRequested(true);
		b.setStatusPending(true);

		return b;
	}

	public static UserProfile createUserProfile() {
		UserProfile up = new UserProfile();
		up.setEmail("test.icare@vtt.fi");
		up.setFirstName("devaaja");
		up.setMiddleNames("guru");
		up.setScreenName("");
		up.setLastName("icare");
		up.setCountryCode("FI");
		up.setPreferredLanguage("fi");
		up.setOrganization("VTT");
		up.setOrganizationalUnit("KIPS");
		up.setTelephone("+358443094");
		// up.setUserId("?");
		return up;
	}

	public static UserProfile createUserProfile1() {
		UserProfile up = createUserProfile();
		up.setEmail("test1.icare@vtt.fi");
		return up;
	}

	public static UserProfile createInvalidUserProfile() {
		UserProfile up = createUserProfile();
		up.setEmail("?!@*~X@%");
		return up;
	}

	public static Dependency createDependency() {
		Dependency dep = new Dependency();
		dep.setDependencyId("depId");
		dep.setDependsOnServiceId("servId");
		return dep;
	}

	public static Dependency createDependency1() {
		Dependency dep = createDependency();
		dep.setDependencyId("depId1");
		return dep;
	}

	public static AvailabilityDeclaration createAvailabilityDeclaration() {
		AvailabilityDeclaration avDec = new AvailabilityDeclaration();
		avDec.setDeclaredAvailability(createServiceAvailability());
		return avDec;
	}

	public static AvailabilityDeclaration createAvailabilityDeclaration1() {
		AvailabilityDeclaration avDec = createAvailabilityDeclaration();
		avDec.setDeclaredAvailability(createServiceAvailability1());
		return avDec;
	}

	public static ServiceDescription createServiceDescription() {
		ServiceDescription sDesc = new ServiceDescription();
		sDesc.setMaturity("ripe");
		sDesc.setServiceDescriptionTitle("Über cool super-service");
		sDesc.setServiceDescriptionVersion("0.00000001 Omega");
		sDesc.setServiceIconURI("http://does.not.exist");
		sDesc.setServiceProviderId("000000000001");
		sDesc.getKeywords().add("cool");
		sDesc.getKeywords().add("selfmade");
		sDesc.getKeywords().add("testing");
		sDesc.getKeywords().add("whocares");
		sDesc.setAvailabilityDeclaration(createAvailabilityDeclaration());
		sDesc.getDependency().add(createDependency());
		sDesc.setOwnerGroup("public");
		return sDesc;
	}

	public static ServiceDescription createServiceDescription1() {
		ServiceDescription sDesc = new ServiceDescription();
		sDesc.setMaturity("raw");
		return sDesc;
	}

	public static ServiceRegistryEntry createServiceRegistryEntry() {
		fi.vtt.dsp.service.serviceregistry.common.ObjectFactory of = new fi.vtt.dsp.service.serviceregistry.common.ObjectFactory();
		ServiceRegistryEntry sRegEntry = of.createServiceRegistryEntry();
		sRegEntry.setServiceDescription(createServiceDescription());

		ServiceInstance sInst = new ServiceInstance();
		sInst.setHostingEntity("Mie Ite");
		sInst.setServiceInstanceVersion("Joku hehkee versio");

		AvailabilityRequestEndPoint avaReqEP = new AvailabilityRequestEndPoint();
		avaReqEP.setAvailabilityRequestURI("http://diipa.daapa.doop");
		sInst.setAvailabilityRequestEndPoint(avaReqEP);

		BindingRequestEndPoint bindReqEP = new BindingRequestEndPoint();
		bindReqEP.setBindingRequestURI("http://darkside.moon.org");
		sInst.setBindingRequestEndPoint(bindReqEP);

		fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint sAEP = new fi.vtt.dsp.service.serviceregistry.common.instance.ServiceAccessEndPoint();
		sAEP.setServiceAccessURI("http://serve.yourself.org");

		Binding bind1 = new Binding();
		bind1.setBindingId("active");
		bind1.setBoundByServiceId("00");

		Binding bind2 = new Binding();
		bind2.setBindingId("active");
		bind2.setBoundByServiceId("01");

		sAEP.getBinding().add(bind1);
		sAEP.getBinding().add(bind2);

		Availability serviceInstanceAvailability = new Availability();
		ServiceAvailability selfRepAva = new ServiceAvailability();
		selfRepAva.setAverageExecutionTime(0);
		selfRepAva.setAverageGoodput(0);
		selfRepAva.setAverageLatency(0);
		selfRepAva.setMaxResponseTime(0);

		serviceInstanceAvailability.setSelfReportedAvailability(selfRepAva);
		sAEP.setAvailability(serviceInstanceAvailability);
		sInst.setServiceAccessEndPoint(sAEP);

		AvailabilityRequestEndPoint aReqEP = new AvailabilityRequestEndPoint();
		aReqEP.setAvailabilityRequestURI("http://anybody.at.home");
		sInst.setAvailabilityRequestEndPoint(aReqEP);

		BindingRequestEndPoint bReqEP = new BindingRequestEndPoint();
		bReqEP.setBindingRequestURI("http://anybody.boundto.me");
		sInst.setBindingRequestEndPoint(bReqEP);

		sRegEntry.getServiceInstance().add(sInst);

		return sRegEntry;
	}

	public static ServiceRegistryEntry createInvalidServiceRegistryEntry1() {
		ServiceRegistryEntry sre = createServiceRegistryEntry();
		sre.getServiceInstance().remove(0);
		sre.getServiceInstance().add(createInvalidServiceInstance1());
		return sre;
	}

	public static TechnicalServiceDescription createTechnicalServiceDescription() {
		TechnicalServiceDescription techDesc1 = new TechnicalServiceDescription();
		techDesc1.setTechnicalDescriptionURI("http://here.and.there");
		techDesc1.setTechnicalServiceAccessProtocol("REST");
		techDesc1.getImplementedByServiceInstanceId().add("3453535");
		return techDesc1;
	}

	public static TechnicalServiceDescription createTechnicalServiceDescription1() {
		TechnicalServiceDescription techDesc1 = createTechnicalServiceDescription();
		techDesc1
				.setTechnicalDescriptionURI("http://here.and.there/and.everywhere");
		techDesc1.setTechnicalServiceAccessProtocol("REST");
		techDesc1.getImplementedByServiceInstanceId().add("3453535");
		return techDesc1;
	}

	public static HumanReadableDescription createHumanReadableDescription() {
		HumanReadableDescription human = new HumanReadableDescription();
		human.setHumanReadableDescription("LOREM IPSUM SIG TRANSIT GLORIA MUNDI EX NIHILO NIHIL FIT AD ASTRA Ja hÃ¶pÃ¶lÃ¶pÃ¶ sekÃ¤ kissa viekÃ¶Ã¶n LOREM IPSUM SIG TRANSIT GLORIA MUNDI EX NIHILO NIHIL FIT AD ASTRA Ja hÃ¶pÃ¶lÃ¶pÃ¶ sekÃ¤ kissa viekÃ¶Ã¶n");
		human.setHumanReadableDescriptionURI("http://super.duper.li");
		return human;
	}

	public static HumanReadableDescription createHumanReadableDescription1() {
		HumanReadableDescription human = createHumanReadableDescription();
		human.setHumanReadableDescription("Human readable description 1.");
		return human;
	}

	public static ServiceAvailability createServiceAvailability() {
		ServiceAvailability sa = new ServiceAvailability();
		sa.setAverageExecutionTime(1);
		sa.setAverageGoodput(1);
		sa.setAverageLatency(1);
		sa.setMaxResponseTime(1);
		sa.setServiceActive(false);
		sa.setTimeStamp(System.currentTimeMillis());
		sa.setUptimeHours(1);
		return sa;
	}

	public static ServiceAvailability createServiceAvailability1() {
		ServiceAvailability sa = createServiceAvailability();
		sa.setAverageExecutionTime(2);
		return sa;
	}

	public static ServiceParameter createServiceParameter() {
		ServiceParameter sp = new ServiceParameter();
		sp.setServiceParameterKey("spKey0");
		sp.setServiceParameterValue("spVal0");
		return sp;
	}

	public static Availability createAvailability() {
		Availability avail = new Availability();
		avail.setInspectedAvailability(createServiceAvailability());
		avail.setSelfReportedAvailability(createServiceAvailability());
		avail.getServiceParameter().add(createServiceParameter());
		return avail;
	}

	public static AvailabilityRequestEndPoint createAvailabilityRequestEndPoint() {
		AvailabilityRequestEndPoint ep = new AvailabilityRequestEndPoint();
		ep.setAvailabilityRequestURI("http://foo.bar");
		return ep;
	}

	public static AvailabilityRequestEndPoint createAvailabilityRequestEndPoint1() {
		AvailabilityRequestEndPoint ep = createAvailabilityRequestEndPoint();
		ep.setAvailabilityRequestURI("http://bar.foo");
		return ep;
	}

	public static BindingRequestEndPoint createBindingRequestEndPoint() {
		BindingRequestEndPoint ep = new BindingRequestEndPoint();
		ep.setBindingRequestURI("http://foo.bar");
		return ep;
	}

	public static BindingRequestEndPoint createBindingRequestEndPoint1() {
		BindingRequestEndPoint ep = createBindingRequestEndPoint();
		ep.setBindingRequestURI("http://bar.foo");
		return ep;
	}

	public static BindingRequestEndPoint createInvalidBindingRequestEndPoint() {
		BindingRequestEndPoint ep = createBindingRequestEndPoint();
		// ep.setBindingRequestURI(OVERLONG_RANDOM_STRING);
		ep.setBindingRequestURI(null);

		return ep;
	}

	public static Binding createBinding() {
		Binding b = new Binding();
		// b.setAuthorizedByUserId(value);
		b.setBindingId("binding0");
		b.setBoundByServiceId("1234");
		b.setBoundByServiceInstanceId("5678");

		b.setModifiedOnDate(DAOUtils.getCurrentDateAsUnixEpoch());

		// b.setRequestedByUserId(value);
		b.setRequestedOnDate(DAOUtils.getCurrentDateAsUnixEpoch());

		b.setStatusActive(false);
		b.setStatusAuthorized(true);
		b.setStatusPending(false);
		b.setStatusRequested(true);
		return b;
	}

	public static ServiceAccessEndPoint createServiceAccessEndPoint() {
		ServiceAccessEndPoint ep = new ServiceAccessEndPoint();
		ep.setAvailability(createAvailability());
		ep.setServiceAccessURI("http://foo.bar");
		ep.getBinding().add(createBinding());
		return ep;
	}

	public static ServiceAccessEndPoint createServiceAccessEndPoint1() {
		ServiceAccessEndPoint ep = createServiceAccessEndPoint();
		ep.setServiceAccessURI("http://bar.foo");
		return ep;
	}

	public static ServiceInstance createServiceInstance() {
		ServiceInstance si = new ServiceInstance();
		si.setAvailabilityRequestEndPoint(createAvailabilityRequestEndPoint());
		si.setBindingRequestEndPoint(createBindingRequestEndPoint());
		si.setCreatedByUserId("1234");

		si.setCreatedOnDate(DAOUtils.getCurrentDateAsUnixEpoch());

		si.setHostingEntity("testHostingEntity");
		si.setModifiedByUserId("5678");

		si.setModifiedOnDate(DAOUtils.getCurrentDateAsUnixEpoch());

		si.setServiceAccessEndPoint(createServiceAccessEndPoint());
		si.setServiceInstanceId("1234");
		si.setServiceInstanceVersion("1.0");
		// si.setUserProfile(createUserProfile());
		return si;
	}

	public static ServiceInstance createServiceInstance1() {
		ServiceInstance si = createServiceInstance();
		si.setServiceInstanceVersion("0.1");
		return si;
	}

	public static ServiceInstance createInvalidServiceInstance1() {
		ServiceInstance si = createServiceInstance();
		si.setServiceInstanceVersion(OVERLONG_RANDOM_STRING);
		return si;
	}
}