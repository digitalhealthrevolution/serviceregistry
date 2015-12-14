package fi.vtt.dsp.service.serviceregistry.impl.handlers;

import fi.vtt.dsp.service.serviceregistry.common.ServiceRegistryEntry;
import fi.vtt.dsp.service.serviceregistry.impl.dao.MongoDBServiceRegistryDAO;

import java.io.FileOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;

public class ServiceIconDownloader {
	private static final String iconDirectory = System
			.getProperty("catalina.base")
			+ File.separator
			+ "webapps"
			+ File.separator
			+ "ROOT"
			+ File.separator
			+ "serviceregistry"
			+ File.separator;
	private static final Logger LOGGER = Logger
			.getLogger(ServiceIconDownloader.class.getName());

	public static void downloadIcon(ServiceRegistryEntry serviceRegistryEntry,
			String Id, boolean update) {
		serviceRegistryEntry.setServiceId(Id);
		downloadIcon(serviceRegistryEntry, update);
	}

	public static void downloadIcon(ServiceRegistryEntry serviceRegistryEntry, final boolean update) {
		final String iconURI = serviceRegistryEntry.getServiceDescription().getServiceIconURI();
		final String serviceId = serviceRegistryEntry.getServiceId();

		if (StringUtils.isBlank(iconURI)) {
			return;
		}
		
		Path path = Paths.get(iconDirectory);

		if (Files.notExists(path)) {
			try {
				if (System.getProperty("os.name").startsWith("Windows")) {
					Files.createDirectories(path);
				} else {
					Set<PosixFilePermission> perms = PosixFilePermissions
							.fromString("rwxrwxrwx");
					FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions
							.asFileAttribute(perms);
					Files.createDirectories(path, attr);
				}
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error creating folder for icons ", e);
			}
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					URL website = new URL(iconURI);
					ReadableByteChannel rbc = Channels.newChannel(website
							.openStream());
					String outputFile = iconDirectory + serviceId;
					Path path = Paths.get(outputFile);

					if (Files.notExists(path) || update) {
						FileOutputStream fos = new FileOutputStream(outputFile);
						fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					}
				} catch (java.net.UnknownHostException  e) {
					LOGGER.log(Level.FINE, "Invalid icon url");
				} catch (Exception e) {
					LOGGER.log(Level.SEVERE, "Error downloading icon ", e);
				}
			}
		}).start();
	}

	@PostConstruct
	public void checkIcons() {
		try {
			MongoDBServiceRegistryDAO mongoDBServiceRegistryDAO = new MongoDBServiceRegistryDAO();
			List<ServiceRegistryEntry> serviceRegistryEntries = mongoDBServiceRegistryDAO.getAll();

			for (ServiceRegistryEntry serviceRegistryEntry : serviceRegistryEntries) {
				downloadIcon(serviceRegistryEntry, false);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error checking icons ", e);
		}
	}
}
