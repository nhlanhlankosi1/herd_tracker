package com.nhlanhlankosi.tablayoutdemo.models;

public class User {
    private String userId;
    private String userName;
    private String email;
    private String profilePicUrl;

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    private String fcmToken;

    public User() {

    }
    public User(String userId, String userName, String email, String profilePicUrl, String fcmToken) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.profilePicUrl = profilePicUrl;
        this.fcmToken = fcmToken;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

}
