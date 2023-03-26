package com.sayantan.springsecuritydemo.controller;

import com.sayantan.springsecuritydemo.entity.User;
import com.sayantan.springsecuritydemo.entity.VerificationToken;
import com.sayantan.springsecuritydemo.event.RegistrationCompleteEvent;
import com.sayantan.springsecuritydemo.model.PasswordModel;
import com.sayantan.springsecuritydemo.model.UserModel;
import com.sayantan.springsecuritydemo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;


@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    //API for Registering the User into our System
    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user,
                applicationUrl(request)
        ));
        return "Success";
    }

    //API for sending Verify Account link over mail. On clicking this link the user will verify his account and Enabled FLag will be set as true (i.e, Active User)
    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam String token){        //taking the verification token as request parameter ( ?... )

        String result=userService.validateVerificationToken(token);      //pass the token that is there in the expiration link to the Service layer method
        if(result.equalsIgnoreCase("valid")){
            return "Account has been VERIFIED";
        }
        return "BAD USER - Account not VERIFIED";
    }


    //API for resending the verification link over mail to the User
    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken,
                                          HttpServletRequest request) {
        VerificationToken verificationToken
                = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";
    }


    //API for sending link for Resetting the Password. On clicking that link "/savePassword" API will be called which is coded after this method.
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request){

        String url="";
        //User would be passing the email id using which they registered. We'll validate if that particular email is present in our system.
        //If email present, then using that email id we will be creating a new password token and we'll send that token to the user over email.
        User user=userService.findUserByEmail(passwordModel.getEmail());
        if(user!=null){
            String newToken = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,newToken);
            url=passwordResetTokenMail(user,applicationUrl(request),newToken);
        }
        return url;            //User will get a link. Upon clicking that link he will be redirected to another API for saving the password.
                               //We need to create another API for saving the password. SEE BELOW
    }


    //API for resetting the password and saving that in the system.
    @PostMapping("/savePassword")
    public String savePassword(@RequestParam String token,
                               @RequestBody PasswordModel passwordModel) {

        String result = userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")){
            return "EXPIRED TOKEN";
        }

        Optional<User> user=userService.getUserByPasswordResetToken(token);

        if(user.isPresent()){
            userService.changePassword(user.get(),passwordModel.getNewPassword());
            return "PASSWORD RESET SUCCESSFUL";
        }else{
            return "INVALID TOKEN";
        }
    }


    //API for changing the password
    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel){
        User user = userService.findUserByEmail(passwordModel.getEmail());
        if(!userService.checkIfValidOldPassword(user,passwordModel.getOldPassword())) {
            return "Invalid Old Password";
        }
        //Save New Password
        userService.changePassword(user,passwordModel.getNewPassword());
        return "Password Changed Successfully";
    }


    //Method to generate the URL that we need to send over mail
    private String applicationUrl(HttpServletRequest request) {
        return "http://"
                +request.getServerName()
                +":"
                +request.getServerPort()
                +request.getContextPath();
    }



    //These below 2 methods are just the mocking of the Email send functionality. For development purposes we are doing it like this.
    //You need to integrate a Third Party Email sender Open API. Or use SpirngBoot Email sender to send these emails. (Check Videos)
    private String passwordResetTokenMail(User user, String applicationUrl, String newToken) {
        String url =
                applicationUrl
                        + "/savePassword?token="
                        + newToken;

        //sendVerificationEmail()
        log.info("Click the link to reset your password : {}",
                url);

        return url;
    }

    private void resendVerificationTokenMail(User user, String applicationUrl, VerificationToken verificationToken) {
        String url =
                applicationUrl
                        + "/verifyRegistration?token="
                        + verificationToken.getToken();

        //sendVerificationEmail()
        log.info("Click the link to verify your account: {}",
                url);
    }

}
