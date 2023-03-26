package com.sayantan.springsecuritydemo.service;

import ch.qos.logback.core.encoder.EchoEncoder;
import com.sayantan.springsecuritydemo.entity.PasswordResetToken;
import com.sayantan.springsecuritydemo.entity.User;
import com.sayantan.springsecuritydemo.entity.VerificationToken;
import com.sayantan.springsecuritydemo.model.UserModel;
import com.sayantan.springsecuritydemo.repository.PasswordResetRepository;
import com.sayantan.springsecuritydemo.repository.UserRepository;
import com.sayantan.springsecuritydemo.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(11);
    }//injecting the dependency here that we created in the WebConfig file.

    @Override
    public User registerUser(UserModel userModel) {
        User user=new User();
        user.setEmail(userModel.getEmail());                        //creating an entity user object using the userModel object values that are being sent by the client as request Body
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");                                      //static as of now, you can change this to take dynamic values as well.

        user.setPassword(passwordEncoder().encode(userModel.getPassword()));             //encoding the password entered by user and then storing this encoded password in the database

        userRepository.save(user);

        return user;

    }

    @Override
    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken=new VerificationToken(user,token);
        verificationTokenRepository.save(verificationToken);

    }

    @Override
    public String validateVerificationToken(String token) {       //taking the token from the controller

        VerificationToken verificationToken=verificationTokenRepository.findByToken(token);     //looking for the token in the database

        if(verificationToken==null){
            return "INVALID TOKEN";
        }

        User user=verificationToken.getUser();    //taking the user from the VerificationToken class against whom the token has been matched

        Calendar calendar=Calendar.getInstance();

        if(((verificationToken.getExpirationTime().getTime())-(calendar.getTime().getTime()))<=0){     //checking if the token has been expired or not
            verificationTokenRepository.delete(verificationToken);
            return "TOKEN HAS EXPIRED";
        }

        user.setEnabled(true);             //if token validated, set the enabled flag as true. That means the user has been verified in the system
        userRepository.save(user);         //save back the updated user back to the user table

        return "Valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken
                = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String newToken) {

        //For Password Reset token creation follow the same steps as creation of verification token in the first place. (create entity and all)
        PasswordResetToken passwordResetToken=new PasswordResetToken(user,newToken);
        passwordResetRepository.save(passwordResetToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {

        PasswordResetToken passwordResetToken=passwordResetRepository.findByToken(token);     //looking for the token in the database

        if(passwordResetToken==null){
            return "INVALID TOKEN";
        }

        User user=passwordResetToken.getUser();    //taking the user from the VerificationToken class against whom the token has been matched

        Calendar calendar=Calendar.getInstance();

        if(((passwordResetToken.getExpirationTime().getTime())-(calendar.getTime().getTime()))<=0){     //checking if the token has been expired or not
            passwordResetRepository.delete(passwordResetToken);
            return "TOKEN HAS EXPIRED";
        }

        return "Valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetRepository.findByToken(token).getUser());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder().encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder().matches(oldPassword, user.getPassword());
    }
}
