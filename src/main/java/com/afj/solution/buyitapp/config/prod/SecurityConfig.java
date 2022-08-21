package com.afj.solution.buyitapp.config.prod;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.afj.solution.buyitapp.security.ApplicationSecurityEntryPoint;
import com.afj.solution.buyitapp.security.AuthenticationFilter;
import com.afj.solution.buyitapp.service.AppUserDetailsService;

/**
 * @author Tomash Gombosh
 */
@Configuration
@EnableWebSecurity
@Profile({"prod"})
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ApplicationSecurityEntryPoint applicationSecurityEntryPoint = new ApplicationSecurityEntryPoint();

    @Autowired
    private AppUserDetailsService appUserDetailsService;

    @Override
    public void configure(final AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder
                .userDetailsService(appUserDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationFilter tokenAuthenticationFilter() {
        return new AuthenticationFilter();
    }

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable()
                .formLogin()
                .disable()
                .httpBasic()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(applicationSecurityEntryPoint)
                .accessDeniedHandler(applicationSecurityEntryPoint)
                .and()
                .authorizeRequests()

                // Auth controller
                .antMatchers(HttpMethod.POST, "/api/v1/auth/anonymous").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/auth/anonymous").permitAll()

                // Localize controller
                .antMatchers(HttpMethod.GET, "/api/v1/localize").permitAll()

                // Login controller
                .antMatchers(HttpMethod.POST, "/api/v1/login").permitAll()

                //Order controller
                .antMatchers(HttpMethod.POST, "/api/v1/orders").hasAnyRole("ANONYMOUS", "USER", "ADMIN")
                .antMatchers(HttpMethod.GET, "/api/v1/orders/**").hasAnyRole("ANONYMOUS", "USER", "ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/v1/orders/**").hasAnyRole("ANONYMOUS", "USER", "ADMIN")

                //Product controller
                .antMatchers(HttpMethod.GET, "/api/v1/products").hasAnyRole("ANONYMOUS", "USER", "ADMIN")
                .antMatchers(HttpMethod.GET, "/api/v1/products/**/image").hasAnyRole("ANONYMOUS", "USER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/api/v1/products").hasAnyRole("USER", "ADMIN")

                //User controller
                .antMatchers(HttpMethod.POST, "/api/v1/users").hasAnyRole("ANONYMOUS")
                .antMatchers(HttpMethod.GET, "/api/v1/users/**").hasAnyRole("USER", "ADMIN")

                .anyRequest()
                .authenticated();

        http.addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(new AnonymousAuthenticationProvider("anonymous"));
    }
}