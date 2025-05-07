package com.rpms.NotificationsAndReminders;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailNotification implements Notifiable {
    // data fields
    private String recipientEmail;
    private String subject;
    private String message;

    // constructor
    public EmailNotification(String recipientEmail, String subject, String message) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.message = message;
    }

    // getters and setters
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // Method to send the email using JavaMail API
    @Override
    public void sendNotification() {
        final String senderEmail = "rpms502082.test@gmail.com"; // hard-coded email address
        final String senderPassword = "iuyl msru fgzu zobf"; // an app password (not my Gmail password)

        // email SMTP configuration
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // session and Authenticator
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            // compose and sending the email
            Message email = new MimeMessage(session);
            email.setFrom(new InternetAddress(senderEmail));
            email.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            email.setSubject(subject);
            email.setText(message);

            Transport.send(email);
            System.out.println("Email sent successfully to " + recipientEmail);
        } catch (MessagingException e) {
            System.out.println("Failed to send email.");
        }
    }
}
