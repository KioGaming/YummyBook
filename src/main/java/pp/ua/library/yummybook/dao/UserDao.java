package pp.ua.library.yummybook.dao;

import pp.ua.library.yummybook.domain.User;

public interface UserDao extends GeneralDAO<User>{
    User findByUsername(String username);
}
