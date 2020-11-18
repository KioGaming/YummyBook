package pp.ua.library.yummybook.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pp.ua.library.yummybook.dao.UserRoleDao;
import pp.ua.library.yummybook.domain.UserRole;
import pp.ua.library.yummybook.spring.repository.UserRoleRepository;

import java.util.List;

@Service
@Transactional
public class UserRoleService implements UserRoleDao {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Override
    public List<UserRole> getAll() {
        return null;
    }

    @Override
    public List<UserRole> search(String... searchString) {
        return null;
    }

    @Override
    public UserRole get(long id) {
        return null;
    }

    @Override
    public UserRole save(UserRole userRole) {
        return userRoleRepository.save(userRole);
    }

    @Override
    public void delete(UserRole object) {

    }

    @Override
    public List<UserRole> getAll(Sort sort) {
        return null;
    }

    @Override
    public Page<UserRole> getAll(int pageNumber, int pageSize, String sortField, Sort.Direction sortDirection) {
        return null;
    }

    @Override
    public Page<UserRole> search(int pageNumber, int pageSize, String sortField, Sort.Direction sortDirection, String... searchString) {
        return null;
    }

}
