package pp.ua.library.yummybook.spring.controller;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pp.ua.library.yummybook.dao.UserDao;
import pp.ua.library.yummybook.dao.UserRoleDao;
import pp.ua.library.yummybook.domain.User;
import pp.ua.library.yummybook.domain.UserRole;

import java.util.Optional;

@Controller
@Log
public class UserController {

    @Autowired
    UserDao userDao;
    @Autowired
    UserRoleDao userRoleDao;
    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(value = {"/index"})
    public String index() {
        return "index";
    }

    @RequestMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "index";
    }

    @PreAuthorize("!isAuthenticated()")
    @RequestMapping(value = {"/singUp"})
    public String singUp() {
        return "singUpPage";
    }

    @PreAuthorize("!isAuthenticated()")
    @RequestMapping(value = {"/registration"})
    public String registration(@RequestParam("username") Optional<String> username, @RequestParam("password") Optional<String> password, Model model) {
        if (password.isPresent() && username.isPresent() && userDao.findByUsername(username.get()) == null) {
            userDao.save(new User(username.get(), passwordEncoder.encode(password.get())));
            userRoleDao.save(new UserRole(username.get()));
            model.addAttribute("loginAfterRegistration", true);
            return "index";
        } else {
            model.addAttribute("loginError", true);
            return "singUpPage";
        }
    }
}
