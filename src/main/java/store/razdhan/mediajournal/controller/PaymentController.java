package store.razdhan.mediajournal.controller;

import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import java.util.Map;
import store.razdhan.mediajournal.model.OrderRequest;
import java.util.regex.Pattern;
import java.util.StringJoiner;


@RestController
public class PaymentController {

    private String KEY_ID = "";
    private String KEY_SECRET = "";

    private final String regexName = "^[a-zA-Z\\s'.]+$";
    private final String regexEmail = "^[a-zA-Z0-9-_!#$%&*`^~+=?/{|}]+(?:\\.[a-zA-Z0-9-_!#$%&*`~?=/{|}+]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
    private final String regexPhone = "^(?:(?:\\+|00)91|0)?[- ]?[6-9]\\d{9}$";
    private final String regexAddressLine = "^[a-zA-Z0-9\\s.,/#\\-]+$";
    private final String regexCityState = "^[a-zA-Z\\s.\\-]+$";
    private final String regexZipCode = "^(?:[1-9]\\d{5}|[0-9a-zA-Z\\s\\-]{3,10})$";

    private final Pattern patternFullName = Pattern.compile(regexName);
    private final Pattern patternEmail = Pattern.compile(regexEmail);
    private final Pattern patternPhone = Pattern.compile(regexPhone);
    private final Pattern patternAddressLine = Pattern.compile(regexAddressLine);
    private final Pattern patternCityState = Pattern.compile(regexCityState);
    private final Pattern patternZipCode = Pattern.compile(regexZipCode);

    @PostMapping("/create-order")
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest orderRequest){
        try {
            RazorpayClient razorpayClient = new RazorpayClient(KEY_ID, KEY_SECRET);

            String productId = orderRequest.productId();
            int amountInRupees = 0;
            switch(productId) {
                case "k1":
                    amountInRupees = 5000;
                    break;
                case "t1":
                    amountInRupees = 2000;
                    break;
                default:
                    return ResponseEntity.status(400).body("{\"error\": \"Invalid ProductId\"");
            }

            int amountInPaise = amountInRupees*100;

            StringJoiner errorMessage = new StringJoiner(",");
            if(!isValidName(orderRequest.fullName())) {
                errorMessage.add("Invalid name");
            }
            if(!isValidEmail(orderRequest.email())) {
                errorMessage.add("Invalid email");
            }
            if(!isValidPhone(orderRequest.phone())) {
                errorMessage.add("Invalid phone");
            }
            if(!isValidAddressLine(orderRequest.addressLine1())) {
                errorMessage.add("Invalid address line 1");
            }
            if(!isValidAddressLine(orderRequest.addressLine2())) {
                errorMessage.add("Invalid address line 2");
            }
            if(!isValidCityState(orderRequest.city())) {
                errorMessage.add("Invalid city");
            }
            if(!isValidCityState(orderRequest.state())) {
                errorMessage.add("Invalid state");
            }
            if(!isValidZipCode(orderRequest.zipCode())) {
                errorMessage.add("Invalid zip code");
            }

            if(errorMessage.length()>0) {
                return ResponseEntity.status(400).body("{\"error\": \"" + errorMessage.toString() + "\"}"); 
            }
            

            JSONObject options = new JSONObject();
            options.put("amount", amountInPaise);
            options.put("currency", "INR");
            options.put("receipt", "rec_txn_" + System.currentTimeMillis());
            com.razorpay.Order order = razorpayClient.orders.create(options);
            return ResponseEntity.ok(order.toString());
        } catch(RazorpayException ex){
            return ResponseEntity.status(500).body("{\"error\": \"" + ex.getMessage() + "\"}");
        }
    }

    private boolean isValidName(String name) {
        return patternFullName.matcher(name).matches();
    }
    
    private boolean isValidEmail(String email) {
        return patternEmail.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return patternPhone.matcher(phone).matches();
    }

    private boolean isValidCityState(String city) {
        return patternCityState.matcher(city).matches();
    }

    private boolean isValidAddressLine(String addressLine) {
        if(addressLine == null || addressLine.isEmpty()) {
            return true; // Address line 2 can be empty
        }
        return patternAddressLine.matcher(addressLine).matches();
    }

    private boolean isValidZipCode(String zipCode) {
        return patternZipCode.matcher(zipCode).matches();
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(@RequestBody Map<String, String> paymentInfo){
        String orderId = paymentInfo.get("orderId");
        String paymentId = paymentInfo.get("paymentId");
        String signature = paymentInfo.get("signature");

        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = com.razorpay.Utils.verifyPaymentSignature(options, KEY_SECRET);

            if(isValid) {
                return ResponseEntity.ok("{\"status\": \"success\", \"message\": \"Payment Verified\"}");
            } else {
                return ResponseEntity.status(400).body("{\"status\": \"failed\", \"message\": \"Invalid Signature\", \"Verification Failed\"}");
            }
        } catch(RazorpayException ex) {
            return ResponseEntity.status(400).body("{\"status\": \"failed\", \"message\": \"Verification Failed\"}");
        }
    }
}
