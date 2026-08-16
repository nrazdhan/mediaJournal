package store.razdhan.mediajournal.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.CookieValue;

import store.razdhan.mediajournal.repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import store.razdhan.mediajournal.model.User;

import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
public class LoginController {

    UserRepository userRepository;
    public LoginController(){
        userRepository = UserRepository.instance();
    }

    @PostMapping(path = "/login")
    public ResponseEntity<String> loginAndReturnJwt(@RequestParam String username, 
        @RequestParam String password){

        String secret = "mySecretKeyForJsonWebToken123456";
        // String username = request.username();
        // String password = request.password();

        User user = userRepository.getUser(username);
        if(user==null){
            return ResponseEntity.status(401).body("Invalid Credentials");
        }

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean passwordMatched = passwordEncoder.matches(password, user.ncryptedPassword());
        if(!passwordMatched){
            return ResponseEntity.status(401).body("Invalid Credentials");
        }

        String jwt = Jwts.builder()
                        .setSubject(user.userName())
                        .claim("role", user.roles())
                        .setIssuedAt(new Date())
                        .setExpiration(
                            new Date(System.currentTimeMillis() + 1000*60*60))
                        .signWith(Keys.hmacShaKeyFor(
                            secret.getBytes(StandardCharsets.UTF_8)), 
                            SignatureAlgorithm.HS256)
                        .compact();
        // return ResponseEntity.ok(jwt);

        ResponseCookie responseCookie = ResponseCookie.from("jwt", jwt)
            .httpOnly(true)
            .maxAge(60*60)
            .path("/")
            .sameSite("Lax")
            .secure(false)
            .build();

        return ResponseEntity
            .status(HttpStatus.SEE_OTHER)
            .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
            .header(HttpHeaders.LOCATION, "/index.html")
            .build();
    };

    @GetMapping("/profile")
    @PreAuthorize("isAutheticated()")
    public ResponseEntity<String> getProfile(@CookieValue(name="jwt", required=false) String token){
        if(token==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("no token received");
        }
        return ResponseEntity.ok().body("Token Received: " + token);
    }
}
