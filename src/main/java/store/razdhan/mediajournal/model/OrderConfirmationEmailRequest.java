package store.razdhan.mediajournal.model;

public class OrderConfirmationEmailRequest {
    private OrderRequest orderRequest;
    private String orderId;
    private String paymentId;
    private int amountInPaise;
    private String currency;

    public OrderConfirmationEmailRequest(OrderRequest orderRequest, String orderId, String paymentId, int amountInPaise, String currency) {
        this.orderRequest = orderRequest;
        this.orderId = orderId;
        this.paymentId = paymentId;
        this.amountInPaise = amountInPaise;
        this.currency = currency;
    }

    public OrderRequest orderRequest() {
        return orderRequest;
    }

    public String orderId() {
        return orderId;
    }

    public String paymentId() {
        return paymentId;
    }

    public int amountInPaise() {
        return amountInPaise;
    }

    public String currency() {
        return currency;
    }
}
