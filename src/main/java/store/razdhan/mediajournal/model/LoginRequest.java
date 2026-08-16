package store.razdhan.mediajournal.model;

public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest(String username, String password){
        this.username = username;
        this.password = password;
    }

    public String username(){
        return username;
    }

    public String password(){
        return password;
    }
}
