package me.travja.email;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class EmailHandler implements RequestHandler<Map<String, String>, Map<String, Object>> {

    private static String from, host, password, port;

    private static Properties props = System.getProperties();
//    private static Session    session;

    private void setup() {
        from = Optional.ofNullable(System.getenv("SMTP_EMAIL"))
                .orElseThrow(() -> new IllegalArgumentException("Sender email undefined!"));
        host = Optional.ofNullable(System.getenv("SMTP_SERVER"))
                .orElseThrow(() -> new IllegalArgumentException("SMTP server undefined!"));
        password = Optional.ofNullable(System.getenv("SMTP_PASSWORD"))
                .orElseThrow(() -> new IllegalArgumentException("Password undefined!"));
        port = Optional.ofNullable(System.getenv("SMTP_PORT"))
                .orElse("587");

        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

//        session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(from, password);
//            }
//        });
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, String> event, Context context) {
        setup();

        String to = event.get("to"),
                subject = event.getOrDefault("subject", "No subject"),
                body = event.getOrDefault("body", "No body");

        if (to == null || to.trim().isEmpty())
            return Map.of("statusCode", 400, "error", "No recipient specified.");

//        try {
            sendEmail(to, subject, body);
//        } catch (MessagingException e) {
//            throw new RuntimeException(e.getMessage(), e);
//        }

        Map<String, Object> ret = new HashMap<>();
        ret.put("statusCode", 204);

        return ret;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            // Replace US_WEST_2 with the AWS Region you're using for
                            // Amazon SES.
                            .withRegion(Regions.US_WEST_2).build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(new Destination().withToAddresses(to))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withHtml(new Content()
                                            .withCharset("UTF-8").withData(body))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(body)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(subject)))
                    .withSource(from)
                    // Comment or remove the next line if you are not using a
                    // configuration set
//                    .withConfigurationSetName(CONFIGSET)
                    ;
            client.sendEmail(request);
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: " + ex.getMessage());
        }

        System.out.println("Sent message successfully....");
    }
}
