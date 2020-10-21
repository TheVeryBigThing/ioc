package com.thing;

import java.util.ArrayList;
import java.util.List;

public class DefaultUserService implements UserService{
    private MailService mailService;

    @Override
    public void activateUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User("John"));
        users.add(new User("Scarlett"));
        users.add(new User("Jane"));

        for (User user : users) {
            mailService.sendEmail(user, "You are active now");
        }

    }

    public void setMailService(MailService mailService) {
        this.mailService = mailService;
    }

    public MailService getMailService() {
        return mailService;
    }
}
