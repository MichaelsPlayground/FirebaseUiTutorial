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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class AuthResetUserPasswordActivity extends AppCompatActivity {

    private static final String TAG = AuthResetUserPasswordActivity.class.getSimpleName();

    /**
     * This class is NOT using firestoreUi for the change purposes
     */

    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private com.google.android.material.textfield.TextInputLayout userOldPasswordLayout, userEmailLayout;
    private com.google.android.material.textfield.TextInputEditText userId, userEmail, userOldPassword, userNewPassword;

    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_reset_user_password);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etAuthUserSignedInUser);
        progressBar = findViewById(R.id.pbAuthUser);

        signedInUser = findViewById(R.id.etAuthUserSignedInUser);
        userId = findViewById(R.id.etAuthUserUserId);
        userOldPasswordLayout = findViewById(R.id.etAuthUserOldPasswordLayout);
        userEmail = findViewById(R.id.etAuthUserUserEmail);
        userOldPassword = findViewById(R.id.etAuthUserOldPassword);
        userEmailLayout = findViewById(R.id.etAuthUserUserEmailLayout);
        userNewPassword = findViewById(R.id.etAuthUserNewPassword);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Button resetPassword = findViewById(R.id.btnAuthUserResetPassword);
        resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "send reset user password email for user id: " + authUserId);
                // sanity checks
                FirebaseUser user = FirebaseUtils.getCurrentUser();
                if (user != null) {
                    // check that a user is signed in with an Email/Password provider and not with Google
                    List<? extends UserInfo> providerData = user.getProviderData();
                    boolean isEmailPasswordProvider = false;
                    for (int i = 0; i < providerData.size(); i++) {
                        UserInfo provDat = providerData.get(i);
                        if (provDat.getProviderId().equals("password"))
                            isEmailPasswordProvider = true;
                    }
                    if (!isEmailPasswordProvider) {
                        AndroidUtils.showSnackbarRedLong(signedInUser, "You signed in with Google, so changing of the password is not allowed here");
                        return;
                    }
                }
                String userEmailString = userEmail.getText().toString();
                if (TextUtils.isEmpty(userEmailString)) {
                    userEmailLayout.setError("user email cannot be empty");
                    Toast.makeText(getApplicationContext(),
                            "user email cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    userEmailLayout.setError("");
                }
                if (userEmailString.length() < 6) {
                    userEmailLayout.setError("user email is minimum 6 characters");
                    Toast.makeText(getApplicationContext(),
                            "user email is too short",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    userEmailLayout.setError("");
                }
                showProgressBar();
                FirebaseUtils.getAuth().sendPasswordResetEmail(userEmailString)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent");
                                    AndroidUtils.showSnackbarGreenShort(signedInUser, "A password reset email was send");
                                } else {
                                    AndroidUtils.showSnackbarRedLong(signedInUser, "Could not send a password reset email");
                                }
                                userEmail.setText("");
                                hideProgressBar();
                            }
                        });
            };
        });
    };

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
            StringBuilder sb = new StringBuilder();
            sb.append("Data from Auth Database").append("\n");
            sb.append("User id: ").append(authUserId).append("\n");
            sb.append("Email: ").append(authUserEmail).append("\n");
            if (TextUtils.isEmpty(authDisplayName)) {
                sb.append("Display name: ").append("no display name available").append("\n");
            } else {
                sb.append("Display name: ").append(authDisplayName).append("\n");
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
                Intent intent = new Intent(AuthResetUserPasswordActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}