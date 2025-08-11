package com.codegym.socialmedia.config;

import com.codegym.socialmedia.jwt.JwtAuthenticationFilter;
import com.codegym.socialmedia.service.admin.AdminDetailsService;
import com.codegym.socialmedia.service.user.CustomUserDetailsService;
import com.codegym.socialmedia.service.user.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.List;
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Autowired
    private CustomOAuth2UserService oauth2UserService;

    @Autowired
    private AdminDetailsService adminDetailsService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public DaoAuthenticationProvider userAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider adminAuthProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(adminDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(adminAuthProvider())
                .authenticationProvider(userAuthProvider())
                .build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/login", "/admin/login/**", "/css/**", "/js/**").permitAll()
                        .anyRequest().hasRole("ADMIN")
                )
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .loginProcessingUrl("/admin/login")
                        .defaultSuccessUrl("/admin/dashboard", true)
                        .failureUrl("/admin/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")
                        .logoutSuccessUrl("/admin/login?logout=true")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/admin/**").denyAll()
                        .requestMatchers("/", "/login", "/login/**", "/register", "/css/**", "/js/**", "/images/**").permitAll()
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
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                        .successHandler((request, response, authentication) -> response.sendRedirect("/news-feed"))
                        .failureHandler((request, response, exception) -> {
                            System.err.println("OAuth2 login failed: " + exception.getMessage());
                            response.sendRedirect("/login?error=oauth2");
                        })
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder(10);
//    }
//
//    @Autowired
//    private CustomOAuth2UserService oauth2UserService;
//
//    @Autowired
//    private AdminDetailsService adminDetailsService;
//
//    @Autowired
//    private CustomUserDetailsService customUserDetailsService;
//
//    // Provider cho user thường
//    @Bean
//    public DaoAuthenticationProvider userAuthProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(customUserDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    // Provider cho admin
//    @Bean
//    public DaoAuthenticationProvider adminAuthProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(adminDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//
//    @Bean
//    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
//        return http.getSharedObject(AuthenticationManagerBuilder.class)
//                .authenticationProvider(adminAuthProvider())
//                .authenticationProvider(userAuthProvider())
//                .build();
//    }
//
//
////    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService;
//
//    // --- Chain cho admin ---
//    @Bean
//    @Order(1)
//    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/admin/**")
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/admin/login", "/admin/login/**", "/css/**", "/js/**").permitAll()
//                        .anyRequest().hasRole("ADMIN")
//                )
//                .formLogin(form -> form
//                        .loginPage("/admin/login")
//                        .loginProcessingUrl("/admin/login")
//                        .defaultSuccessUrl("/admin/dashboard", true)
//                        .failureUrl("/admin/login?error=true")
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/admin/logout")
//                        .logoutSuccessUrl("/admin/login?logout=true")
//                )
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }
//
//    // --- Chain cho user ---
//    @Bean
//    @Order(2)
//    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/admin/**").denyAll()
//                        .requestMatchers("/", "/login", "/login/**", "/register", "/css/**", "/js/**", "/images/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .loginProcessingUrl("/login")
//                        .usernameParameter("username")
//                        .passwordParameter("password")
//                        .defaultSuccessUrl("/news-feed", true)
//                        .failureUrl("/login?error=true")
//                        .permitAll()
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
//                        .successHandler((request, response, authentication) -> response.sendRedirect("/news-feed"))
//                        .failureHandler((request, response, exception) -> {
//                            System.err.println("OAuth2 login failed: " + exception.getMessage());
//                            response.sendRedirect("/login?error=oauth2");
//                        })
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout=true")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .permitAll()
//                )
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }


    // --- Chain cho admin ---
//    @Bean
//    @Order(1)
//    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
//        http
//                .securityMatcher("/admin/**") // chỉ bắt request /admin
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/admin/login", "/css/**", "/js/**").permitAll()
//                        .anyRequest().hasRole("ADMIN")
//                )
//                .formLogin(form -> form
//                        .loginPage("/admin/login")
//                        .loginProcessingUrl("/admin/login") // phải khác /login của user
//                        .defaultSuccessUrl("/admin/dashboard", true)
//                        .failureUrl("/admin/login?error=true")
//                        .permitAll()
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/admin/logout")
//                        .logoutSuccessUrl("/admin/login?logout=true")
//                )
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }
//
//    // --- Chain cho user ---
//    @Bean
//    @Order(2)
//    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
//
//        http
//                .headers(headers -> headers
//                        .contentSecurityPolicy(csp -> csp
//                                .policyDirectives("img-src 'self' https://lh3.googleusercontent.com https://*.fbcdn.net https://res.cloudinary.com https://graph.facebook.com https://i.imgur.com https://secure.gravatar.com data: blob:;")
//                        )
//                )
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/admin/**").denyAll()
//                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/api/debug/**", "/api/test/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .loginProcessingUrl("/login")
//                        .usernameParameter("username")
//                        .passwordParameter("password")
//                        .defaultSuccessUrl("/news-feed", true)
//                        .failureUrl("/login?error=true")
//                        .permitAll()
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/login")
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .userService(oauth2UserService)
//                        )
//                        .successHandler((request, response, authentication) -> {
//                            response.sendRedirect("/news-feed");
//                        })
//                        .failureHandler((request, response, exception) -> {
//                            System.err.println("OAuth2 login failed: " + exception.getMessage());
//                            exception.printStackTrace();
//                            response.sendRedirect("/login?error=oauth2");
//                        })
//                )
//                .logout(logout -> logout
//                        .logoutUrl("/logout")
//                        .logoutSuccessUrl("/login?logout=true")
//                        .invalidateHttpSession(true)
//                        .deleteCookies("JSESSIONID")
//                        .permitAll()
//                )
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }
//}
