package com.example.delivery.config;

import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

import com.example.delivery.member.entity.Role;
import com.example.delivery.security.JwtAccessDeniedHandler;
import com.example.delivery.security.JwtAuthenticationEntryPoint;
import com.example.delivery.security.JwtAuthenticationFilter;
import com.example.delivery.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // 인증 없이 호출할 수 있는 API. JWT 필터도 이 요청은 건너뛴다.
    private static final RequestMatcher PUBLIC_REQUESTS = new OrRequestMatcher(
            pathPattern(HttpMethod.POST, "/api/members"),
            pathPattern(HttpMethod.POST, "/api/auth/login"),
            pathPattern(HttpMethod.GET, "/api/menus"),
            pathPattern(HttpMethod.GET, "/api/menus/{menuId}"));

    private static final String OWNER = Role.OWNER.name();
    private static final String CUSTOMER = Role.CUSTOMER.name();

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_REQUESTS).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/menus").hasRole(OWNER)
                        .requestMatchers(HttpMethod.PUT, "/api/menus/{menuId}").hasRole(OWNER)
                        .requestMatchers(HttpMethod.DELETE, "/api/menus/{menuId}").hasRole(OWNER)
                        .requestMatchers(HttpMethod.POST, "/api/orders").hasRole(CUSTOMER)
                        .requestMatchers(HttpMethod.GET, "/api/orders").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/{orderId}/cancel").hasRole(CUSTOMER)
                        .requestMatchers(HttpMethod.PATCH, "/api/orders/{orderId}/status").hasRole(OWNER)
                        .requestMatchers(HttpMethod.POST, "/api/orders/{orderId}/payments").hasRole(CUSTOMER)
                        .requestMatchers(HttpMethod.GET, "/api/orders/{orderId}/payments").hasRole(CUSTOMER)
                        .anyRequest().authenticated())
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtProvider, authenticationEntryPoint, PUBLIC_REQUESTS),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
