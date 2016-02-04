package es.aalvarez.modelica.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .inMemoryAuthentication()
                .withUser("tt").password("tt").roles("USER").and()
                .withUser("vv").password("vv").roles("CONSULTA").and()
                .withUser("e").password("e").roles("GESTOR").and()
    			.withUser("f").password("f").roles("GESTOR", "ADMIN", "SUPERADMIN");
        
    }
    
    protected void configure(HttpSecurity http) throws Exception {
        http
        	.headers().frameOptions().sameOrigin().and()
        	.csrf().disable()
        	.authorizeRequests()
            	.antMatchers("/resources/**").permitAll()
            	.antMatchers("/javax.faces.resource/**").permitAll()
            	.antMatchers("/WEB-INF/template/**").permitAll()
            	.antMatchers("/index.xhtml").permitAll()
            	.anyRequest().authenticated()                                                   
		        .and()
			// ...
			.formLogin();
        
			
            	        	
            	        	 		
            	
        	
    }
}