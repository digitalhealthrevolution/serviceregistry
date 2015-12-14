package fi.vtt.dsp.service.serviceregistry.impl.util;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.commons.lang.StringUtils;

public final class MailUtil {
	private final String smtpHost;
	private final String smtpPort;
	private String smtpTimeout;

	public MailUtil(String smtpHost, String smtpPort) {
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
	}
	public MailUtil(String smtpHost, String smtpPort, String smtpTimeout) {
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.smtpTimeout = smtpTimeout;
	}
	

	public void sendMail(String emailAddress, String subject, String content) throws MessagingException, UnsupportedEncodingException {
		Properties props = new Properties();
		props.put("mail.smtp.host", smtpHost);
		props.put("mail.smtp.port", smtpPort);
		if( StringUtils.isNotBlank(smtpTimeout) ) {
			props.put("mail.smtp.connectiontimeout", smtpTimeout);
			props.put("mail.smtp.timeout", smtpTimeout);
		}

		Session session = Session.getInstance(props);

		Message msg = new MimeMessage(session);

		InternetAddress from = new InternetAddress();
		from.setAddress("do_not_reply@digitalserviceshub.com");
		from.setPersonal("Do Not Reply");
		msg.setFrom(from);

		InternetAddress to = new InternetAddress();
		to.setAddress(emailAddress);
		msg.setRecipient(Message.RecipientType.TO, to);

		msg.setSubject(subject);
		msg.setContent(content, "text/plain; charset=utf-8");
		msg.setSentDate(new Date());
		Transport.send(msg);
	}
}
