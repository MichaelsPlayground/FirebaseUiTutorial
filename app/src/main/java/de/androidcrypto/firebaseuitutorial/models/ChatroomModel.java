package de.androidcrypto.firebaseuitutorial.models;

public class ChatroomModel {

    private String chatroomId;
    private String userId1;
    private String userName1;
    private String userEmail1;
    private String userProfileImage1;
    private String userId2;
    private String userName2;
    private String userEmail2;
    private String userProfileImage2;
    private long chatLastTime;
    private String lastMessage;
    private String lastMessageFromUserId;
    private String lastMessageFromUserName;

    public ChatroomModel() {
    }

    public ChatroomModel(String chatroomId, String userId1, String userName1, String userEmail1, String userProfileImage1, String userId2, String userName2, String userEmail2, String userProfileImage2, long chatLastTime, String lastMessage, String lastMessageFromUserId, String lastMessageFromUserName) {
        this.chatroomId = chatroomId;
        this.userId1 = userId1;
        this.userName1 = userName1;
        this.userEmail1 = userEmail1;
        this.userProfileImage1 = userProfileImage1;
        this.userId2 = userId2;
        this.userName2 = userName2;
        this.userEmail2 = userEmail2;
        this.userProfileImage2 = userProfileImage2;
        this.chatLastTime = chatLastTime;
        this.lastMessage = lastMessage;
        this.lastMessageFromUserId = lastMessageFromUserId;
        this.lastMessageFromUserName = lastMessageFromUserName;
    }

    public ChatroomModel(String chatroomId, String userId1, String userId2, long chatLastTime, String lastMessage, String lastMessageFromUserId, String lastMessageFromUserName) {
        this.chatroomId = chatroomId;
        this.userId1 = userId1;
        this.userId2 = userId2;
        this.chatLastTime = chatLastTime;
        this.lastMessage = lastMessage;
        this.lastMessageFromUserId = lastMessageFromUserId;
        this.lastMessageFromUserName = lastMessageFromUserName;
    }

    public String getChatroomId() {
        return chatroomId;
    }

    public void setChatroomId(String chatroomId) {
        this.chatroomId = chatroomId;
    }

    public String getUserId1() {
        return userId1;
    }

    public void setUserId1(String userId1) {
        this.userId1 = userId1;
    }

    public String getUserName1() {
        return userName1;
    }

    public void setUserName1(String userName1) {
        this.userName1 = userName1;
    }

    public String getUserEmail1() {
        return userEmail1;
    }

    public void setUserEmail1(String userEmail1) {
        this.userEmail1 = userEmail1;
    }

    public String getUserProfileImage1() {
        return userProfileImage1;
    }

    public void setUserProfileImage1(String userProfileImage1) {
        this.userProfileImage1 = userProfileImage1;
    }

    public String getUserId2() {
        return userId2;
    }

    public void setUserId2(String userId2) {
        this.userId2 = userId2;
    }

    public String getUserName2() {
        return userName2;
    }

    public void setUserName2(String userName2) {
        this.userName2 = userName2;
    }

    public String getUserEmail2() {
        return userEmail2;
    }

    public void setUserEmail2(String userEmail2) {
        this.userEmail2 = userEmail2;
    }

    public String getUserProfileImage2() {
        return userProfileImage2;
    }

    public void setUserProfileImage2(String userProfileImage2) {
        this.userProfileImage2 = userProfileImage2;
    }

    public long getChatLastTime() {
        return chatLastTime;
    }

    public void setChatLastTime(long chatLastTime) {
        this.chatLastTime = chatLastTime;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageFromUserId() {
        return lastMessageFromUserId;
    }

    public void setLastMessageFromUserId(String lastMessageFromUserId) {
        this.lastMessageFromUserId = lastMessageFromUserId;
    }

    public String getLastMessageFromUserName() {
        return lastMessageFromUserName;
    }

    public void setLastMessageFromUserName(String lastMessageFromUserName) {
        this.lastMessageFromUserName = lastMessageFromUserName;
    }
}
