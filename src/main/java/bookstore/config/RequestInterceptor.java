package bookstore.config;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getRequestURI().equals("/auth/login")) {
            String referrer = request.getHeader("Referer");
            if (referrer != null && !referrer.equals("/") && !referrer.contains("/auth/login")) {
                request.getSession().setAttribute("url_prior_login", referrer);
            }
        }
        return true;
    }
}