package com.brillio.sts.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.brillio.sts.service.AccountsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Bean
    public JwtAuthFilter jwtAuthFilter(UserDetailsService userDetailsService) {
        return new JwtAuthFilter(jwtService, userDetailsService);
    }

    @Bean
    public UserDetailsService userDetailsService(AccountsService accountsService) {
        return accountsService; // Only providing the instance, no direct field injection
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/accounts/showAccounts", "/accounts/addAccount", "/accounts/generateToken",
                                "/accounts/nextAccountId", "/accounts/approvalRequests",
                                "/accounts/search/{email}", "/accounts/updateStatus/{id}",
                                "/accounts/searchAccountByRole/{role}", "/tickets/pending/{pincode}",
                                "/accounts/userBypincode/{pincode}", "/accounts/engineerBypincode/{pincode}",
                                "/connections/searchConnectionUserId/{userId}", "/accounts/securityQuestion/{email}",
                                "/accounts/superAdmin", "/accounts/verifySecurityAnswer",
                                "/accounts/resetPassword", "/ticketHistory/showTicketsHistory",
                                "/tickets//raiseTicketsFault", "/ticketHistory/userTicketHistory/{userId}",
                                "/ticketHistory/engineerTicketHistory/{engineerId}",
                                "/ticketHistory/pincodeTicketHistory/{pincode}",
                                "/tickets/raiseInstallationTicket","/hazards/**" ,"/accounts/**"
                        ).permitAll()
                        .requestMatchers("/tickets/**", "/leave/**", "/connections/**").permitAll()
                        .requestMatchers("/accounts/user/**").authenticated()
                        .requestMatchers("/accounts/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/accounts/engineer/**").authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider(userDetailsService(null)))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
