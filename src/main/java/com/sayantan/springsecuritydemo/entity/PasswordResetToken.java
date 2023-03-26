package com.sayantan.springsecuritydemo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;


//Copy everything from verification token entity class. Exactly same steps for creating password reset token.
@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {

    //Expiration time of 10minutes for each verificationToken
    private static final int EXPIRATION_TIME = 10;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String token;
    private Date expirationTime; //when a particular token will be expired. about 10-15min.

    @OneToOne
    @JoinColumn(                                          //creating the One to One mapping. Please refer to JPA Relationships for thi.
            name="user_id",
            nullable=false,
            foreignKey = @ForeignKey(name = "FK_USER_PASSWORD_TOKEN")
    )
    private User user;       //for each particular user, a unique token will be stored in the database. Hence we need to do one to one mapping the database with the User table and the PasswordResetToken Table

    public PasswordResetToken(User user,String token){               //Here we are not using the lombok annotations for COnstructore and we want to define the constructor by yourself
        super();
        this.user=user;
        this.token=token;
        this.expirationTime=calculateExpirationTime(EXPIRATION_TIME);     //we are adding a method whose return value will be treated as the value assigned to the expirationTime field;
    }

    private Date calculateExpirationTime(int expirationTime) {
        Calendar calendar=Calendar.getInstance();          //we are creating and instance of the current calendar object that has the current time.
        calendar.setTimeInMillis(new Date().getTime());

        calendar.add(Calendar.MINUTE,expirationTime);     //we are adding the expiration time of 10minutes to thr current time.

        return new Date(calendar.getTime().getTime());    //we are sending currenttime+10minutes to the expirationTime variable.
    }


}
