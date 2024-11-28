package bookstore.config;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String redirectUrl = (String) request.getSession().getAttribute("url_prior_login");

        if (redirectUrl != null && !redirectUrl.equals("/")) {
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("/auth/dashboard");
        }
    }
}
