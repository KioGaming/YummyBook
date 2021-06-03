package com.yummybook.dao;

import com.yummybook.domain.User;

public interface UserDao extends GeneralDAO<User>{
    User findByUsername(String username);
}
