package com.codegym.socialmedia.config;

import com.codegym.socialmedia.jwt.JwtAuthenticationFilter;
import com.codegym.socialmedia.service.admin.AdminDetailsService;
import com.codegym.socialmedia.service.user.CustomUserDetailsService;
import com.codegym.socialmedia.service.user.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomOAuth2UserService oauth2UserService;
    @Autowired
    private AdminDetailsService adminDetailsService;
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("img-src 'self' " +
                                        "https://*.googleusercontent.com " +
                                        "https://*.fbcdn.net " +
                                        "https://res.cloudinary.com " +
                                        "https://graph.facebook.com " +
                                        "https://i.imgur.com " +
                                        "https://secure.gravatar.com data: blob:;")
                        )
                )

                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/api/debug/**", "/api/test/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/news-feed", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(oauth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            System.out.println("OAuth2 login successful for user: " + authentication.getName());
                            response.sendRedirect("/news-feed");
                        })
                        .failureHandler((request, response, exception) -> {
                            System.err.println("OAuth2 login failed: " + exception.getMessage());
                            exception.printStackTrace();
                            response.sendRedirect("/login?error=oauth2");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    @Order(1) // Ưu tiên cao hơn filter mặc định
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/admin/**") // Chỉ áp dụng cho admin API
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/admin/login").permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .userDetailsService(adminDetailsService)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
