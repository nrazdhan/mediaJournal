package store.razdhan.mediajournal.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import store.razdhan.mediajournal.model.OrderConfirmationEmailRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SendEmailResponse;
import software.amazon.awssdk.services.ses.model.Message;
import software.amazon.awssdk.services.ses.model.Body;
import software.amazon.awssdk.services.ses.model.Content;
import software.amazon.awssdk.services.ses.model.Destination;

@RestController
public class EmailController {
    private static final String DEFAULT_REGION = "us-east-2";

    private final SesClient sesClient;

    public EmailController() {
        this(DEFAULT_REGION);
    }
    public EmailController(String region) {
        sesClient = SesClient.builder().region(Region.US_EAST_2).build();
    }

    @PostMapping(value = "/email-order-confirmation")
    public ResponseEntity<Boolean> emailOrderConfirmation(@RequestBody OrderConfirmationEmailRequest request) {
        String fromAddress = "anubha@razdhan.store";
        String toAddress = request.orderRequest().email();
        String bccAddress1 = "nrazdhan2@gmail.com"; //"bestak27@gmail.com";
        String bccAddress2 = "nrazdhan2@gmail.com";
        String subject = "razdhan.store payment was successful!";
        String bodyHtml = "<html><title>Transaction Information</title><body>Rest of information from ORDER goes here</body></html>";

        Content subjectContent = Content.builder().data(subject).charset("utf-8").build();
        Content bodyContent = Content.builder().data(bodyHtml).charset("utf-8").build();

        Body body = Body.builder().html(bodyContent).build();

        Message message = Message.builder().subject(subjectContent).body(body).build();
        Destination destination = Destination.builder().toAddresses(toAddress).bccAddresses(bccAddress1, bccAddress2).build();

        try {
            SendEmailResponse emailResponse = sesClient.sendEmail(SendEmailRequest.builder()
            .source(fromAddress)
            .destination(destination)
            .message(message)
            .build());
            System.out.println("Payment Confirmation Email sent successfully! MessageId: " + emailResponse.messageId());
            return ResponseEntity.ok(true);
        } catch(Exception ex) {
            System.out.println("Failed to send payment confirmation email: " + ex.getMessage());
            return ResponseEntity.ok(false);
        }
    }
}
