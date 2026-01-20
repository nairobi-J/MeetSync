package com.root.meetsync.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private AdminSecurityInterceptor adminSecurityInterceptor;
    
    @Autowired
    private PendingUserSecurityInterceptor pendingUserSecurityInterceptor;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Pending user interceptor - applies to all authenticated routes
        registry.addInterceptor(pendingUserSecurityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/pending-approval", "/logout", "/css/**", "/js/**", "/images/**", 
                                   "/favicon.ico", "/login", "/signup", "/", "/oauth2/**", "/u/**", 
                                   "/event/participant/**", "/api/events/participant/**");
        
        // Admin interceptor - applies only to admin routes
        registry.addInterceptor(adminSecurityInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
           
        registry.addMapping("/**")
                .allowedOrigins("https://meetsync.innovatorslab.net")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true);
        
    }
}
