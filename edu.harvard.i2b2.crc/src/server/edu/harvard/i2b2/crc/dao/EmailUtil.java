package edu.harvard.i2b2.crc.dao;


import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.harvard.i2b2.common.exception.I2B2Exception;
import edu.harvard.i2b2.crc.util.QueryProcessorUtil;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class EmailUtil {

	public void email (String toEmail, String fromEmail, String subject, String body) throws I2B2Exception, UnsupportedEncodingException, MessagingException {


		QueryProcessorUtil qpUtil = QueryProcessorUtil.getInstance();


		final String password = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.password");
		final String username = qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.username");
		Properties props = new Properties();
		props.put("mail.smtp.host", qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.host")); //SMTP Host
		props.put("mail.smtp.socketFactory.port", qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.port")); //SSL Port
		if (qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.ssl.enable").equalsIgnoreCase("true"))
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory"); //SSL Factory Class
		props.put("mail.smtp.auth", qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.auth")); //Enabling SMTP Authentication
		props.put("mail.smtp.port", qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.port")); //SMTP Port

		Authenticator auth = new Authenticator() {
			//override the getPasswordAuthentication method
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		};

		Session session = null;
		if (qpUtil.getCRCPropertyValue("edu.harvard.i2b2.crc.smtp.auth").equalsIgnoreCase("false"))
			session = Session.getDefaultInstance(props, null);
		else
			session = Session.getDefaultInstance(props, auth);
		sendEmail(session, toEmail,subject, body);


	}
	private  void sendEmail(Session session, String toEmail, String subject, String body) throws MessagingException, UnsupportedEncodingException{
		
			MimeMessage msg = new MimeMessage(session);
			//set message headers
			msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
			msg.addHeader("format", "flowed");
			msg.addHeader("Content-Transfer-Encoding", "8bit");

			msg.setFrom(new InternetAddress("no_reply@example.com", "NoReply-JD"));

			msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));

			msg.setSubject(subject, "UTF-8");

			msg.setText(body, "UTF-8");

			msg.setSentDate(new Date());

			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
			Transport.send(msg);  

		
	}
}
