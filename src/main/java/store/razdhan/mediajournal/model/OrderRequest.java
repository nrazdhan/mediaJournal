package store.razdhan.mediajournal.model;

public class OrderRequest {
    private String productId;
    private String color;
    private String size;
    private String fullName;
    private String email;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String zipCode;
    private String country;

    public OrderRequest(
        String productId,
        String color,
        String size,
        String fullName,
        String email,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String zipCode,
        String country
    ) {
        this.productId = productId;
        this.color = color;
        this.size = size;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    public String productId() {
        return productId;
    }

    public String color() {
        return color;
    }

    public String size() {
        return size;
    }

    public String fullName() {
        return fullName;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public String addressLine1() {
        return addressLine1;
    }

    public String addressLine2() {
        return addressLine2;
    }

    public String city() {
        return city;
    }

    public String state() {
        return state;
    }

    public String zipCode() {
        return zipCode;
    }

    public String country() {
        return country;
    }
}