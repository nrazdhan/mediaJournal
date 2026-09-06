package store.razdhan.mediajournal.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import jakarta.servlet.http.Cookie;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter{

    @Override
    public void doFilterInternal(HttpServletRequest request, 
        HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.equals("/login") ||
            uri.equals("/login.html") ||
            uri.equals("/sellingpoint.html") ||
            uri.equals("/email-order-confirmation") ||
            uri.equals("/") ||
            uri.equals("/create-order") ||
            uri.equals("/verify-payment") ||
            uri.equals("/error") ||
            uri.startsWith("/css/") ||
            uri.startsWith("/js/") ||
            uri.startsWith("/images/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String secret = "mySecretKeyForJsonWebToken123456";
        String token = null;
        Cookie[] cookies = request.getCookies();
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals("jwt")){
                token = cookie.getValue();
                break;
            }
        }

        if(token !=null){
            try{
                Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
            
                String username = claims.getSubject();
                String roles = claims.get("role").toString();

                // request.setAttribute("username", username);
                // request.setAttribute("role", roles);

                String[] role = roles.split(",");
                List<SimpleGrantedAuthority> lstGrants = new ArrayList<>();
                for(String r : role){
                    lstGrants.add(new SimpleGrantedAuthority(r.trim()));
                }

                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, lstGrants);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch(Exception ex){
                response.sendRedirect("/login.html");
            }
        }else{
            response.sendRedirect("/login.html");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // private void setUnauthorizedResponse(HttpServletResponse response) throws IOException{
    //     response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    //     response.getWriter().write("Invalid JWT cookie");
    // }
}
