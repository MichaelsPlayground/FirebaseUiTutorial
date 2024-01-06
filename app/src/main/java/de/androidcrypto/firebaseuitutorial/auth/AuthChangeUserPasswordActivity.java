package de.androidcrypto.firebaseuitutorial.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class AuthChangeUserPasswordActivity extends AppCompatActivity {

    private static final String TAG = AuthChangeUserPasswordActivity.class.getSimpleName();

    /**
     * This class is NOT using firestoreUi for the change purposes
     */

    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private com.google.android.material.textfield.TextInputLayout userOldPasswordLayout, userNewPasswordLayout;
    private com.google.android.material.textfield.TextInputEditText userId, userEmail, userOldPassword, userNewPassword;

    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_change_user_password);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etAuthUserSignedInUser);
        progressBar = findViewById(R.id.pbAuthUser);

        signedInUser = findViewById(R.id.etAuthUserSignedInUser);
        userId = findViewById(R.id.etAuthUserUserId);
        userOldPasswordLayout = findViewById(R.id.etAuthUserOldPasswordLayout);
        userEmail = findViewById(R.id.etAuthUserUserEmail);
        userOldPassword = findViewById(R.id.etAuthUserNewPassword);
        userNewPasswordLayout = findViewById(R.id.etAuthUserNewPasswordLayout);
        userNewPassword = findViewById(R.id.etAuthUserNewPassword);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Button changePassword = findViewById(R.id.btnAuthUserChangePassword);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "change user password for user id: " + authUserId);

                // sanity checks

                FirebaseUser user = FirebaseUtils.getCurrentUser();
                List<? extends UserInfo> providerData = user.getProviderData();
                boolean isEmailPasswordProvider = false;
                for (int i = 0; i < providerData.size(); i++) {
                    UserInfo provDat = providerData.get(i);
                    Log.d(TAG, "provData providerId: " + provDat.getProviderId());
                    if (provDat.getProviderId().equals("password")) isEmailPasswordProvider = true;
                }
                Log.d(TAG, "ProviderData ID:" + user.getProviderId());
                if (!isEmailPasswordProvider) {
                    AndroidUtils.showSnackbarRedLong(signedInUser, "You signed in with Google, so changing of the password is not allowed here.");
                    return;
                }

                String userOldPasswordString = userOldPassword.getText().toString();
                if (TextUtils.isEmpty(userOldPasswordString)) {
                    userNewPasswordLayout.setError("old user password cannot be empty");
                    Toast.makeText(getApplicationContext(),
                            "old user password cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    userOldPasswordLayout.setError("");
                }
                String userNewPasswordString = userNewPassword.getText().toString();
                if (TextUtils.isEmpty(userNewPasswordString)) {
                    userNewPasswordLayout.setError("new user password cannot be empty");
                    Toast.makeText(getApplicationContext(),
                            "new user password cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    userNewPasswordLayout.setError("");
                }
                if (userNewPasswordString.length() < 6) {
                    userNewPasswordLayout.setError("new user password is minimum 6 characters");
                    Toast.makeText(getApplicationContext(),
                            "new user password is too short",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    userNewPasswordLayout.setError("");
                }
                if (!authUserId.equals("")) {
                    if (!Objects.requireNonNull(userId.getText()).toString().equals("")) {
                        // re-authenticate the user
                        // this is for Email/Password provider only !
                        AuthCredential credential = EmailAuthProvider
                                .getCredential(authUserEmail, userOldPasswordString);
                        // Prompt the user to re-provide their sign-in credentials
                        user.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Log.d(TAG, "User re-authenticated, starting password changing.");
                                        user.updatePassword(userNewPasswordString)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "User password updated.");
                                                            AndroidUtils.showSnackbarGreenShort(signedInUser, "password changed");
                                                        } else {
                                                            AndroidUtils.showSnackbarRedLong(signedInUser, "could not change the password, please sign in again");
                                                        }
                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "error on re-authentication of the user: " + e.getMessage());
                                    }
                                });
                    } else {
                        // this should not happen, but...
                        Toast.makeText(getApplicationContext(),
                                "sign in a user before changing",
                                Toast.LENGTH_SHORT).show();
                    }

                }
            }

            ;
        });
    }

    ;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
        if (currentUser != null) {
            reload();
        } else {
            signedInUser.setText("no user is signed in");
        }
    }

    private void reload() {
        Objects.requireNonNull(FirebaseUtils.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(FirebaseUtils.getCurrentUser());
                } else {
                    Log.e(TAG, "reload", task.getException());
                    Toast.makeText(getApplicationContext(),
                            "Failed to reload user.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {
            authUserId = user.getUid();
            authUserEmail = user.getEmail();
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "";
            }
            if (user.getPhotoUrl() != null) {
                authPhotoUrl = Objects.requireNonNull(user.getPhotoUrl()).toString();
            } else {
                authPhotoUrl = "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Data from Auth Database").append("\n");
            sb.append("User id: ").append(authUserId).append("\n");
            sb.append("Email: ").append(authUserEmail).append("\n");
            if (TextUtils.isEmpty(authDisplayName)) {
                sb.append("Display name: ").append("no display name available").append("\n");
            } else {
                sb.append("Display name: ").append(authDisplayName).append("\n");
            }
            if (TextUtils.isEmpty(authPhotoUrl)) {
                sb.append("Photo URL: ").append("no photo url available").append("\n");
            } else {
                sb.append("Photo URL: ").append(authPhotoUrl).append("\n");
            }
            if (user.isEmailVerified()) {
                sb.append("Email verification: ").append("Email address is VERIFIED");
            } else {
                sb.append("Email verification: ").append("Email address is NOT verified");
            }
            signedInUser.setText(sb.toString());
            userId.setText(authUserId);
            userEmail.setText(authUserEmail);
            userNewPassword.setText("");
        } else {
            signedInUser.setText(null);
        }
    }

    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * section for options menu
     */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_return_home, menu);

        MenuItem mGoToHome = menu.findItem(R.id.action_return_main);
        mGoToHome.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(AuthChangeUserPasswordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}