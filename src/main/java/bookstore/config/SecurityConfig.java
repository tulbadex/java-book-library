package bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/auth/register", "/auth/login", "/auth/reset-password", "/auth/forgot-password", "/css/**", "/h2-console/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/auth/dashboard", true)
                .failureUrl("/auth/login?loginError=true")
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
            )
            .csrf((csrf) -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/auth/reset-password")
            )
            .headers((headers) -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

        return http.build();
    }
}

