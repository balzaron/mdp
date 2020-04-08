package com.miotech.mdp.admin.config;

import com.miotech.mdp.admin.security.CustomAuthenticationFilter;
import com.miotech.mdp.admin.security.SecurityService;
import com.mioying.consul.ConfigService;
import com.mioying.consul.exception.MioTechConfigException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.Collection;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@ConditionalOnExpression("#{environment.getActiveProfiles()[0] != 'test'}") // disable this config in testing
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    SecurityService securityService;

    @Autowired
    ConfigService configService;

    @Value("${security.pass-token}")
    private String passToken;

    @Bean
    public ConfigService getConfigService() {
        return ConfigService.getInstance();
    }

    public String getLdapUrl() throws MioTechConfigException {
        return configService.getValueWithNameSpace("MioGlobal", "DEV", "com.miotech.internalSystems.ldap.url") + "dc=miotech,dc=com";
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf()
                .disable();
        http
                .authorizeRequests()
                .antMatchers("/",
                        "/v2/api-docs",
                        "/configuration/ui",
                        "/swagger-resources/**",
                        "/configuration/**",
                        "/swagger-ui.html",
                        "/webjars/**")
                .permitAll()
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .addFilterBefore(
                        customAuthenticationFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .logout()
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(securityService.logoutSuccessHandler())

                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new Http403ForbiddenEntryPoint());

                /*  暂时不需要remember me功能
                .and()
                .rememberMe()
                .rememberMeServices(rememberMeServices())
                .key("mdp-server")*/
        ;
        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED);
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .ldapAuthentication()
                .userDnPatterns("cn={0},ou=Users")
                .groupSearchBase("ou=Users")
                .contextSource()
                .url(getLdapUrl());
    }

    @Bean
    public AbstractAuthenticationProcessingFilter customAuthenticationFilter() throws Exception {
        CustomAuthenticationFilter authenticationFilter = new CustomAuthenticationFilter();
        authenticationFilter.setAuthenticationSuccessHandler(securityService.loginSuccessHandler());
        authenticationFilter.setAuthenticationFailureHandler(securityService.loginFailureHandler());
        authenticationFilter.setRequiresAuthenticationRequestMatcher(new AntPathRequestMatcher("/api/login", "POST"));
        authenticationFilter.setAuthenticationManager(authenticationManagerBean());
        authenticationFilter.setSecurityService(securityService);
        authenticationFilter.setPassToken(passToken);
        return authenticationFilter;
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        BasicRememberMeUserDetailsService rememberMeUserDetailsService = new BasicRememberMeUserDetailsService();
        InMemoryTokenRepositoryImpl rememberMeTokenRepository = new InMemoryTokenRepositoryImpl();
        PersistentTokenBasedRememberMeServices services = new PersistentTokenBasedRememberMeServices("mdp-server", rememberMeUserDetailsService, rememberMeTokenRepository);
        services.setAlwaysRemember(false);
        return services;
    }

    public class BasicRememberMeUserDetailsService implements UserDetailsService {
        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return new CustomUserDetails(username);
        }
    }

    private class CustomUserDetails implements UserDetails {

        private String username;

        public CustomUserDetails(String username) {
            this.username = username;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public String getUsername() {
            return this.username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

}
