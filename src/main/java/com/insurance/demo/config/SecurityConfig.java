package com.insurance.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.insurance.demo.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider,
			JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

		http.csrf(AbstractHttpConfigurer::disable).authenticationProvider(authenticationProvider).authorizeHttpRequests(
				auth -> auth

						// PUBLIC AUTH
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.requestMatchers("/api/auth/**").permitAll()

						// POLICY PLANS
						.requestMatchers(HttpMethod.POST, "/api/plans/create").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/plans/update/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/plans/deactivate/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/plans/active", "/api/plans/product/*/active",
								"/api/plans/**")
						.hasAnyRole("ADMIN", "AGENT", "CUSTOMER")

						// POLICIES
						.requestMatchers(HttpMethod.POST, "/api/policies/purchase").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/policies/issue").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/policies/my-policies").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/policies/**").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.PATCH, "/api/policies/*/cancel").hasAnyRole("ADMIN", "AGENT")

						// CLAIMS
						.requestMatchers(HttpMethod.POST, "/api/claims/raise").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/claims/my-claims").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/claims/{claimId}")
						.hasAnyRole("ADMIN", "AGENT", "CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/claims/{claimId}/history").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.PATCH, "/api/claims/*/review").hasRole("AGENT")
						.requestMatchers(HttpMethod.PATCH, "/api/claims/*/under-review").hasRole("AGENT")
						.requestMatchers(HttpMethod.PATCH, "/api/claims/*/final-decision").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/claims").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.POST, "/api/claims/*/documents")
						.hasAnyRole("CUSTOMER", "AGENT", "ADMIN")

						// USERS
						.requestMatchers(HttpMethod.GET, "/api/users/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/users/**").hasRole("ADMIN")

						// CUSTOMER MANAGEMENT

						.requestMatchers(HttpMethod.POST, "/api/customers/**").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.PUT, "/api/customers/**").hasRole("CUSTOMER")

						.requestMatchers(HttpMethod.GET, "/api/customers").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/customers/paged").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/customers/*").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")

						.requestMatchers(HttpMethod.DELETE, "/api/customers/*").hasRole("ADMIN")
						
						// INSURANCE PRODUCT MANAGEMENT

						.requestMatchers(HttpMethod.POST, "/api/product/create").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/product/update/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/product/deactivate/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/product/active").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/product/page").hasAnyRole("ADMIN", "AGENT")

						// Fallback
						.anyRequest().authenticated())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {

		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService);

		authenticationProvider.setPasswordEncoder(passwordEncoder);

		return authenticationProvider;
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

}
