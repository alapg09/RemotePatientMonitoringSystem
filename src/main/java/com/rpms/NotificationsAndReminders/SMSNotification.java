package com.rpms.NotificationsAndReminders;

public class SMSNotification implements Notifiable{
    // every sms object will have these two properties
    private String phoneNumber;
    private String message;

    public SMSNotification(String phoneNumber, String message) {
        this.phoneNumber = phoneNumber;
        this.message = message;
    }
    // getters and setters for phoneNumber and message
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    // implementing the snedNotification method from the Notifiable interface
    @Override
    public void sendNotification() {
        System.out.println("SMS sent to " + phoneNumber + ": " + message);
    }

}
