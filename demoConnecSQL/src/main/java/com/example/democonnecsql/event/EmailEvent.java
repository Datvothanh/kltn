package com.example.democonnecsql.event;

import com.example.democonnecsql.entity.User;
import com.example.democonnecsql.entity.VerificationToken;
import org.springframework.context.ApplicationEvent;


import com.example.democonnecsql.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Setter
@Getter
public class EmailEvent extends ApplicationEvent {

    private final User user;
    private final String applicationUrl;

    public EmailEvent(User user , String applicationUrl) {
        super(user);
        this.user = user;
        this.applicationUrl = applicationUrl;

    }
}
