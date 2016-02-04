package es.aalvarez.modelica.util;


import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import org.springframework.security.web.context.*;

public class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {

    public SecurityWebApplicationInitializer() {
        super(SecurityConfig.class);
    }
}