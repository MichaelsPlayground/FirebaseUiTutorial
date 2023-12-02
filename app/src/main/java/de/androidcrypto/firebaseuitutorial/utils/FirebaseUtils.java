package de.androidcrypto.firebaseuitutorial.utils;

import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;

import de.androidcrypto.firebaseuitutorial.models.UserModel;

public class FirebaseUtils {

    /**
     * section for internal constants
     */

    // realtime database
    public static final String USER_OFFLINE = "offline";
    public static final String USER_ONLINE = "online";
    public static final String USERS_FOLDER_NAME = "users";
    public static final String CHATROOM_FOLDER_NAME = "messages";
    private static final String RECENT_MESSAGES_FOLDER_NAME = "recentMessages";
    public static final String CHATROOMS_FOLDER_NAME = "chatrooms";
    public static final String INFO_CONNECTED = ".info/connected";
    public static final String DATABASE_CONNECTIONS = "connections";
    public static final String DATABASE_LAST_ONLINE = "lastOnline";
    public static final String DATABASE_LAST_ONLINE_TIME = "lastOnlineTime";
    public static final String DATABASE_USER_PHOTO_URL_FIELD = "userPhotoUrl";

    // storage

    public static final String STORAGE_PROFILE_IMAGES_FOLDER_NAME = "profile_images";
    public static final String STORAGE_PROFILE_IMAGE_FILE_EXTENSION = ".jpg";


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

    public static void writeToCurrentUserAuthData(String displayName, String photoUrl) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(Uri.parse(photoUrl))
                .build();

        getCurrentUser().updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // success
                        }
                    }
                });
        return;
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

    public static Query getDatabaseUsersSortedLastOnlineDateReference() {
        return getDatabaseUsersReference().orderByChild(DATABASE_LAST_ONLINE_TIME);
    }

    public static DatabaseReference getDatabaseUserReference(String userId) {
        return getDatabaseReference().child(USERS_FOLDER_NAME).child(userId);
    }

    public static DatabaseReference getDatabaseUserFieldReference(String userId, String fieldName) {
        return getDatabaseReference().child(USERS_FOLDER_NAME).child(userId).child(fieldName);
    }

    public static DatabaseReference getDatabaseChatsReference() {
        return getDatabaseReference().child(CHATROOM_FOLDER_NAME);
    }

    public static DatabaseReference getDatabaseChatroomReference(String chatroomId) {
        return getDatabaseReference().child(CHATROOM_FOLDER_NAME).child(chatroomId);
    }

    public static DatabaseReference getDatabaseUserRecentMessagesReference(String userId) {
        return getDatabaseReference().child(RECENT_MESSAGES_FOLDER_NAME).child(userId);
    }
    public static DatabaseReference getDatabaseUserChatroomsReference(String userId, String chatroomId) {
        return getDatabaseUserChatroomsReference(userId).child(chatroomId);
    }
    public static DatabaseReference getDatabaseUserChatroomsReference(String userId) {
        return getDatabaseReference().child(USERS_FOLDER_NAME).child(userId).child(CHATROOMS_FOLDER_NAME);
    }

    // https://firebase.google.com/docs/database/android/offline-capabilities#section-presence
    public static DatabaseReference getDatabaseUserConnectionReference(String userId) {
        return getDatabaseUserReference(userId).child(DATABASE_CONNECTIONS);
    }

    public static DatabaseReference getDatabaseUserLastOnlineReference(String userId) {
        return getDatabaseUserReference(userId).child(DATABASE_LAST_ONLINE);
    }


    public static DatabaseReference getDatabaseInfoConnected() {
        return getDatabaseReference().child(INFO_CONNECTED);
    }
    public static void setPersistenceStatus(boolean status) {
        FirebaseDatabase.getInstance().setPersistenceEnabled(status);
    }
    // see Keeping Data Fresh as well:
    /*
    DatabaseReference scoresRef = FirebaseDatabase.getInstance().getReference("scores");
    scoresRef.keepSynced(true); or false
     */

    public static String getChatroomId(String userId1, String userId2) {
        if (userId1.hashCode() < userId2.hashCode()) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }

    // copies the current user credentials to Database user
    public static void copyAuthDatabaseToUserDatabase() {
        FirebaseUser user = getCurrentUser();
        String userId = user.getUid();
        if (TextUtils.isEmpty(userId)) {
            UserModel userModel = new UserModel(
                    userId, user.getDisplayName(), user.getEmail(), user.getPhotoUrl().toString(), "", 0, USER_ONLINE, TimeUtils.getActualUtcZonedDateTime());
        FirebaseUtils.getDatabaseUserReference(userId).setValue(userModel);
            System.out.println("*** uName: " + user.getDisplayName() + " url: " + user.getPhotoUrl());
        }
    }

