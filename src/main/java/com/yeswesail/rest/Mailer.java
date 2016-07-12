package com.yeswesail.rest;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Mailer {
	private static ApplicationProperties prop = ApplicationProperties.getInstance();
	
	public static void sendMail(String to, String cc, String subject, String body, String imagePath) 
			throws AddressException, MessagingException
	{
		Properties properties = System.getProperties();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.host", prop.getMailSmtpHost());

		Authenticator auth = new SMTPAuthenticator();
		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties, auth);
		session.setDebug(true);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(prop.getMailFrom()));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		if (cc != null)
			message.addRecipient(Message.RecipientType.BCC, new InternetAddress(cc));
		message.setSubject(subject);

        MimeMultipart multipart = new MimeMultipart("related");

        // first part  (the html)
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent(body, "text/html");

        // add it
        multipart.addBodyPart(messageBodyPart);
        
        // second part (the image)
        messageBodyPart = new MimeBodyPart();
                
        DataSource fds = new FileDataSource(imagePath);
        messageBodyPart.setDataHandler(new DataHandler(fds));
        messageBodyPart.setHeader("Content-ID","<image>");

        // add it
        multipart.addBodyPart(messageBodyPart);

        // put everything together
        message.setContent(multipart);

		// Send message
		Transport.send(message);
	}
	
	public static void sendMail(String to, String subject, String body, String imagePath) 
			throws AddressException, MessagingException
	{
		sendMail(to, null, subject, body, imagePath);
	}
}
