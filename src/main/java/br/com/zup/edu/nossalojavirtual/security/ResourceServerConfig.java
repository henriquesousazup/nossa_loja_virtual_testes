package br.com.zup.edu.nossalojavirtual.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class ResourceServerConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                    .csrf().disable()
                    .httpBasic().disable()
                    .rememberMe().disable()
                    .formLogin().disable()
                    .logout().disable()
                    .headers().frameOptions().deny()
                .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .authorizeRequests()
                        .antMatchers(HttpMethod.POST, "/api/purchase").hasAuthority("SCOPE_lojavirtual:write")
                        .antMatchers(HttpMethod.POST, "/api/purchases/confirm-payment").hasAuthority("SCOPE_lojavirtual:write")
                        .antMatchers(HttpMethod.POST, "/api/categories").hasAuthority("SCOPE_lojavirtual:write")
                        .antMatchers(HttpMethod.POST, "/api/products").hasAuthority("SCOPE_lojavirtual:write")
                        .antMatchers(HttpMethod.POST, "/api/products/{\\d+}/questions").hasAuthority("SCOPE_lojavirtual:write")
                        .antMatchers(HttpMethod.GET, "/api/products/{\\d+}").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/opinions").hasAuthority("SCOPE_lojavirtual:write")
                        .antMatchers(HttpMethod.POST, "/invoice/register").hasAuthority("SCOPE_lojavirtual:write")
                        .antMatchers(HttpMethod.POST, "/sellerRanking/newPurchase").hasAuthority("SCOPE_lojavirtual:write")
                        .antMatchers(HttpMethod.POST, "/api/users").permitAll()
                    .anyRequest()
                        .authenticated()
                .and()
                    .oauth2ResourceServer()
                        .jwt( jwt -> jwt.jwkSetUri("http://localhost:18080/realms/loja-virtual/protocol/openid-connect/certs"));
    }

}
