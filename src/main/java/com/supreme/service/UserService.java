package com.supreme.service;

import com.supreme.model.User;

import java.util.List;

public interface UserService {

    public User getUserProfile(String jwt);

    public List<User> getAllUsers();

}
