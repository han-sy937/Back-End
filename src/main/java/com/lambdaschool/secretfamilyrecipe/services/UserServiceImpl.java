package com.lambdaschool.secretfamilyrecipe.services;

import com.lambdaschool.secretfamilyrecipe.exceptions.ResourceNotFoundException;
import com.lambdaschool.secretfamilyrecipe.models.Role;
import com.lambdaschool.secretfamilyrecipe.models.User;
import com.lambdaschool.secretfamilyrecipe.models.UserRoles;

import com.lambdaschool.secretfamilyrecipe.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Transactional
@Service(value = "userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userrepos;

    @Autowired
    private RoleService roleService;

    @Autowired
    private HelperFunctions helperFunctions;

    public User findUserById(long id) throws ResourceNotFoundException {
        return userrepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + id + " not found"));
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        userrepos.findAll()
                .iterator()
                .forEachRemaining(list::add);
        return list;
    }

    @Override
    public List<User> findByNameContaining(String username) {
        return null;
    }

    @Override
    public User findByName(String name) {
        User uu = userrepos.findByUsername(name.toLowerCase());
        if (uu == null) {
            throw new ResourceNotFoundException("User name " + name + " not found!");
        }
        return uu;
    }

    @Transactional
    @Override
    public void delete(long id) {
        userrepos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User id " + id + " not found!"));
        userrepos.deleteById(id);
    }

    @Override
    public User save(User user) {
        User newUser = new User();
        if (user.getUserid() != 0) {
            userrepos.findById(user.getUserid())
                    .orElseThrow(() -> new ResourceNotFoundException("User id " + user.getUserid() + " not found!"));
            newUser.setUserid(user.getUserid());
        }

        newUser.setUsername(user.getUsername()
                .toLowerCase());
        newUser.setEmail(user.getEmail()
                .toLowerCase());
        newUser.setPasswordNoEncrypt(user.getPassword());

        newUser.getRoles()
                .clear();
        for (UserRoles ur : user.getRoles()) {
            Role addRole = roleService.findRoleById(ur.getRole()
                    .getRoleid());
            newUser.getRoles()
                    .add(new UserRoles(newUser, addRole));
        }

        // recipe code need to be add later

        return userrepos.save(newUser);
    }

    @Transactional
    @Override
    public User update(User user, long id) {
        User currentUser = findUserById(id);

        if (helperFunctions.inAuthorizedToMakeChanges(currentUser.getUsername())) {
            if (user.getUsername() != null) {
                currentUser.setUsername(user.getUsername().toLowerCase());
            }
            if (user.getEmail() != null) {
                currentUser.setEmail(user.getEmail().toLowerCase());
            }
            if (user.getPassword() != null) {
                currentUser.setPasswordNoEncrypt(user.getPassword());
            }
            if (user.getRoles().size() > 0) {
                currentUser.getRoles().clear();
                for (UserRoles ur : user.getRoles()) {
                    Role addRole = roleService.findRoleById(ur.getRole().getRoleid());
                    currentUser.getRoles().add(new UserRoles(currentUser, addRole));
                }
            }
            //add recipe code later

            return userrepos.save(currentUser);
        } else {
            throw
                    new ResourceNotFoundException("This user is not authorized to make changes");
        }
    }

    @Transactional
    @Override
    public void deleteAll() {
        userrepos.deleteAll();
    }
}
