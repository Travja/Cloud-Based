package me.travja.performances.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.Getter;
import me.travja.performances.api.models.Person;
import me.travja.performances.serializers.ZonedDateTimeSerializer;
import org.mindrot.jbcrypt.BCrypt;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

public class Util {

    @Getter
    public static final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules()
            .registerModule(new SimpleModule().addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer()));

    public static long getLong(Map<String, Object> event, String key) {
        if (event.get(key) instanceof Integer)
            return ((Integer) event.get(key)).longValue();

        return Long.parseLong((String) event.get(key));
    }

    public static void ensureExists(Map<String, Object> event, String... keys) {
        for (String key : keys) {
            if (!event.containsKey(key))
                throw new RuntimeException("Missing `" + key + "` in request body.");
        }
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

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(10)); // 10 Salting rounds!
    }

    public static boolean checkHash(String pass, String hash) {
        return BCrypt.checkpw(pass, hash);
    }

    public static void printStack(StackTraceElement[] stack) {
        for (int i = 1; i < stack.length; i++) {
            StackTraceElement s = stack[i];
            System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
                    + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
    }

}
