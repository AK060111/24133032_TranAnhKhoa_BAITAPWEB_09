package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import vn.iotstar.security.CustomUserDetailsService;

@Configuration @RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.userDetailsService(userDetailsService)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/register", "/verify-otp", "/resend-register-otp",
                        "/forgot-password", "/reset-password", "/css/**", "/js/**", "/images/**", "/error", "/403", "/404").permitAll()
                .requestMatchers("/users/**").hasRole("ADMIN")
                .requestMatchers("/products/**").authenticated()
                .anyRequest().authenticated())
            .formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true).failureUrl("/login?error=true").permitAll())
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true).clearAuthentication(true).deleteCookies("JSESSIONID"))
            .exceptionHandling(ex -> ex.accessDeniedPage("/403"))
            .sessionManagement(session -> session.sessionFixation(fix -> fix.migrateSession()));
        return http.build();
    }
}
