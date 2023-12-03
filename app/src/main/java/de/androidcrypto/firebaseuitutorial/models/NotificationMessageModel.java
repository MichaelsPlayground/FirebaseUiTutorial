package de.androidcrypto.firebaseuitutorial.models;

public class NotificationMessageModel {
    private String chatroomId;
    private String userId;
    private String userName;
    private String userEmail;
    private String userProfileImage;
    private long chatLastTime;

    public NotificationMessageModel() {
    }

    public NotificationMessageModel(String chatroomId, String userId, String userName, String userEmail, String userProfileImage, long chatLastTime) {
        this.chatroomId = chatroomId;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userProfileImage = userProfileImage;
        this.chatLastTime = chatLastTime;
    }

    public String getChatroomId() {
        return chatroomId;
    }
    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
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

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserProfileImage() {
        return userProfileImage;
    }

    public void setUserProfileImage(String userProfileImage) {
        this.userProfileImage = userProfileImage;
    }

    public long getChatLastTime() {
        return chatLastTime;
    }

    public void setChatLastTime(long chatLastTime) {
        this.chatLastTime = chatLastTime;
    }
}
