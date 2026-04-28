package services;

import dao.UserDAO;
import dao.impl.UserDAOImpl;
import enums.Roles;
import models.entities.User;
import utility.PasswordHasher;
import utility.RoleChecker;

import java.util.List;

public class UserService implements Service<User, Integer> {
    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public User findEntity(Integer id) throws Exception {
        return userDAO.findById(id);
    }

    @Override
    public void saveEntity(User entity) throws Exception {

        String plainPassword = entity.getPasswordHash();
        if (plainPassword != null && !plainPassword.isEmpty()) {
            entity.setPasswordHash(PasswordHasher.hash(plainPassword));
        } else {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        userDAO.create(entity);
    }

    @Override
    public void deleteEntity(Integer id) throws Exception {
        userDAO.delete(id);
    }

    @Override
    public void updateEntity(User entity) throws Exception {
        User existing = userDAO.findById(entity.getId());
        if (existing == null) throw new IllegalArgumentException("User not found");
        if (entity.getPasswordHash() != null && !entity.getPasswordHash().isEmpty()) {
            entity.setPasswordHash(PasswordHasher.hash(entity.getPasswordHash()));
        } else {
            entity.setPasswordHash(existing.getPasswordHash());
        }
        userDAO.update(entity);
    }

    @Override
    public List<User> findAllEntities() throws Exception {
        return userDAO.findAll();
    }

    public User login(String login, String password) throws Exception {
        User user = userDAO.findByLogin(login);
        if (user != null && PasswordHasher.hash(password).equals(user.getPasswordHash()) && "active".equals(user.getStatus())) {
            return user;
        }
        return null;
    }

    public User registerUser(User registrant, String login, String password, int roleId) throws Exception {
        if (!RoleChecker.canRegisterUsers(registrant)) {
            throw new SecurityException("Insufficient permissions to register users");
        }

        if (roleId == Roles.ADMIN.getLevel()) {
            throw new SecurityException("Admin role assignment is restricted");
        }

        if (roleId < 1 || roleId > Roles.values().length) {
            throw new IllegalArgumentException("Invalid role ID");
        }

        if (roleId >= registrant.getRoleId() && !RoleChecker.hasRole(registrant, Roles.ADMIN)) {
            throw new SecurityException("Cannot assign role with higher or equal privilege");
        }

        if (userDAO.findByLogin(login) != null) {
            throw new IllegalArgumentException("Login already exists");
        }

        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPasswordHash(password);
        newUser.setRoleId(roleId);
        newUser.setStatus("active");
        userDAO.create(newUser);
        return newUser;
    }

    public List<User> getUsersForManager(User manager) throws Exception {
        if (!RoleChecker.canManageUsers(manager) && !RoleChecker.canRegisterUsers(manager)) {
            throw new SecurityException("Insufficient permissions");
        }
        List<User> allUsers = userDAO.findAll();
        if (RoleChecker.hasRole(manager, Roles.ADMIN)) {
            return allUsers;
        }
        allUsers.removeIf(u -> u.getRoleId() >= manager.getRoleId() && u.getId() != manager.getId());
        return allUsers;
    }
}