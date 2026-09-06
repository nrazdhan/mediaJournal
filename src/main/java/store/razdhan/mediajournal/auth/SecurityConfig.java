package store.razdhan.mediajournal.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import store.razdhan.mediajournal.filter.JwtFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    // @Bean
    // FilterRegistrationBean<JwtFilter> registerFilter(JwtFilter jwtFilter) {
    //     FilterRegistrationBean<JwtFilter> reg = new FilterRegistrationBean<>();
    //     reg.setFilter(jwtFilter);
    //     reg.addUrlPatterns("/media/*", "/profile");
    //     return reg;
    // }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login.html", "/sellingpoint.html", "/email-order-confirmation", "/login", "/error", "/create-order", "/verify-payment", "/css/**", "/js/**", "/images/**").permitAll()
                .anyRequest().authenticated()
            )
            .anonymous(anonymous -> anonymous.principal("anonymousUser").authorities("ROLE_ANONYMOUS"))
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
