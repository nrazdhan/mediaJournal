package store.razdhan.mediajournal.repository;

import java.util.ArrayList;
import java.util.List;
import store.razdhan.mediajournal.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserRepository {

    List<User> users;
    private static UserRepository userRepository;
    private static PasswordEncoder passwordEncoder;
    private UserRepository(){
        users = new ArrayList<>();
        passwordEncoder = new BCryptPasswordEncoder();
    }

    public static synchronized UserRepository instance(){
        if(userRepository==null){
            userRepository = new UserRepository();
            String anuNcryptPassword = passwordEncoder.encode("anubha123");
            String naveenNCryptPassword = passwordEncoder.encode("naveen123");
            User anubha = new User(1, "anubha", anuNcryptPassword, "ROLE_ADMIN, ROLE_USER");
            User naveen = new User(2, "naveen", naveenNCryptPassword, "ROLE_ADMIN, ROLE_USER");
            userRepository.users.add(naveen);
            userRepository.users.add(anubha);
        }
        return userRepository;
    }

    public User getUser(String userName){
        return userRepository.users.stream().filter(x -> x.userName().equals(userName)).findFirst().get();
    }
}
