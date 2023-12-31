package de.androidcrypto.firebaseuitutorial.models;

import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class UserModel {

    private String userName, userMail, userId, userPhotoUrl, recentMessage, about="null", deviceToken="null", userPublicKey;
    private long  recentMsgTime;
    private int userPublicKeyNumber;
    private boolean userOnline;
    private String userOnlineString;
    private long userLastOnlineTime;

    public UserModel() {
    }

    public UserModel(String userId, String userName, String userMail, String userPhotoUrl) {
        this.userName = userName;
        this.userMail = userMail;
        this.userId = userId;
        this.userPhotoUrl = userPhotoUrl;
    }

    public UserModel(String userId, String userName, String userMail, String userPhotoUrl, String userPublicKey, int userPublicKeyNumber, String userOnlineString, long userLastOnlineTime) {
        this.userId = userId;
        this.userName = userName;
        this.userMail = userMail;
        this.userPhotoUrl = userPhotoUrl;
        this.userPublicKey = userPublicKey;
        this.userPublicKeyNumber = userPublicKeyNumber;
        this.userOnlineString = userOnlineString;
        this.userLastOnlineTime = userLastOnlineTime;
    }

    // for first storage
    public UserModel(String userId, String userName, String userMail, String userPhotoUrl, String userPublicKey, int userPublicKeyNumber) {
        this.userId = userId;
        this.userName = userName;
        this.userMail = userMail;
        this.userPhotoUrl = userPhotoUrl;
        this.userPublicKey = userPublicKey;
        this.userPublicKeyNumber = userPublicKeyNumber;
        this.userOnlineString = "offline"; // default
    }

    public UserModel(String userId, String userName, String userMail, String userPhotoUrl, String userPublicKey, int userPublicKeyNumber, String onlineStatus) {
        this.userId = userId;
        this.userName = userName;
        this.userMail = userMail;
        this.userPhotoUrl = userPhotoUrl;
        this.userPublicKey = userPublicKey;
        this.userPublicKeyNumber = userPublicKeyNumber;
        this.userOnlineString = onlineStatus;
    }

    // for secure messaging
    public UserModel(String userName, String userMail, String userPhotoUrl, String userPublicKey, int userPublicKeyNumber) {
        this.userName = userName;
        this.userMail = userMail;
        this.userPhotoUrl = userPhotoUrl;
        this.userPublicKey = userPublicKey;
        this.userPublicKeyNumber = userPublicKeyNumber;
    }

    // for signUp
    public UserModel(String userId, String userName, String userMail, String userPhotoUrl, String userPublicKey, int userPublicKeyNumber, boolean userIsOnline) {
        this.userId = userId;
        this.userName = userName;
        this.userMail = userMail;
        this.userPhotoUrl = userPhotoUrl;
        this.userPublicKey = userPublicKey;
        this.userPublicKeyNumber = userPublicKeyNumber;
        this.userOnline = userIsOnline;
    }

    // for Presence


    public UserModel(String userName, String userMail, String userId, String userPhotoUrl, boolean userOnline, String userOnlineString, long userLastOnlineTime) {
        this.userName = userName;
        this.userMail = userMail;
        this.userId = userId;
        this.userPhotoUrl = userPhotoUrl;
        this.userOnline = userOnline;
        this.userOnlineString = userOnlineString;
        this.userLastOnlineTime = userLastOnlineTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    public String getRecentMessage() {
        return recentMessage;
    }

    public void setRecentMessage(String recentMessage) {
        this.recentMessage = recentMessage;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public String getUserPublicKey() {
        return userPublicKey;
    }

    public void setUserPublicKey(String userPublicKey) {
        this.userPublicKey = userPublicKey;
    }

    public long getRecentMsgTime() {
        return recentMsgTime;
    }

    public void setRecentMsgTime(long recentMsgTime) {
        this.recentMsgTime = recentMsgTime;
    }

    public int getUserPublicKeyNumber() {
        return userPublicKeyNumber;
    }

    public void setUserPublicKeyNumber(int userPublicKeyNumber) {
        this.userPublicKeyNumber = userPublicKeyNumber;
    }

    public boolean isUserOnline() {
        return userOnline;
    }

    public void setUserOnline(boolean userOnline) {
        this.userOnline = userOnline;
    }

    public String getUserOnlineString() {
        return userOnlineString;
    }

    public void setUserOnlineString(String userOnlineString) {
        this.userOnlineString = userOnlineString;
    }

    public long getUserLastOnlineTime() {
        return userLastOnlineTime;
    }

    public void setUserLastOnlineTime(long userLastOnlineTime) {
        this.userLastOnlineTime = userLastOnlineTime;
    }
}
