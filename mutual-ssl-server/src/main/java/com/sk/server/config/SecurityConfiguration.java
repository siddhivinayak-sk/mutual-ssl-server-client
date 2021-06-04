package com.sk.server.config;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Value("#{'${cn.usernames}'.split(',')}")
	private List<String> cnUsernames;
	
	@Value("${security.require-ssl}")
	private boolean isHttpsEnabled;
	
    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	if(!isHttpsEnabled) {
    		 http
	          .authorizeRequests()
	          .anyRequest()
	          .permitAll()
	          .and()
	          .sessionManagement()
	          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	          .and()
	          .csrf()
	          .disable();
    	}else {
	        http
	          .authorizeRequests()
	          .antMatchers("/poll")
	          .permitAll()
	          .anyRequest()
	          .authenticated()
	          .and()
	          .sessionManagement()
	          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	          .and()
	          .csrf()
	          .disable()
	          .x509()
	          .subjectPrincipalRegex("CN=(.*?)(?:,|$)")
	          .userDetailsService(userDetailsService());
    	}
    }
              
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return (UserDetailsService) username -> {
            if (cnUsernames.stream().anyMatch(cnUsername -> cnUsername.trim().equals(username.trim()))) {
                return new User(username, "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
            } else {
                throw new UsernameNotFoundException(String.format("User %s not found", username));
            }
        };
    }
}
