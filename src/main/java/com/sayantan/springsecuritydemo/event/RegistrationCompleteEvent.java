package com.sayantan.springsecuritydemo.event;

import com.sayantan.springsecuritydemo.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class RegistrationCompleteEvent extends ApplicationEvent {

    private User user;
    private String applicationUrl;           //this is the url what we need to create/generate and send to the user for changing their password over the mail.

    public RegistrationCompleteEvent(User user, String applicationUrl) {
        super(user);
        this.user=user;
        this.applicationUrl=applicationUrl;
    }
}
