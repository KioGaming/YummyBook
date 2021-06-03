package com.yummybook.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery(
                        "select username, password, enabled from library.user where username = ?")
                .authoritiesByUsernameQuery(
                        "select username, role from library.user_roles where username = ?").passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/**").permitAll()

                .antMatchers("/saveBook").hasAuthority("ADMIN")
                .antMatchers("/deleteBook").hasAuthority("ADMIN")

                .antMatchers("/voting").hasAnyAuthority("ADMIN", "USER")

                .antMatchers("/author").hasAuthority("ADMIN")
                .antMatchers("/saveAuthor").hasAuthority("ADMIN")
                .antMatchers("/deleteAuthor").hasAuthority("ADMIN")

                .antMatchers("/genre").hasAuthority("ADMIN")
                .antMatchers("/saveGenre").hasAuthority("ADMIN")
                .antMatchers("/deleteGenre").hasAuthority("ADMIN")

                .antMatchers("/publisher").hasAuthority("ADMIN")
                .antMatchers("/savePublisher").hasAuthority("ADMIN")
                .antMatchers("/deletePublisher").hasAuthority("ADMIN")

                .antMatchers("/registration").not().authenticated()
                .antMatchers("/login").not().authenticated()
                .antMatchers("/signUp").not().authenticated()
                .antMatchers("/signIn").not().authenticated()
                .antMatchers("/signUpError").not().authenticated()
                .antMatchers("/signInError").not().authenticated()
                .antMatchers("/logout").authenticated()

                .and()

                .exceptionHandling().accessDeniedPage("/error")

                .and()

                .csrf().disable()
                .formLogin()
                .loginPage("/signIn")
                .failureUrl("/signInError")
                .defaultSuccessUrl("/book")
                .passwordParameter("password")
                .usernameParameter("username")

                .and()

                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/book")
                .deleteCookies("JSESSIONID", "SPRING_SECURITY_REMEMBER_ME_COOKIE")
                .invalidateHttpSession(true);
    }
}