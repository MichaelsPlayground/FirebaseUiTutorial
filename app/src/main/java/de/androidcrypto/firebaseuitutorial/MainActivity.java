package de.androidcrypto.firebaseuitutorial;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.androidcrypto.firebaseuitutorial.auth.AuthChangeUserPasswordActivity;
import de.androidcrypto.firebaseuitutorial.auth.AuthEditUserProfileActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseChatroomsActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseEditUserProfileActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseExportCompleteChatActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseListUserActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseListUserLvActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseListUserPresenceActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseListUserPresenceLvActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseListUserRecentMessagesActivity;
import de.androidcrypto.firebaseuitutorial.firestore.FirestoreChatroomsActivity;
import de.androidcrypto.firebaseuitutorial.firestore.FirestoreEditUserProfileActivity;
import de.androidcrypto.firebaseuitutorial.firestore.FirestoreListUserActivity;
import de.androidcrypto.firebaseuitutorial.firestore.FirestoreListUserRecentMessagesActivity;
import de.androidcrypto.firebaseuitutorial.storage.StorageDownloadFilesAndImagesActivity;
import de.androidcrypto.firebaseuitutorial.storage.StorageListImagesActivity;
import de.androidcrypto.firebaseuitutorial.storage.StorageListReferencesOnDatabaseActivity;
import de.androidcrypto.firebaseuitutorial.storage.StorageListReferencesOnFirestoreActivity;
import de.androidcrypto.firebaseuitutorial.storage.StorageUploadFilesAndImagesActivity;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class MainActivity extends AppCompatActivity {

    /**
     * section for start up methods
     */

    private static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 101;

    /**
     * section authentication
     */

    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private Button signIn, signOut, editAuthUserProfile, verification, accountDeletion;
    private Button changePassword, resetPassword;

    private List<AuthUI.IdpConfig> authenticationProviders = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            (result) -> {

                // Handle the FirebaseAuthUIAuthenticationResult
                //FirebaseUser user = firebaseAuth.getCurrentUser();
                FirebaseUser user = FirebaseUtils.getCurrentUser();
                if (user != null) {
                    Toast.makeText(MainActivity.this, "User Signed In", Toast.LENGTH_SHORT).show();
                    signedInUser.setText(user.getEmail() + "\nDisplayName: " + user.getDisplayName());
                    activeButtonsWhileUserIsSignedIn(true);

                    // detect that the user is a new user (recently signed up)
                    IdpResponse idpResponse = result.getIdpResponse();
                    if ((idpResponse != null) && (idpResponse.isNewUser())) {
                        // setup user is Realtime Database and Firestore Database
                        System.out.println("*** a new user is detected ***");
                        FirebaseUtils.copyAuthDatabaseToDatabaseUser();
                        FirebaseUtils.copyAuthDatabaseToFirestoreUser();
                    }
                    status("online", TimeUtils.getActualUtcZonedDateTime());
                } else {
                    Log.e(TAG, "Error in handling the FirebaseAuthUIAuthenticationResult");
                }
            });


    /**
     * section Firebase Realtime Database
     */

    private Button editDatabaseUserProfileCanHub, listDatabaseUser, listDatabaseUserLv;
    private Button listDatabaseUserRecentMessages;
    private Button listDatabaseUserChatrooms;
    private Button presenceCheckDatabase;
    private Button presenceCheckDatabaseLv;
    private Button exportDatabaseCompleteChat;
    private DatabaseReference actualUserDatabaseReference;

    /**
     * section Cloud Firestore Database
     */

    private Button editFirestoreUserProfilecanHub, listFirestoreUser;
    private Button listFirestoreUserRecentMessages;
    private Button listFirestoreUserChatrooms;

    /**
     * section Firebase Storage
     */

    private Button uploadStorageFilesAndImages, downloadStorageFilesAndImages;
    private Button listStorageReferencesOnDatabase, listStorageReferencesOnFirestore;
    private Button listStorageImagesListview;

    /**
     * section
     */


    /**
     * section
     */

    /**
     * section for general purposes
     */

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);


        /**
         * section for start up methods
         */

        askWriteStoragePermission();


        /**
         * section for Authentication
         */

        signedInUser = findViewById(R.id.etMainSignedInUser);
        signIn = findViewById(R.id.btnMainAuthSignIn);
        signOut = findViewById(R.id.btnMainAuthSignOut);
        editAuthUserProfile = findViewById(R.id.btnMainAuthEditUserProfile);
        changePassword = findViewById(R.id.btnMainAuthChangePassword);
        resetPassword = findViewById(R.id.btnMainAuthResetPassword);
        verification = findViewById(R.id.btnMainAuthVerification);
        accountDeletion = findViewById(R.id.btnMainAuthDeleteUser);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(MainActivity.this, "User Signed In", Toast.LENGTH_SHORT).show();
                    signedInUser.setText(user.getEmail() + "\nDisplayName: " + user.getDisplayName());
                    activeButtonsWhileUserIsSignedIn(true);
                    // generate or update database user entry
                    FirebaseUtils.copyAuthDatabaseToDatabaseUser(); // this is set by copyAuth...
                    FirebaseUtils.copyAuthDatabaseToFirestoreUser(); // this is set by copyAuth...
                } else {
                    Log.e(TAG, "Could not retrieve onAuthStateChanged, user is NULL");
                    signedInUser.setText("");
                    activeButtonsWhileUserIsSignedIn(false);
                }
            }
        };

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sign up a new or sign in an existing user");
                Intent signInIntent = AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        // ... options ...
                        .setIsSmartLockEnabled(true)
                        .setAvailableProviders(authenticationProviders)
                        .setTheme(R.style.Theme_FirebaseUiTutorial)
                        .build();
                signInLauncher.launch(signInIntent);
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sign out the current user");
                // set user onlineStatus in Firestore users to false
                status("offline", TimeUtils.getActualUtcZonedDateTime());
                firebaseAuth.signOut();
                signedInUser.setText(null);
                activeButtonsWhileUserIsSignedIn(false);
            }
        });

        editAuthUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "edit user profile on auth database");
                Intent intent = new Intent(MainActivity.this, AuthEditUserProfileActivity.class);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "change password on auth database");
                Intent intent = new Intent(MainActivity.this, AuthChangeUserPasswordActivity.class);
                startActivity(intent);
            }
        });

        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "reset password on auth database");
                // todo code this
                //Intent intent = new Intent(MainActivity.this, AuthChangeUserPasswordActivity.class);
                //startActivity(intent);
            }
        });

        verification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "verificate the current Email account");

                // see https://firebase.google.com/docs/auth/android/email-link-auth
                FirebaseUtils.getCurrentUser().sendEmailVerification()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),
                                            "An email was sent to your Email address for verification",
                                            Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });
            }
        });

        accountDeletion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "account deletion");
                // see https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#deleting-accounts

                // this is a permanently operation - show a confirmation dialog
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                AuthUI.getInstance()
                                        .delete(MainActivity.this)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Deletion succeeded
                                                    Toast.makeText(getApplicationContext(),
                                                            "The account was deleted",
                                                            Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "Account deleted");
                                                } else {
                                                    // Deletion failed
                                                    Toast.makeText(getApplicationContext(),
                                                            "FAILURE on account deletion",
                                                            Toast.LENGTH_SHORT).show();
                                                    Log.d(TAG, "FAILURE on account deletion");
                                                }
                                            }
                                        });
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                // nothing to do
                                Toast.makeText(getApplicationContext(),
                                        "Deletion of account aborted",
                                        Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "deletion aborted");
                                break;
                        }
                    }
                };
                final String selectedFolderString = "You are going to delete the account" + "\n\n" +
                        "This is an irrevocable setting that cannot get undone" + "\n\n" +
                        "Do you want to proceed ?";
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setMessage(selectedFolderString).setPositiveButton(android.R.string.yes, dialogClickListener)
                        .setNegativeButton(android.R.string.no, dialogClickListener)
                        .setTitle("DELETE the account")
                        .show();
            }
        });

        /**
         * section for Firebase Realtime Database
         */

        editDatabaseUserProfileCanHub = findViewById(R.id.btnMainDatabaseEditUserProfile);
        editDatabaseUserProfileCanHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "edit Database User Profile");
                Intent intent = new Intent(MainActivity.this, DatabaseEditUserProfileActivity.class);
                startActivity(intent);
            }
        });

        listDatabaseUser = findViewById(R.id.btnMainDatabaseListUser);
        listDatabaseUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Database user");
                Intent intent = new Intent(MainActivity.this, DatabaseListUserActivity.class);
                startActivity(intent);
            }
        });

        listDatabaseUserLv = findViewById(R.id.btnMainDatabaseListUserLv);
        listDatabaseUserLv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Database user ListView");
                Intent intent = new Intent(MainActivity.this, DatabaseListUserLvActivity.class);
                startActivity(intent);
            }
        });

        listDatabaseUserRecentMessages = findViewById(R.id.btnMainDatabaseListUserRecentMessages);
        listDatabaseUserRecentMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Database user recent messages RecyclerView");
                Intent intent = new Intent(MainActivity.this, DatabaseListUserRecentMessagesActivity.class);
                startActivity(intent);
            }
        });

        listDatabaseUserChatrooms = findViewById(R.id.btnMainDatabaseListUserChatrooms);
        listDatabaseUserChatrooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Database user chatrooms RecyclerView");
                Intent intent = new Intent(MainActivity.this, DatabaseChatroomsActivity.class);
                startActivity(intent);
            }
        });

        exportDatabaseCompleteChat = findViewById(R.id.btnMainDatabaseExportCompleteChat);
        exportDatabaseCompleteChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "export complete database chat");
                Intent intent = new Intent(MainActivity.this, DatabaseExportCompleteChatActivity.class);
                startActivity(intent);
            }
        });

        presenceCheckDatabase = findViewById(R.id.btnMainDatabasePresenceCheck);
        presenceCheckDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "presenceCheckDatabase");
                // https://firebase.google.com/docs/database/android/offline-capabilities#section-presence

                Intent intent = new Intent(MainActivity.this, DatabaseListUserPresenceActivity.class);
                startActivity(intent);

