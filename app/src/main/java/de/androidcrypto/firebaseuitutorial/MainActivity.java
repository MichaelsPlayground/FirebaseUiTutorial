package de.androidcrypto.firebaseuitutorial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /**
     * section authentication
     */

    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private Button signIn, signOut;
    public static final int RC_SIGN_IN = 1;
    private List<AuthUI.IdpConfig> authenticationProviders = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;

    private ActivityResultLauncher<Intent> signInLauncher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),
            (result) -> {
                // Handle the FirebaseAuthUIAuthenticationResult
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Toast.makeText(MainActivity.this, "User Signed In", Toast.LENGTH_SHORT).show();
                    signedInUser.setText(user.getEmail() + "\nDisplayName: " + user.getDisplayName());
                    activeButtonsWhileUserIsSignedIn(true);
                } else {
                    Log.e(TAG, "Error in handling the FirebaseAuthUIAuthenticationResult");
                }
            });


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

        /**
         * section for authentication
         */

        signedInUser = findViewById(R.id.etMainSignedInUser);
        signIn = findViewById(R.id.btnMainSignIn);
        signOut = findViewById(R.id.btnMainSignOut);

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
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(authenticationProviders)
                        .setTheme(R.style.Theme_FirebaseUiTutorial)
                        .build();

                signInLauncher.launch(signInIntent);
            }
        });



        /*
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "sign up a new or sign in an existing user");
                startActivityForResult(
                        AuthUI.getInstance()
                                .createSignInIntentBuilder()
                                .setIsSmartLockEnabled(true)
                                .setAvailableProviders(authenticationProviders)
                                .setTheme(R.style.Theme_FirebaseUiTutorial)
                                .build(),
                        RC_SIGN_IN
                );
            }
        });

         */

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
         * section for
         */

        /**
         * section for general purposes
         */



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
     * section for
     */



    /**
     * section for general purposes
     */

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void activeButtonsWhileUserIsSignedIn(boolean isSignedIn) {
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

}