/*
databaseUserReference = FirebaseUtils.getDatabaseUserReference(authUserId);
        if (!TextUtils.isEmpty(authUserId)) {
            databaseUserReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    hideProgressBar();
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting data", task.getException());
                    } else {
                        // check for a null value means no user data were saved before
                        UserModel userModel = task.getResult().getValue(UserModel.class);
                        Log.d(TAG, "User model: " + String.valueOf(userModel));
                        if (userModel.getUserId() == null) {
                            Log.i(TAG, "userModel is null, show message");
                            infoNoData.setVisibility(View.VISIBLE);
                            // get data from user
                            userId.setText(authUserId);
                            userEmail.setText(authUserEmail);
                            userName.setText(FirebaseUtils.usernameFromEmail(authUserEmail));
                            userPhotoUrl.setText(authPhotoUrl);
                            userPublicKey.setText("");
                            userPublicKeyNumber.setText("0");

                            // automatically save a new dataset
                            showProgressBar();
                            writeUserProfile(authUserId, Objects.requireNonNull(userName.getText()).toString(),
                                    Objects.requireNonNull(userEmail.getText()).toString(),
                                    Objects.requireNonNull(userPhotoUrl.getText()).toString(),
                                    Objects.requireNonNull(userPublicKey.getText()).toString(),
                                    Objects.requireNonNull(userPublicKeyNumber.getText()).toString(),
                                    FirebaseUtils.USER_ONLINE,
                                    TimeUtils.getActualUtcZonedDateTime()
                            );
                            Snackbar snackbar = Snackbar
                                    .make(progressBar, "data written to database", Snackbar.LENGTH_SHORT);
                            snackbar.show();
                            hideProgressBar();
                        } else {
                            Log.i(TAG, "userModel email: " + userModel.getUserMail());
                            infoNoData.setVisibility(View.GONE);
                            // get data from user
                            userId.setText(authUserId);
                            userEmail.setText(userModel.getUserMail());
                            userName.setText(userModel.getUserName());
                            String photoUrl = userModel.getUserPhotoUrl();
                            userPhotoUrl.setText(photoUrl);
                            userPublicKey.setText(userModel.getUserPublicKey());
                            userPublicKeyNumber.setText(String.valueOf(userModel.getUserPublicKeyNumber()));
                            // load image if userPhotoUrl is not empty
                            if (!TextUtils.isEmpty(photoUrl)) {
                                // Download directly from StorageReference using Glide
                                // (See MyAppGlideModule for Loader registration)
                                GlideApp.with(getApplicationContext())
                                        .load(photoUrl)
                                        .into(profileImageView);
                            }
                        }
                    }
                }
 */


    /**
     * section Cloud Firestore Database
     */




    /**
     * section Firebase Storage
     */

    public static StorageReference getStorageProfileImagesReference(String userId) {
        return getStorageChildReference(STORAGE_PROFILE_IMAGES_FOLDER_NAME).child(userId + STORAGE_PROFILE_IMAGE_FILE_EXTENSION);
    }

    public static StorageReference getStorageChildReference(String child) {
        return FirebaseStorage.getInstance().getReference().child(child);
    }



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
