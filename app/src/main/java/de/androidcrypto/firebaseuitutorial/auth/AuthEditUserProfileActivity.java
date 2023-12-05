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
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class AuthEditUserProfileActivity extends AppCompatActivity {

    /**
     * This class is NOT using firestoreUi for the edit purposes
     */

    private CircleImageView profileImageView;
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private com.google.android.material.textfield.TextInputLayout userNameLayout;
    private com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userName;
    private TextView infoNoData;

    private static final String TAG = AuthEditUserProfileActivity.class.getSimpleName();

    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_edit_user_profile);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etAuthUserSignedInUser);
        progressBar = findViewById(R.id.pbAuthUser);

        infoNoData = findViewById(R.id.tvAuthUserNoData);
        signedInUser = findViewById(R.id.etAuthUserSignedInUser);
        userId = findViewById(R.id.etAuthUserUserId);
        userEmail = findViewById(R.id.etAuthUserUserEmail);
        userNameLayout = findViewById(R.id.etAuthUserUserNameLayout);
        userName = findViewById(R.id.etAuthUserUserName);
        userPhotoUrl = findViewById(R.id.etAuthUserPhotoUrl);
        profileImageView = findViewById(R.id.ivUserProfileImage);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Button savaData = findViewById(R.id.btnAuthUserSave);
        savaData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "save user data from database for user id: " + authUserId);
                // sanity check
                String userNameString = userName.getText().toString();
                if (TextUtils.isEmpty(userNameString)) {
                    userNameLayout.setError("userName cannot be empty");
                    Toast.makeText(getApplicationContext(),
                            "userName cannot be empty",
                            Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    userNameLayout.setError("");
                }
                showProgressBar();
                if (!authUserId.equals("")) {
                    if (!Objects.requireNonNull(userId.getText()).toString().equals("")) {
                        infoNoData.setVisibility(View.GONE);
                        Snackbar snackbar = Snackbar
                                .make(view, "data written to AUTH database", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                        // write the data only to the auth database
                        FirebaseUtils.writeToCurrentUserAuthData(userName.getText().toString(), userPhotoUrl.getText().toString());
                        FirebaseUtils.copyAuthDatabaseToDatabaseUser();
                        FirebaseUtils.copyAuthDatabaseToFirestoreUser();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "load user data before saving",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // this should not happen, but...
                    Toast.makeText(getApplicationContext(),
                            "sign in a user before saving",
                            Toast.LENGTH_SHORT).show();
                }
                hideProgressBar();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = FirebaseUtils.getCurrentUser();
        if(currentUser != null){
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
            userName.setText(authDisplayName);
            userEmail.setText(authUserEmail);
            userPhotoUrl.setText(authPhotoUrl);
            if (!TextUtils.isEmpty(authPhotoUrl)) {
                // Download directly from StorageReference using Glide
                // (See MyAppGlideModule for Loader registration)
                GlideApp.with(getApplicationContext())
                        .load(authPhotoUrl)
                        .into(profileImageView);
            }
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
                Intent intent = new Intent(AuthEditUserProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}