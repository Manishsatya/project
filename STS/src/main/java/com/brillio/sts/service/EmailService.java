package com.brillio.sts.service;
 
import org.apache.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
 
@Service
public class EmailService {
 
    private static final Logger logger = Logger.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
 
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
 
    private void sendEmail(String to, String subject, String body) {
        logger.info("Sending email to:  | Subject: "+ to+ subject);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        logger.info("Email sent successfully to: "+ to);
    }
 
 
 
	 // ✅ 1. Email when a new ticket is raised (Fault or Installation)
    public void sendTicketRaisedEmail(String userEmail, int ticketId, String serviceType) {
        logger.info("Sending ticket raised email for Ticket ID: "+ ticketId);
        String subject = "Ticket Raised Successfully - SwiftLink";
        String body = "Dear Customer,\n\n" +
                "Your request for " + serviceType + " has been successfully raised.\n" +
                "Ticket ID: " + ticketId + "\n" +
                "We will assign an engineer soon.\n\n" +
                "Best Regards,\nSwiftLink Team";
        sendEmail(userEmail, subject, body);
    }
 
    public void sendEngineerAssignedEmail(String userEmail, int ticketId, String engineerName) {
        logger.info("Sending engineer assigned email for Ticket ID: "+ ticketId);
        String subject = "Engineer Assigned to Your Ticket - SwiftLink";
        String body = "Dear Customer,\n\n" +
                "An engineer has been assigned to resolve your ticket.\n" +
                "Ticket ID: " + ticketId + "\n" +
                "Assigned Engineer: " + engineerName + "\n\n" +
                "Best Regards,\nSwiftLink Team";
        sendEmail(userEmail, subject, body);
    }
 
    // ✅ 3. Email when the ticket is resolved
    public void sendTicketResolvedEmail(String userEmail, int ticketId) {
        logger.info("Sending ticket resolved email for Ticket ID: "+ ticketId);
        String subject = "Your Ticket Has Been Resolved - SwiftLink";
        String body = "Dear Customer,\n\n" +
                "Your ticket (ID: " + ticketId + ") has been successfully resolved. 🎉\n\n" +
                "Thank you for choosing SwiftLink!\n\n" +
                "Best Regards,\nSwiftLink Team";
        sendEmail(userEmail, subject, body);
    }
 
    public void sendServiceActivationEmail(String userEmail, int ticketId, String serviceType) {
        logger.info("Sending service activation email for Ticket ID: "+ ticketId);
        String subject = "Service Activation - SwiftLink";
        String body = "Dear Customer,\n\n" +
                "Your " + serviceType + " request (Ticket ID: " + ticketId + ") has been successfully completed.\n" +
                "Your service is now **ACTIVE**. You can start using our services. 🚀\n\n" +
                "Best Regards,\nSwiftLink Team";
        sendEmail(userEmail, subject, body);
    }
    // ✅ 5. Email when a ticket fails
    public void sendTicketFailedEmail(String userEmail, int ticketId) {
        logger.info("Sending ticket failed email for Ticket ID: "+ ticketId);
        String subject = "Issue with Your Ticket - SwiftLink";
        String body = "Dear Customer,\n\n" +
                "Unfortunately, your ticket (ID: " + ticketId + ") could not be resolved. 😞\n" +
                "Please contact our support team for further assistance.\n\n" +
                "Best Regards,\nSwiftLink Team";
        sendEmail(userEmail, subject, body);
    }
 
    public void sendTicketDeferredEmail(String userEmail, int ticketId) {
        logger.info("Sending ticket deferred email for Ticket ID: "+ ticketId);
        String subject = "Your Ticket Has Been Deferred - SwiftLink";
        String body = "Dear Customer,\n\n" +
                "Your ticket (ID: " + ticketId + ") has been deferred due to unforeseen circumstances.\n" +
                "We will reassign an engineer soon.\n\n" +
                "Best Regards,\nSwiftLink Team";
        sendEmail(userEmail, subject, body);
    }
 
    // ✅ 7. Email for account approval (User)
    public void sendAccountApprovalEmail(String userEmail, String firstName) {
        logger.info("Sending account approval email for user: "+ firstName);
        String subject = "Your Account Has Been Approved!";
        String body = "Dear " + firstName + ",\n\n" +
                "Congratulations! Your account has been APPROVED.\n" +
                "You can now log in and start using our services.\n\n" +
                "Best Regards,\nSwiftLink Customer Support\n" +
                "Email: support@swiftlink.com\n" +
                "Contact: +1-800-123-4567";
        sendEmail(userEmail, subject, body);
    }
 
    public void sendAdminOrEngineerApprovalEmail(String userEmail, String firstName, String role) {
        logger.info("Sending account approval email for  as "+ firstName+ role);
        String subject = "Your Account Has Been Approved!";
        String body = "Dear " + firstName + ",\n\n" +
                "Congratulations! Your account has been APPROVED as a " + role + ".\n" +
                "You now have full access to the system.\n\n" +
                "Best Regards,\nSwiftLink Customer Support\n" +
                "Email: support@swiftlink.com\n" +
                "Contact: +1-800-123-4567";
        sendEmail(userEmail, subject, body);
    }
 
    // ✅ 9. Email for account rejection
    public void sendAccountRejectionEmail(String userEmail, String firstName) {
        logger.info("Sending account rejection email for user: "+ firstName);
        String subject = "Your Account Has Been Rejected";
        String body = "Dear " + firstName + ",\n\n" +
                "Unfortunately, your account request has been REJECTED.\n" +
                "If you believe this was a mistake, please contact our support team.\n\n" +
                "Best Regards,\nSwiftLink Customer Support\n" +
                "Email: support@swiftlink.com\n" +
                "Contact: +1-800-123-4567";
        sendEmail(userEmail, subject, body);
    }
 
 
    
}
 