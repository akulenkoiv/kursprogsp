package services;

import dao.UserDAO;
import dao.impl.UserDAOImpl;
import models.entities.User;
import utility.PasswordHasher;

import java.util.List;

public class UserService implements Service<User, Integer> {
    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public User findEntity(Integer id) throws Exception {
        return userDAO.findById(id);
    }

    @Override
    public void saveEntity(User entity) throws Exception {
        entity.setPasswordHash(PasswordHasher.hash(entity.getPasswordHash()));
        userDAO.create(entity);
    }

    @Override
    public void deleteEntity(Integer id) throws Exception {
        userDAO.delete(id);
    }

    @Override
    public void updateEntity(User entity) throws Exception {
        userDAO.update(entity);
    }

    @Override
    public List<User> findAllEntities() throws Exception {
        return userDAO.findAll();
    }

    public User login(String login, String password) throws Exception {
        User user = userDAO.findByLogin(login);
        if (user != null && user.getPasswordHash().equals(PasswordHasher.hash(password))) {
            return user;
        }
        return null;
    }
}