/*
                String userId = FirebaseUtils.getCurrentUserId();
                // Since I can connect from multiple devices, we store each connection instance separately
                // any time that connectionsRef's value is null (i.e. has no children) I am offline
                final DatabaseReference myConnectionsRef = FirebaseUtils.getDatabaseUserConnectionReference(userId);

                // Stores the timestamp of my last disconnect (the last time I was seen online)
                final DatabaseReference lastOnlineRef = FirebaseUtils.getDatabaseUserLastOnlineReference(userId);

                final DatabaseReference connectedRef = FirebaseUtils.getDatabaseInfoConnected();
                connectedRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean connected = snapshot.getValue(Boolean.class);
                        Log.d(TAG, "presenceCheckDatabase onDataChange connected: " + connected);
                        if (connected) {
                            DatabaseReference con = myConnectionsRef.push();

                            // When this device disconnects, remove it
                            con.onDisconnect().removeValue();

                            // When I disconnect, update the last time I was seen online
                            lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                            // Add this device to my connections list
                            // this value could contain info about the device or a timestamp too
                            con.setValue(Boolean.TRUE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Listener was cancelled at .info/connected");
                    }
                });
*/
            }
        });

        presenceCheckDatabaseLv = findViewById(R.id.btnMainDatabasePresenceLvCheck);
        presenceCheckDatabaseLv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "presenceCheckDatabaseLv");
                // https://firebase.google.com/docs/database/android/offline-capabilities#section-presence

                Intent intent = new Intent(MainActivity.this, DatabaseListUserPresenceLvActivity.class);
                startActivity(intent);
            }
        });


        /**
         * section for Cloud Firestore Database
         */

        editFirestoreUserProfilecanHub =

                findViewById(R.id.btnMainFirestoreEditUserProfile);
        editFirestoreUserProfilecanHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "edit Firestore User Profile");
                Intent intent = new Intent(MainActivity.this, FirestoreEditUserProfileActivity.class);
                startActivity(intent);
            }
        });

        listFirestoreUser =

                findViewById(R.id.btnMainFirestoreListUser);
        listFirestoreUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Database user");
                Intent intent = new Intent(MainActivity.this, FirestoreListUserActivity.class);
                startActivity(intent);
            }
        });

        listFirestoreUserRecentMessages =

                findViewById(R.id.btnMainFirestoreListUserRecentMessages);
        listFirestoreUserRecentMessages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Firestore user recent messages RecyclerView");
                Intent intent = new Intent(MainActivity.this, FirestoreListUserRecentMessagesActivity.class);
                startActivity(intent);
            }
        });

        listFirestoreUserChatrooms =

                findViewById(R.id.btnMainFirestoreListUserChatrooms);
        listFirestoreUserChatrooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Firestore user chatrooms RecyclerView");
                Intent intent = new Intent(MainActivity.this, FirestoreChatroomsActivity.class);
                startActivity(intent);
            }
        });

        /**
         * section for Firebase Storage
         */

        uploadStorageFilesAndImages =

                findViewById(R.id.btnMainStorageUploadFilesImages);
        uploadStorageFilesAndImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "upload Firebase Storage files and images");
                Intent intent = new Intent(MainActivity.this, StorageUploadFilesAndImagesActivity.class);
                startActivity(intent);
            }
        });

        downloadStorageFilesAndImages =

                findViewById(R.id.btnMainStorageDownloadFilesImages);
        downloadStorageFilesAndImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "download Firebase Storage files and images");
                Intent intent = new Intent(MainActivity.this, StorageDownloadFilesAndImagesActivity.class);
                startActivity(intent);
            }
        });

        listStorageReferencesOnDatabase =

                findViewById(R.id.btnMainStorageListReferencesOnDatabase);
        listStorageReferencesOnDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Firebase Storage references on Database");
                Intent intent = new Intent(MainActivity.this, StorageListReferencesOnDatabaseActivity.class);
                startActivity(intent);
            }
        });


        listStorageReferencesOnFirestore =

                findViewById(R.id.btnMainStorageListReferencesOnFirestore);
        listStorageReferencesOnFirestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Firebase Storage references on Firestore");
                Intent intent = new Intent(MainActivity.this, StorageListReferencesOnFirestoreActivity.class);
                startActivity(intent);
            }
        });

        listStorageImagesListview =

                findViewById(R.id.btnMainStorageListImages);
        listStorageImagesListview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "list Firebase Storage images in ListView");
                Intent intent = new Intent(MainActivity.this, StorageListImagesActivity.class);
                startActivity(intent);
            }
        });


        /**
         * section for
         */


        /**
         * section for
         */


        /**
         * section for
         */

        /**
         * section for general purposes
         */


        // don't show the keyboard on startUp
        getWindow().

                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    /**
     * section for
     */

    private void status(String status, long utcTimestamp) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        // update only if a user is signed in
        if (!TextUtils.isEmpty(currentUserId)) {
            // Firebase
            actualUserDatabaseReference = FirebaseUtils.getDatabaseUserReference(currentUserId);
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userOnlineString", status);
            hashMap.put("userLastOnlineTime", utcTimestamp);
            actualUserDatabaseReference.updateChildren(hashMap);

            // Firestore
            DocumentReference actualUserFirebaseReference = FirebaseUtils.getFirestoreUserReference(currentUserId);
            actualUserFirebaseReference.update(hashMap);
        }
    }


    /**
     * section for start up methods
     */

    private void askWriteStoragePermission() {
        String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permissions[1]) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Write permission granted", Toast.LENGTH_SHORT)
                    .show();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }


    /**
     * section for
     */


    /**
     * section for
     */


    /**
     * section for
     */


    /**
     * section for
     */


    /**
     * section for general purposes
     */

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        firebaseAuth.addAuthStateListener(authStateListener);
        // called when this activity is in the foreground
        status("online", TimeUtils.getActualUtcZonedDateTime());
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        firebaseAuth.removeAuthStateListener(authStateListener);
        status("offline", TimeUtils.getActualUtcZonedDateTime());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        // called when the app is closed completely with Androids taskmanager
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    private void activeButtonsWhileUserIsSignedIn(boolean isSignedIn) {
        // auth
        editAuthUserProfile.setEnabled(isSignedIn);
        verification.setEnabled(isSignedIn);
        accountDeletion.setEnabled(isSignedIn);
        // realtime database
        editDatabaseUserProfileCanHub.setEnabled(isSignedIn);
        listDatabaseUser.setEnabled(isSignedIn);
        listDatabaseUserLv.setEnabled(isSignedIn);
        listDatabaseUserRecentMessages.setEnabled(isSignedIn);
        listDatabaseUserChatrooms.setEnabled(isSignedIn);
        // firestore database
        editFirestoreUserProfilecanHub.setEnabled(isSignedIn);
        listFirestoreUser.setEnabled(isSignedIn);
        listFirestoreUserRecentMessages.setEnabled(isSignedIn);
        listFirestoreUserChatrooms.setEnabled(isSignedIn);
        // storage
        uploadStorageFilesAndImages.setEnabled(isSignedIn);
        downloadStorageFilesAndImages.setEnabled(isSignedIn);
        listStorageReferencesOnDatabase.setEnabled(isSignedIn);
        listStorageReferencesOnFirestore.setEnabled(isSignedIn);
        listStorageImagesListview.setEnabled(isSignedIn);
    }

}