package com.example.tutti.user;

import java.util.List;

public interface UserService {
    UserResponse getUserById(Long userId);
    UserResponse getUserByEmail(String email);
    UserResponse createUser(UserRequest request);
    UserResponse updateUser(Long userId, UserRequest request);
    void deleteUser(Long userId);
    List<UserResponse> getAllUsers();
    void changePassword(Long userId, ChangePasswordRequest request);
}