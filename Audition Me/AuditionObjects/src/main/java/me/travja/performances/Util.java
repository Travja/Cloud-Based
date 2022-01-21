package me.travja.performances;

import java.util.Map;
import java.util.Optional;

public class Util {

    static {
        setup();
    }

    private static String from;

    private static void setup() {
        from = Optional.ofNullable(System.getenv("SMTP_EMAIL"))
                .orElse(null);
    }

    public static long getLong(Map<String, String> event, String key) {
        return Long.parseLong(event.get(key));
    }

    public static void ensureExists(Map<String, String> event, String key) {
        if (!event.containsKey(key))
            throw new RuntimeException("Missing `" + key + "` in request body.");
    }

    public static void sendEmail(String to, String subject, String body) {
        if (from == null)
            throw new RuntimeException("Cannot send email. Sender email is null");

        System.out.println("Sending email...\nSubject: " + subject + "\n\nBody:\n" + body);
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
