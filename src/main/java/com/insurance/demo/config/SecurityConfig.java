package com.insurance.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()).authorizeHttpRequests(auth -> auth.requestMatchers("/api/auth/**").permitAll()

				.requestMatchers(HttpMethod.POST, "/api/policies/purchase").hasRole("COSTOMER")

				.requestMatchers(HttpMethod.POST, "/api/policies/issue").hasAnyRole("ADMIN", "AGENT")

				.requestMatchers(HttpMethod.GET, "/api/policies/my-policies").hasRole("COSTOMER")

				.requestMatchers(HttpMethod.GET, "/api/policies/**").hasAnyRole("ADMIN", "AGENT")

				.requestMatchers(HttpMethod.PATCH, "/api/policies/*/activate").hasAnyRole("ADMIN", "AGENT")

				.requestMatchers(HttpMethod.PATCH, "/api/policies/*/cancel").hasAnyRole("ADMIN", "AGENT")

				.requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole("ADMIN").anyRequest().authenticated())
				.httpBasic(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		return http.build();
	}

//	@Bean
//	UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
//
//		UserDetails agent = User.builder().username("agent").password(passwordEncoder.encode("Agent123"))
//				                .roles("AGENT").build();
//		UserDetails admin = User.builder().username("admin").password(passwordEncoder.encode("Admin123"))
//				.roles("ADMIN").build();
//
//		UserDetails customer = User.builder().username("customer").password(passwordEncoder.encode("Customer123"))
//				.roles("CUSTOMER").build();
//
//		return new InMemoryUserDetailsManager(admin, agent, customer);
//
//	}

	@Bean
	PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

}
