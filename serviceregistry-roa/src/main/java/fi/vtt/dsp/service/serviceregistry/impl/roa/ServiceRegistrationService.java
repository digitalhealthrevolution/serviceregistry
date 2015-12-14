package fi.vtt.dsp.service.serviceregistry.impl.roa;

/**
 * Main abstract class for the service registry
 * 
 * @author ELETAI
 * @version 1.0
 * @created 03-tammi-2014 14:53:02
 */
public abstract class ServiceRegistrationService {

	private ServiceRegistrationROAInterface roaif;

	public ServiceRegistrationService() {
	}

	public ServiceRegistrationROAInterface getroaif() {
		return roaif;
	}

	/**
	 * 
	 * @param newVal
	 */
	public void setroaif(ServiceRegistrationROAInterface newVal) {
		roaif = newVal;
	}

}