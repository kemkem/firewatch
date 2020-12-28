package net.kprod.firewatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class BasicSecurity extends WebSecurityConfigurerAdapter {
    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private static final String PROPERTY_USER_USERNAME_PATTERN = "firewatch\\.auth\\.user\\..+\\.username";
    private static final String PROPERTY_USER_PREFIX = "firewatch.auth.user.";
    private static final String PROPERTY_USER_PWD_SUFFIX = ".password";
    private static final String PROPERTY_USER_ROLE_SUFFIX = ".role";
    private static final String PROPERTY_SECURED_URL_PATTERN = "firewatch\\.auth\\.secure\\.(.+)\\.url";
    private static final String PROPERTY_SECURED_PREFIX = "firewatch.auth.secure.";
    private static final String PROPERTY_SECURED_URL_SUFFIX = ".url";
    private static final String PROPERTY_SECURED_ROLE_SUFFIX = ".role";
    private static final String ROLES_PATTERN = "\\s*;\\s*";

    @Autowired
    private Environment env;

    @Value("${firewatch.auth.enabled:false}")
    private boolean authEnabled;

    private static Map<String, Object> getAllProperties(Environment env) {
        Map<String, Object> map = new HashMap<>();
        Iterator it = ((AbstractEnvironment)env).getPropertySources().iterator();

        while(it.hasNext()) {
            PropertySource propertySource = (PropertySource)it.next();
            if (propertySource instanceof MapPropertySource) {
                map.putAll((Map)((MapPropertySource)propertySource).getSource());
            }
        }

        return map;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {

        Map<String, Object> mapProperties = this.getAllProperties(env);

        listUsersToInit(mapProperties)
            .forEach(
                username -> {
                    String pwd =
                            env.getProperty(PROPERTY_USER_PREFIX + username + PROPERTY_USER_PWD_SUFFIX);
                    String role =
                            env.getProperty(PROPERTY_USER_PREFIX + username + PROPERTY_USER_ROLE_SUFFIX);
                    try {
                        String[] roles = role.split(ROLES_PATTERN);

                        auth.inMemoryAuthentication()
                                .passwordEncoder(passwordEncoder())
                                .withUser(username)
                                .password(pwd)
                                .roles(roles);
                    } catch (Exception e) {
                        LOG.error("Unable to init user [{}]", username);
                    }
                });
    }

    private List<String> listUsersToInit(Map<String, Object> mapProperties) {
        return mapProperties.entrySet().stream()
                .filter(e -> e.getKey().matches(PROPERTY_USER_USERNAME_PATTERN))
                .map(e -> e.getValue().toString())
                .collect(Collectors.toList());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if(!authEnabled) {
            //auth disabled, authorize everything
            http.authorizeRequests()
                    .anyRequest().permitAll()
                    .and().csrf().disable();
        } else {
            //auth enabled
            Map<String, Object> mapProperties = this.getAllProperties(env);

            //create a secured url for all listed secured url in properties
            listUrlToSecure(mapProperties).forEach(urlToSecure -> {
                try {
                    secureUrl(http, urlToSecure.getUrl(), urlToSecure.getRole());
                } catch (Exception e) {
                    LOG.error("Error securing url [{}] exception : {}", urlToSecure.getUrl(), e.getMessage());
                }
            });

            //by default, accept admin only
            http
                    .authorizeRequests()
                    .anyRequest().hasRole("ADMIN")
                    .and().httpBasic()
                    .and().csrf().disable();
        }
    }

    private void secureUrl(HttpSecurity http, String url, String role) throws Exception {
        //TODO find a better solution when a top level permission takes over a low level permission
        String[] aUrl = url.split(";");

        for (String urlToSecure : aUrl) {
            if (role.equals("ALL")) {
                LOG.debug("Securing url [{}] with role [{}]", urlToSecure, role);
                http.authorizeRequests().antMatchers(aUrl).permitAll();
            } else {
                LOG.debug("Securing url [{}] with role [{}]", urlToSecure, role);
                http.authorizeRequests().antMatchers(aUrl).hasAnyRole(role);
            }
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return encoder;
    }

    private List<UrlToSecure> listUrlToSecure(Map<String, Object> mapProperties) {

        return (List<UrlToSecure>)mapProperties.entrySet().stream()
                .filter(e -> e.getKey().matches(PROPERTY_SECURED_URL_PATTERN))
                .map(e -> {

                    Pattern p = Pattern.compile(PROPERTY_SECURED_URL_PATTERN);
                    Matcher m = p.matcher(e.getKey());

                    if(m.matches()) {
                        String name = m.group(1);
                        String url = env.getProperty(PROPERTY_SECURED_PREFIX + name + PROPERTY_SECURED_URL_SUFFIX);
                        String role = env.getProperty(PROPERTY_SECURED_PREFIX + name + PROPERTY_SECURED_ROLE_SUFFIX);

                        return Optional.of(new UrlToSecure(url, role));
                    }
                    return Optional.empty();
                })
                .filter(Optional::isPresent)
                .map(o->(UrlToSecure)o.get())
                .collect(Collectors.toList());
    }

    class UrlToSecure {
        private String url;
        private String role;

        public UrlToSecure(String url, String role) {
            this.url = url;
            this.role = role;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

}
