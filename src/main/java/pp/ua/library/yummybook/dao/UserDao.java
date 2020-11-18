package pp.ua.library.yummybook.dao;

import pp.ua.library.yummybook.domain.User;

import java.util.List;

public interface UserDao extends GeneralDAO<User>{
    List<User> findByUsername(String username);
}
