package com.yummybook.spring.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.yummybook.dao.UserDao;
import com.yummybook.dao.UserRoleDao;
import com.yummybook.domain.User;
import com.yummybook.domain.UserRole;

import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Optional;
import java.util.logging.Level;


@Controller
@Log
public class UserController {

    @Autowired
    UserDao userDao;
    @Autowired
    UserRoleDao userRoleDao;
    @Autowired
    PasswordEncoder passwordEncoder;

    @PreAuthorize("!isAuthenticated()")
    @RequestMapping(value = {"/registration"}, method = RequestMethod.GET)
    public String registration() {
        return "signUp";
    }

    @PreAuthorize("!isAuthenticated()")
    @RequestMapping(value = {"/login"}, method = RequestMethod.GET)
    public String login() {
        return "signIn";
    }

    @PreAuthorize("!isAuthenticated()")
    @RequestMapping(value = {"/signUp"}, method = RequestMethod.POST)
    public String signUp(@RequestParam("username") @Min(3) @Max(15) Optional<String> username,
                         @RequestParam("mail") @Email Optional<String> mail,
                         @RequestParam("password") @Min(8) @Max(20) Optional<String> password,
                         @RequestParam("passwordRepeat") @Min(8) @Max(20) Optional<String> passwordRepeat) {
        if (username.isPresent() && mail.isPresent() && password.isPresent() && passwordRepeat.isPresent() && password.get().equals(passwordRepeat.get()) && userDao.findByUsername(username.get()) == null) {
            userDao.save(new User(username.get(), mail.get(), passwordEncoder.encode(password.get())));
            userRoleDao.save(new UserRole(username.get()));
            return "signIn";
        } else {
            log.log(Level.ALL, "Unable to sing up new User because params aren't valid");
            return "forward:/signUpError";
        }
    }

    @PreAuthorize("!isAuthenticated()")
    @RequestMapping("/signUpError")
    public String signUpError(Model model) {
        model.addAttribute("signUpError", true);
        return "signUp";
    }

    @PreAuthorize("!isAuthenticated()")
    @RequestMapping("/signInError")
    public String signInError(Model model) {
        model.addAttribute("signInError", true);
        return "signIn";
    }
}