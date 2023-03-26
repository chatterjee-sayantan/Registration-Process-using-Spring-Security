package com.sayantan.springsecuritydemo.event;

import com.sayantan.springsecuritydemo.entity.User;
import com.sayantan.springsecuritydemo.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class ResgistrationCompleteEventListner implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

        //Here we will create the verification token for the user and attach to the link
        User user=event.getUser();
        String token= UUID.randomUUID().toString();      //we need to save this token in our database for each user. So whenever a user hits the url for reset password we can match this token in the database.
                                                        // Hence we need a separate table for storing the verification tokens in the database. So we need to create a corresponding entity class for the same.
        userService.saveVerificationTokenForUser(token,user);


        //Send mail to user (Here we are not actually sending the email. We are just mimicking to send the url to email.)
        //Instead we are printing the url in the console and then using it from there (for development purposes)

        String url=event.getApplicationUrl()
                +"/verifyRegistration?token="
                +token;

        //sendVerificationEmail() needs to be implemented here. We are just mimicking the concept and printing the link in the console using log.info()
        log.info("Click the link to verify your account : {}",url);
    }
}
