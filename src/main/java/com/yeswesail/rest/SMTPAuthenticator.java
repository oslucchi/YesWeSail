package com.yeswesail.rest;

import javax.mail.PasswordAuthentication;

public class SMTPAuthenticator extends javax.mail.Authenticator {
	public PasswordAuthentication getPasswordAuthentication() {
		ApplicationProperties prop = new ApplicationProperties();
		String username = prop.getMailUser();
		String password = prop.getMailPassword();
		return new PasswordAuthentication(username, password);
	}
}
