package edu.northeastern.cs5500.starterbot.controller;

import edu.northeastern.cs5500.starterbot.model.User;
import edu.northeastern.cs5500.starterbot.repository.GenericRepository;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.bson.types.ObjectId;

@Singleton
public class UserController {
    GenericRepository<User> userRepository;

    @Inject
    public UserController(GenericRepository<User> userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(ObjectId userId) {
        return userRepository.get(userId);
    }

    public User addUser(User user) {
        return userRepository.add(user);
    }
}
