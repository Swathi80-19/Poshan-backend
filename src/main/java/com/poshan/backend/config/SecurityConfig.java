package com.poshan.backend.config;

import com.poshan.backend.security.AuthInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
public class SecurityConfig {

    private final AuthInterceptor authInterceptor;

    public SecurityConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer authWebMvcConfigurer() {
        return new org.springframework.web.servlet.config.annotation.WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(authInterceptor)
                    .addPathPatterns("/api/**")
                    .excludePathPatterns(
                        "/api/auth/**",
                        "/h2-console/**"
                    );
            }
        };
    }
}
