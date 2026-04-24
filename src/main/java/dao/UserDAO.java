package dao;

import models.entities.User;
import models.entities.Role;

public interface UserDAO extends DAO<User, Integer> {
    User findByLogin(String login) throws Exception;
    Role findRoleById(int id) throws Exception;
}