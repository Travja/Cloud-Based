package me.travja.performances;

import java.util.Map;
import java.util.Optional;

public class Util {

    public static long getLong(Map<String, String> event, String key) {
        return Long.parseLong(event.get(key));
    }

    public static void ensureExists(Map<String, String> event, String key) {
        if (!event.containsKey(key))
            throw new RuntimeException("Missing `" + key + "` in request body.");
    }

    public static void ensureNotNull(String key, Object obj) {
        if (obj == null || (obj instanceof String && ((String) obj).trim().isEmpty()))
            throw new RuntimeException("Missing `" + key + "` in request.");
    }

    public static void sendEmail(Person person, String subject, String body) {
        if (person == null) {
            System.out.println("Target for email is null... Can't send email.");
            return;
        }
        sendEmail(person.getEmail(), subject, body);
    }

    public static void sendEmail(String to, String subject, String body) {
        String from = Optional.ofNullable(System.getenv("SMTP_EMAIL"))
                .orElse(null);
        if (from == null)
            throw new RuntimeException("Cannot send email. Sender email is null");
        if (to == null || to.trim().isEmpty())
            throw new RuntimeException("Cannot send email. Recipient email is null");


        System.out.println("Sending email...\nTo: " + to + "\nSubject: " + subject + "\n\nBody:\n" + body);
//        try {
//            AmazonSimpleEmailService client =
//                    AmazonSimpleEmailServiceClientBuilder.standard()
//                            .withRegion(Regions.US_WEST_2).build();
//            SendEmailRequest request = new SendEmailRequest()
//                    .withDestination(new Destination().withToAddresses(to))
//                    .withMessage(new Message()
//                            .withBody(new Body()
//                                    .withHtml(new Content()
//                                            .withCharset("UTF-8").withData(body))
//                                    .withText(new Content()
//                                            .withCharset("UTF-8").withData(body)))
//                            .withSubject(new Content()
//                                    .withCharset("UTF-8").withData(subject)))
//                    .withSource(from);
//            client.sendEmail(request);
//            System.out.println("Email sent!");
//        } catch (Exception ex) {
//            throw new RuntimeException("The email was not sent. Error message: " + ex.getMessage());
//        }


        System.out.println("Sent message successfully....");
    }

}
