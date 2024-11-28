package bookstore.config;

import bookstore.config.CustomAuthenticationSuccessHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public AuthenticationManager authenticationManager(
            final AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/auth/register", "/auth/login", "/auth/reset-password", "/auth/forgot-password", "/css/**", "/h2-console/**", "/books/list").permitAll()
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("email")
                .passwordParameter("password")
                // .successHandler(savedRequestAwareAuthenticationSuccessHandler())  // Use this to handle redirection
                .successHandler(customAuthenticationSuccessHandler)  // Use this to handle redirection
                // .failureUrl("/auth/login?loginError=true")
                .failureHandler(new SimpleUrlAuthenticationFailureHandler("/auth/login?loginError=true"))
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
            )
            .exceptionHandling((csrf) -> csrf
                .accessDeniedHandler(new AccessDeniedHandlerImpl())
                .defaultAuthenticationEntryPointFor((request, response, e) -> {
                    response.sendRedirect(request.getContextPath() + "/auth/login");
                }, new AntPathRequestMatcher("/**")) // Redirect unauthorized access to login page if not authenticated
            )
            .csrf((csrf) -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/auth/reset-password")
            )
            .headers((headers) -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            );

        return http.build();
    }

    @Bean
    public SavedRequestAwareAuthenticationSuccessHandler savedRequestAwareAuthenticationSuccessHandler() {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setDefaultTargetUrl("/auth/dashboard");  // Set your default target URL here
        // successHandler.setTargetUrlParameter("redirect"); // Use this parameter if needed for custom redirection
        return successHandler;
    }

}

