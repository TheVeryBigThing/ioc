package com.thing.service;

import com.thing.entity.User;

public interface MailService {

    void sendEmail(User user, String massage);
}
