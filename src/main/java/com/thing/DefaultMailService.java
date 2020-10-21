package com.thing;

public class DefaultMailService implements MailService{
    private int port;
    private String protocol;

    @Override
    public void sendEmail(User user, String message) {
        System.out.println("sending email to " + user + " with message: " + message);
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }
}
