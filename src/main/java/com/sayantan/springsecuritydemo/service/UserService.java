package com.sayantan.springsecuritydemo.service;

import com.sayantan.springsecuritydemo.entity.User;
import com.sayantan.springsecuritydemo.entity.VerificationToken;
import com.sayantan.springsecuritydemo.model.UserModel;

import java.util.Optional;

public interface UserService {

    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String newToken);

    String validatePasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);

    void changePassword(User user, String newPassword);

    boolean checkIfValidOldPassword(User user, String oldPassword);
}
