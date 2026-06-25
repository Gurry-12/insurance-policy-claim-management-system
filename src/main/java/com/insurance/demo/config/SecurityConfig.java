package com.insurance.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import com.insurance.demo.security.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider,
			JwtAuthenticationFilter jwtAuthenticationFilter,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver handlerExceptionResolver) throws Exception {

		http.cors(cors -> {
		}).csrf(AbstractHttpConfigurer::disable)

				.authenticationProvider(authenticationProvider)

				.exceptionHandling(exceptionHandling -> exceptionHandling
						.authenticationEntryPoint((request, response, authException) -> handlerExceptionResolver
								.resolveException(request, response, null, authException))
						.accessDeniedHandler((request, response, accessDeniedException) -> handlerExceptionResolver
								.resolveException(request, response, null, accessDeniedException)))

				.authorizeHttpRequests(auth -> auth

						// IMPORTANT FOR CORS
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

						// PUBLIC
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

						.requestMatchers("/api/auth/**").permitAll()

						// PLANS
						.requestMatchers(HttpMethod.POST, "/api/plans/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/plans/*").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/plans/*/deactivate").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/plans/*/activate").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/plans/active").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/plans/*/active").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/plans/page").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/plans/**").hasAnyRole("ADMIN", "AGENT")

						// POLICIES
						.requestMatchers(HttpMethod.POST, "/api/policies/purchase").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.POST, "/api/policies/issue").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/policies/my-policies").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/policies/customer/*").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/policies/*").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/policies").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.PATCH, "/api/policies/*/cancel").hasAnyRole("ADMIN", "AGENT")

						// CLAIMS
						.requestMatchers(HttpMethod.POST, "/api/claims/raise").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/claims/my-claims").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/claims/*").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")
						.requestMatchers(HttpMethod.PATCH, "/api/claims/*/review").hasRole("AGENT")
						.requestMatchers(HttpMethod.PATCH, "/api/claims/*/under-review").hasRole("AGENT")
						.requestMatchers(HttpMethod.PATCH, "/api/claims/*/final-decision").hasRole("ADMIN")
						.requestMatchers(HttpMethod.POST, "/api/claims/*/documents")
						.hasAnyRole("CUSTOMER", "AGENT", "ADMIN")

						// DOCUMENTS
						.requestMatchers(HttpMethod.POST, "/api/document/upload/**").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.DELETE, "/api/document/**").hasRole("CUSTOMER")

						// CUSTOMERS
						.requestMatchers(HttpMethod.POST, "/api/customers/**").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.PUT, "/api/customers/**").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/customers/profile").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/customers").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/customers/page").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/customers/*").hasAnyRole("ADMIN", "AGENT")

						// PRODUCTS
						.requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PUT, "/api/products/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.PATCH, "/api/products/**").hasRole("ADMIN")
						.requestMatchers(HttpMethod.GET, "/api/products/active")
						.hasAnyRole("ADMIN", "AGENT", "CUSTOMER").requestMatchers(HttpMethod.GET, "/api/products/**")
						.hasAnyRole("ADMIN", "AGENT")

						// PAYMENTS
						.requestMatchers(HttpMethod.POST, "/api/payments").hasAnyRole("CUSTOMER", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/payments/my-payments").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/payments/my-policies/*").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.GET, "/api/payments/policy/*").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/payments/page").hasAnyRole("ADMIN", "AGENT")
						.requestMatchers(HttpMethod.GET, "/api/payments/*").hasAnyRole("ADMIN", "AGENT", "CUSTOMER")

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
