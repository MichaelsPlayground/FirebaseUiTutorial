package de.androidcrypto.firebaseuitutorial.utils;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;

public class FirebaseUtils {

    /**
     * section for internal constants
     */

    public static final String USERS_FOLDER_NAME = "users";
    public static final String CHATROOMS_FOLDER_NAME = "messages";

    private static DatabaseReference databaseReference;

    /**
     * This class collects all references and collections on Firebase products
     */

    /**
     * section Authentication
     */

    public static FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }
    public static String getCurrentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }
    public static boolean isLoggedIn(){
        if(getCurrentUserId() != null){
            return true;
        }
        return false;
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    /**
     * section Firebase Realtime Database
     */

    public static DatabaseReference getDatabaseReference() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getDatabaseUsersReference() {
        return getDatabaseReference().child(USERS_FOLDER_NAME);
    }

    public static DatabaseReference getDatabaseUserReference(String userId) {
        return getDatabaseReference().child(USERS_FOLDER_NAME).child(userId);
    }

    public static DatabaseReference getDatabaseChatsReference() {
        return getDatabaseReference().child(CHATROOMS_FOLDER_NAME);
    }

    public static DatabaseReference getDatabaseChatroomReference(String chatroomId) {
        return getDatabaseReference().child(CHATROOMS_FOLDER_NAME).child(chatroomId);
    }

    public static String getChatroomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }


    /**
     * section Cloud Firestore Database
     */




    /**
     * section Firebase Storage
     */


    /**
     * section for conversions
     */

    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:mm").format(timestamp.toDate());
    }

    public static String timestampFullToString(Timestamp timestamp){
        return new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(timestamp.toDate());
    }

    public static String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

}
