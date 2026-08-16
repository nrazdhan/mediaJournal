package store.razdhan.mediajournal.model;

public class User {
    private long id;
    private String userName;
    private String ncryptedPassword;
    private String roles;
    
    public User(long id, String userName, String ncryptedPassword, String roles){
        this.id=id;
        this.userName=userName;
        this.ncryptedPassword=ncryptedPassword;
        this.roles=roles;
    }

    public long id(){
        return id;
    }
    public String userName(){
        return userName;
    }
    public String ncryptedPassword(){
        return ncryptedPassword;
    }
    public String roles(){
        return roles;
    }
}
