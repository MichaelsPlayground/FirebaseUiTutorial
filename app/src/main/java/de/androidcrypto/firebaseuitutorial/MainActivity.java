package de.androidcrypto.firebaseuitutorial;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import de.androidcrypto.firebaseuitutorial.firebasedatabase.DatabaseEditUserProfileActivity;
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

    private Button editDatabaseUserProfile;

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