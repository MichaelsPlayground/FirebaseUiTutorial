package de.androidcrypto.firebaseuitutorial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.androidcrypto.firebaseuitutorial.database.DatabaseEditUserProfileActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseListUserActivity;
import de.androidcrypto.firebaseuitutorial.database.DatabaseListUserLvActivity;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

//public class MainActivity extends AppCompatActivity implements ActivityResultCallback<FirebaseAuthUIAuthenticationResult> {
public class MainActivity extends AppCompatActivity {

    /**
     * section authentication
     */

    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private Button signIn, signOut;

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
                } else {
                    Log.e(TAG, "Error in handling the FirebaseAuthUIAuthenticationResult");
                }
            });


    /**
     * section Firebase Realtime Database
     */

    private Button editDatabaseUserProfile, listDatabaseUser, listDatabaseUserLv;
    private Button presenceCheckDatabase;
    private DatabaseReference actualUserDatabaseReference;

    /**
     * section
     */


    /**
     * section
     */



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
         * section for Authentication
         */

        signedInUser = findViewById(R.id.etMainSignedInUser);
        signIn = findViewById(R.id.btnMainAuthSignIn);
        signOut = findViewById(R.id.btnMainAuthSignOut);

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
                //setFirestoreUserOnlineStatus(mFirebaseAuth.getCurrentUser().getUid(), false);
                firebaseAuth.signOut();
                signedInUser.setText(null);
                activeButtonsWhileUserIsSignedIn(false);
            }
        });

        /**
         * section for Firebase Realtime Database
         */

        editDatabaseUserProfile = findViewById(R.id.btnMainDatabaseEditUserProfile);
        editDatabaseUserProfile.setOnClickListener(new View.OnClickListener() {
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

        presenceCheckDatabase = findViewById(R.id.btnMainDatabasePresenceCheck);
        presenceCheckDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "presenceCheckDatabase");
                // https://firebase.google.com/docs/database/android/offline-capabilities#section-presence

                // todo check that you are logged in when running this !!

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

            }
        });

        /*

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
         * section for
         */




        /**
         * section for
         */

        /**
         * section for general purposes
         */


        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

    }

    /**
     * section for
     */

    private void status(String status){
        actualUserDatabaseReference = FirebaseUtils.getDatabaseUserReference(FirebaseUtils.getCurrentUserId());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userOnlineString", status);
        actualUserDatabaseReference.updateChildren(hashMap);
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
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
        firebaseAuth.removeAuthStateListener(authStateListener);
        status("offline");
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
        editDatabaseUserProfile.setEnabled(isSignedIn);
        /*
        databaseUserProfile.setEnabled(isSignedIn);
        databaseUpdateUserImage.setEnabled(isSignedIn);
        databaseListUser.setEnabled(isSignedIn);
        databaseListUserRv.setEnabled(isSignedIn);
        databaseSendMessage.setEnabled(isSignedIn);
        databaseGenerateTestMessages.setEnabled(isSignedIn);
        firestoreDatabaseUserProfile.setEnabled(isSignedIn);
        firestoreDatabaseUpdateUserImage.setEnabled(isSignedIn);
        firestoreDatabaseSelectUser.setEnabled(isSignedIn);
        firestoreDatabaseSelectUserRv.setEnabled(isSignedIn);
        firestoreDatabaseChatMessage.setEnabled(isSignedIn);
        images.setEnabled(isSignedIn);
        uploadImage.setEnabled(isSignedIn);
        uploadCropImage.setEnabled(isSignedIn);
        uploadResizedImage.setEnabled(isSignedIn);
        uploadCropImageNotUi.setEnabled(isSignedIn);
        listImages.setEnabled(isSignedIn);
        listResizedImages.setEnabled(isSignedIn);
        */
    }

    /*
    @Override
    public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
        // Successfully signed in
        IdpResponse response = result.getIdpResponse();
        handleSignInResponse(result.getResultCode(), response);
    }

     */

    private void handleSignInResponse(int resultCode, @Nullable IdpResponse response) {
        // Successfully signed in
        if (resultCode == RESULT_OK) {
            Log.d(TAG, "handleSignInResponse: RESULT_OK");
            //startSignedInActivity(response);
            //finish();
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                Log.d(TAG, "handleSignInResponse: RESPONSE == null");
                //showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                Log.d(TAG, "handleSignInResponse: no_internet_connection");
                //showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                Log.d(TAG, "handleSignInResponse: ANONYMOUS_UPGRADE_MERGE_CONFLICT");
                return;
                /*
                Intent intent = new Intent(this, AnonymousUpgradeActivity.class).putExtra
                        (ExtraConstants.IDP_RESPONSE, response);
                startActivity(intent);

                 */
            }

            if (response.getError().getErrorCode() == ErrorCodes.ERROR_USER_DISABLED) {
                Log.d(TAG, "handleSignInResponse: account_disabled");
                //showSnackbar(R.string.account_disabled);
                return;
            }
            Log.d(TAG, "handleSignInResponse: unknown_error");
            //showSnackbar(R.string.unknown_error);
            Log.e(TAG, "Sign-in error: ", response.getError());
            return;
        }
    }

}