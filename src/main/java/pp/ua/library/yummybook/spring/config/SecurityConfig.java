package pp.ua.library.yummybook.spring.config;

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
                        "select username,password,enabled from library.user where username = ?")
                .authoritiesByUsernameQuery(
                        "select username, role from library.user_roles where username = ?").passwordEncoder(passwordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/**").permitAll()
                .antMatchers("/deleteBook").hasRole("ADMIN")
                .antMatchers("/editBook").hasRole("ADMIN")
                .antMatchers("/addBook").hasRole("ADMIN")
                .antMatchers("/saveBook").hasRole("ADMIN")
                .antMatchers("/catalogs").hasRole("ADMIN")
                .antMatchers("/deleteAuthor").hasRole("ADMIN")
                .antMatchers("/deleteGenre").hasRole("ADMIN")
                .antMatchers("/deletePublisher").hasRole("ADMIN")
                .antMatchers("/editAuthor").hasRole("ADMIN")
                .antMatchers("/editGenre").hasRole("ADMIN")
                .antMatchers("/editPublisher").hasRole("ADMIN")
                .antMatchers("/saveAuthor").hasRole("ADMIN")
                .antMatchers("/saveGenre").hasRole("ADMIN")
                .antMatchers("/savePublisher").hasRole("ADMIN")

                .antMatchers("/getBook").hasAnyRole("ADMIN", "USER")
                .antMatchers("/openBook").hasAnyRole("ADMIN", "USER")
                .antMatchers("/voting").hasAnyRole("ADMIN", "USER")

                .and()

                .exceptionHandling().accessDeniedPage("/booksPage")

                .and()

                .csrf().disable()

                .formLogin()
                .loginPage("/index")
                .failureUrl("/login-error")
                .defaultSuccessUrl("/booksPage")
                .passwordParameter("password")
                .usernameParameter("username")
                .and()

                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/index")
                .deleteCookies("JSESSIONID", "SPRING_SECURITY_REMEMBER_ME_COOKIE")
                .invalidateHttpSession(true);
    }
}