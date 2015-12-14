package fi.vtt.dsp.service.serviceregistry.impl.handlers;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.common.UserProfile;
import fi.vtt.dsp.service.serviceregistry.common.instance.ServiceInstance;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAOGeneralSystemFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.DAONotFoundFault;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceInstanceDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBUserProfileDAO;
import fi.vtt.dsp.service.serviceregistry.impl.util.MailUtil;
import fi.vtt.dsp.serviceframework.common.Binding;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class BindingsGranter implements ServletContextListener {
	private static final Logger LOGGER = Logger.getLogger(BindingsGranter.class.getName());
	
	private static String serviceURL = null;	
	private static MailUtil mailer;
	
	public static void sendMail(String serviceId, String serviceInstanceId, Binding binding) {		
		StringBuilder message = new StringBuilder();

		LOGGER.log(Level.FINER, "Bindings-granter send mail called");
		
		try {
			MongoDBUserProfileDAO userProfileDAO = new MongoDBUserProfileDAO();
			MongoDBServiceRegistryDAO mongoDBServiceRegistryDAO = new MongoDBServiceRegistryDAO();
			MongoDBServiceInstanceDAO mongoDBServiceInstanceDAO = new MongoDBServiceInstanceDAO();
			ServiceRegistryEntry serviceRegistryEntry = mongoDBServiceRegistryDAO.findServiceRegistryEntry(serviceId);
			ServiceInstance serviceInstance = mongoDBServiceInstanceDAO.findServiceInstance(serviceId, serviceInstanceId);
			UserProfile userProfileRequesting = userProfileDAO.findUserProfileByUserId(binding.getRequestedByUserId());
			UserProfile userProfileOwner = userProfileDAO.findUserProfileByUserId(serviceRegistryEntry.getServiceDescription().getCreatedByUserId());
			
			message.append("Binding has been requested on service ");
			message.append(serviceRegistryEntry.getServiceDescription().getServiceDescriptionTitle());
			message.append(" instance ");
			message.append(serviceInstance.getHostingEntity());
			message.append(".\n");

			message.append("Binding is requested by ");
			
			if (userProfileRequesting.getFirstName() != null && !userProfileRequesting.getFirstName().equals("")) {
				message.append(userProfileRequesting.getFirstName());
				message.append(" ");
			}
			
			if (userProfileRequesting.getLastName() != null && !userProfileRequesting.getLastName().equals("")) {
				message.append(userProfileRequesting.getLastName());
				message.append(" ");
			}
			
			message.append(userProfileRequesting.getEmail());
			message.append("\n\n");

			message.append("To accept this binding, please click: \n");
			message.append(serviceURL);
			message.append(String.format("/serviceregistrations/%s/serviceinstances/%s/serviceaccessendpoint/bindings/%s", 
				serviceRegistryEntry.getServiceId(), serviceInstance.getServiceInstanceId(), binding.getBindingId()));
			message.append(String.format("/grant/%s/verification/%s", 
				"true", getVerificationCode(serviceRegistryEntry.getServiceDescription().getCreatedByUserId(), binding.getBindingId())));
			
			message.append("\n\n");
			message.append("To reject this binding, please click: \n");
			message.append(serviceURL);
			message.append(String.format("/serviceregistrations/%s/serviceinstances/%s/serviceaccessendpoint/bindings/%s", 
				serviceRegistryEntry.getServiceId(), serviceInstance.getServiceInstanceId(), binding.getBindingId()));
			message.append(String.format("/grant/%s/verification/%s", 
				"false", getVerificationCode(serviceRegistryEntry.getServiceDescription().getCreatedByUserId(), binding.getBindingId())));
			
			mailer.sendMail(userProfileOwner.getEmail(), "Service registry. Binding request", message.toString());
			
			LOGGER.log(Level.FINER, "Mail sent");
		} catch (DAOGeneralSystemFault | MessagingException | UnsupportedEncodingException e) {
			LOGGER.log(Level.SEVERE, "Error sending mail to grant binding", e);
		} catch (DAONotFoundFault ex) {
			LOGGER.log(Level.WARNING, "Error sending mail to grant binding.", ex);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		InputStream in = getClass().getResourceAsStream("/service.properties");
		Properties cp = new Properties();

		try {
			cp.load(in);
			
			String srHost = cp.getProperty("registry.api.host");			
			if ("".equals(srHost)) {
				LOGGER.log(Level.SEVERE, "Service registry host configuration is not set.");
				throw new RuntimeException("Service initialization failed.");
			}			
			String srPath = cp.getProperty("registry.api.path");			
			if ("".equals(srPath)) {
				LOGGER.log(Level.SEVERE, "Service registry path configuration is not set.");
				throw new RuntimeException("Service initialization failed.");
			}	
			serviceURL = srHost + srPath;
			
			String smtpHost = cp.getProperty("mail.smtp.host");			
			if ("".equals(smtpHost)) {
				LOGGER.log(Level.SEVERE, "SMTP mail host configuration is not set.");
				throw new RuntimeException("Service initialization failed.");
			}			
			String smtpPort = cp.getProperty("mail.smtp.port");			
			if ("".equals(smtpPort)) {
				LOGGER.log(Level.SEVERE, "SMTP mail host configuration is not set.");
				throw new RuntimeException("Service initialization failed.");
			}			
			String smtpTimeout = cp.getProperty("mail.smtp.timeout");			
			if ("".equals(smtpTimeout)) {
				LOGGER.log(Level.SEVERE, "SMTP mail host configuration is not set.");
				throw new RuntimeException("Service initialization failed.");
			}					
			mailer = new MailUtil(smtpHost, smtpPort, smtpTimeout);
			
		}
		catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Exception while loading service configuration", e);
		}
		finally {
			try {
				in.close();
			}
			catch (IOException e) {
			}
		}
	}
		
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
	}
	
	// TODO: This could be more secure...
	public static String getVerificationCode(String ownerId, String bindingId) {
		String verificationCode = ownerId + bindingId;
		LOGGER.log(Level.FINE, "Generated verification code: " + verificationCode);
		return verificationCode;
	}
	
	public static boolean checkVerificationCode(String ownerId, String bindingId, String verficationCode) {
		if (getVerificationCode(ownerId, bindingId).equals(verficationCode)) {
			return true;
		}
		
		return false;
	}
}
