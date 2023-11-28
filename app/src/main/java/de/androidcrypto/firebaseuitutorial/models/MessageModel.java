package de.androidcrypto.firebaseuitutorial.models;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

import java.util.HashMap;
import java.util.Map;

public class MessageModel {

    private String message;
    private long messageTime;
    private Timestamp messageTimestamp;
    private String senderId;
    private String receiverId;
    private String attachmentId;
    private boolean messageRead;
    private boolean messageEncrypted;
    private int pubKeyIdSender;
    private int pubKeyIdReceiver;
    private String pubKeySender;

    public MessageModel() {}

    // full message constructor

    public MessageModel(String message, long messageTime, Timestamp messageTimestamp, String senderId, String receiverId, String attachmentId, boolean messageRead, boolean messageEncrypted, int pubKeyIdSender, int pubKeyIdReceiver, String pubKeySender) {
        this.message = message;
        this.messageTime = messageTime;
        this.messageTimestamp = messageTimestamp;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.attachmentId = attachmentId;
        this.messageRead = messageRead;
        this.messageEncrypted = messageEncrypted;
        this.pubKeyIdSender = pubKeyIdSender;
        this.pubKeyIdReceiver = pubKeyIdReceiver;
        this.pubKeySender = pubKeySender;
    }

    public MessageModel(String message, long messageTime, String senderId, String receiverId, String attachmentId, boolean messageRead, boolean messageEncrypted, int pubKeyIdSender, int pubKeyIdReceiver, String pubKeySender) {
        this.message = message;
        this.messageTime = messageTime;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.attachmentId = attachmentId;
        this.messageRead = messageRead;
        this.messageEncrypted = messageEncrypted;
        this.pubKeyIdSender = pubKeyIdSender;
        this.pubKeyIdReceiver = pubKeyIdReceiver;
        this.pubKeySender = pubKeySender;
    }

    // constructor for beginner chats (unencrypted, no attachment)
    public MessageModel(String message, long messageTime, String senderId, String receiverId) {
        this.message = message;
        this.messageTime = messageTime;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.attachmentId = "";
        this.messageRead = false;
        this.messageEncrypted = false;
        this.pubKeyIdSender = 0;
        this.pubKeyIdReceiver = 0;
        this.pubKeySender = "";
    }

    public MessageModel(String message, long messageTime, Timestamp messageTimestamp, String senderId, String receiverId) {
        this.message = message;
        this.messageTime = messageTime;
        this.messageTimestamp = messageTimestamp;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.attachmentId = "";
        this.messageRead = false;
        this.messageEncrypted = false;
        this.pubKeyIdSender = 0;
        this.pubKeyIdReceiver = 0;
        this.pubKeySender = "";
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("sender", senderId);
        result.put("message", message);
        result.put("messageTime", messageTime);
        result.put("messageEncrypted", messageEncrypted);
        return result;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public boolean isMessageEncrypted() {
        return messageEncrypted;
    }

    public void setMessageEncrypted(boolean messageEncrypted) {
        this.messageEncrypted = messageEncrypted;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public boolean isMessageRead() {
        return messageRead;
    }

    public void setMessageRead(boolean messageRead) {
        this.messageRead = messageRead;
    }

    public int getPubKeyIdSender() {
        return pubKeyIdSender;
    }

    public void setPubKeyIdSender(int pubKeyIdSender) {
        this.pubKeyIdSender = pubKeyIdSender;
    }

    public int getPubKeyIdReceiver() {
        return pubKeyIdReceiver;
    }

    public void setPubKeyIdReceiver(int pubKeyReceiver) {
        this.pubKeyIdReceiver = pubKeyIdReceiver;
    }

    public String getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(String attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getPubKeySender() {
        return pubKeySender;
    }

    public void setPubKeySender(String pubKeySender) {
        this.pubKeySender = pubKeySender;
    }

    public Timestamp getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(Timestamp messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }
}
