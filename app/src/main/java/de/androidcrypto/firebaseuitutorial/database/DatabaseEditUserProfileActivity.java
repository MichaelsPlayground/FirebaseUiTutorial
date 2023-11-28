package de.androidcrypto.firebaseuitutorial.database;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class DatabaseEditUserProfileActivity extends AppCompatActivity {
    private static final String TAG = DatabaseEditUserProfileActivity.class.getSimpleName();

    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private com.google.android.material.textfield.TextInputLayout userNameLayout;
    private com.google.android.material.textfield.TextInputEditText userId, userEmail, userName, userPhotoUrl, userPublicKey, userPublicKeyNumber;
    private Button loadUserProfile, saveUserProfile;
    private ProgressBar progressBar;
    private TextView infoNoData;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseUserReference;
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_edit_user_profile);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        progressBar = findViewById(R.id.pbDatabaseUserProfile);
        infoNoData = findViewById(R.id.tvDatabaseUserNoData);
        signedInUser = findViewById(R.id.etDatabaseUserSignedInUser);
        userId = findViewById(R.id.etDatabaseUserUserId);
        userEmail = findViewById(R.id.etDatabaseUserUserEmail);
        userNameLayout = findViewById(R.id.etDatabaseUserUserNameLayout);
        userName = findViewById(R.id.etDatabaseUserUserName);
        userPhotoUrl = findViewById(R.id.etDatabaseUserPhotoUrl);
        userPublicKey = findViewById(R.id.etDatabaseUserPublicKey);
        userPublicKeyNumber = findViewById(R.id.etDatabaseUserPublicKeyNumber);
        loadUserProfile = findViewById(R.id.btnDatabaseUserLoad);
        saveUserProfile = findViewById(R.id.btnDatabaseUserSave);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        loadUserProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "load user data from database for user id: " + authUserId);
                infoNoData.setVisibility(View.GONE);
                showProgressBar();
                databaseUserReference = FirebaseUtils.getDatabaseUserReference(authUserId);
                if (!authUserId.equals("")) {
                    databaseUserReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            hideProgressBar();
                            if (!task.isSuccessful()) {
                                Log.e(TAG, "Error getting data", task.getException());
                            } else {
                                // check for a null value means no user data were saved before
                                UserModel userModel = task.getResult().getValue(UserModel.class);
                                Log.i(TAG, String.valueOf(userModel));
                                if (userModel == null) {
                                    Log.i(TAG, "userModel is null, show message");
                                    infoNoData.setVisibility(View.VISIBLE);
                                    // get data from user
                                    userId.setText(authUserId);
                                    userEmail.setText(authUserEmail);
                                    userName.setText(FirebaseUtils.usernameFromEmail(authUserEmail));
                                    userPhotoUrl.setText(authPhotoUrl);
                                    userPublicKey.setText("not available");
                                    userPublicKeyNumber.setText("0");
                                } else {
                                    Log.i(TAG, "userModel email: " + userModel.getUserMail());
                                    infoNoData.setVisibility(View.GONE);
                                    // get data from user
                                    userId.setText(authUserId);
                                    userEmail.setText(userModel.getUserMail());
                                    userName.setText(userModel.getUserName());
                                    userPhotoUrl.setText(userModel.getUserPhotoUrl());
                                    userPublicKey.setText(userModel.getUserPublicKey());
                                    userPublicKeyNumber.setText(String.valueOf(userModel.getUserPublicKeyNumber()));
                                }
                            }
                        }
                    });
                } else {
                    // this should not happen but...
                    Toast.makeText(getApplicationContext(),
                            "sign in a user before loading",
                            Toast.LENGTH_SHORT).show();
                    hideProgressBar();
                }

            }
        });

        saveUserProfile.setOnClickListener(new View.OnClickListener() {
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
                        writeUserProfile(authUserId, Objects.requireNonNull(userName.getText()).toString(),
                                Objects.requireNonNull(userEmail.getText()).toString(),
                                Objects.requireNonNull(userPhotoUrl.getText()).toString(),
                                Objects.requireNonNull(userPublicKey.getText()).toString(),
                                Objects.requireNonNull(userPublicKeyNumber.getText()).toString());
                        Snackbar snackbar = Snackbar
                                .make(view, "data written to database", Snackbar.LENGTH_SHORT);
                        snackbar.show();
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
        Log.d(TAG, "onStart");
        // Check if user is signed in (non-null) and update UI accordingly.
        firebaseUser = FirebaseUtils.getCurrentUser();
        if (firebaseUser != null) {
            reload();
        } else {
            signedInUser.setText("no user is signed in");
        }
    }

    private void reload() {
        Log.d(TAG, "reload");
        infoNoData.setVisibility(View.GONE);
        Objects.requireNonNull(firebaseUser).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(firebaseUser);
                    /*
                    Toast.makeText(getApplicationContext(),
                            "Reload successful!",
                            Toast.LENGTH_SHORT).show();
                     */
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
                sb.append("Photo url: ").append("no photo url available").append("\n");
            } else {
                sb.append("Photo url: ").append(authPhotoUrl).append("\n");
            }
            if (user.isEmailVerified()) {
                sb.append("Email verification: ").append("Email address is VERIFIED");
            } else {
                sb.append("Email verification: ").append("Email address is NOT verified");
            }
            signedInUser.setText(sb.toString());

            if (user.isEmailVerified()) {
//                mBinding.verifyEmailButton.setVisibility(View.GONE);
            } else {
//                mBinding.verifyEmailButton.setVisibility(View.VISIBLE);
            }

            // automatically load the user from database
            loadUserFromDatabase();

        } else {
            signedInUser.setText(null);
        }
    }

    private void loadUserFromDatabase() {
        Log.i(TAG, "load user data from database for user id: " + authUserId);
        infoNoData.setVisibility(View.GONE);
        showProgressBar();
        databaseUserReference = FirebaseUtils.getDatabaseUserReference(authUserId);
        if (!authUserId.equals("")) {
            databaseUserReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    hideProgressBar();
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting data", task.getException());
                    } else {
                        // check for a null value means no user data were saved before
                        UserModel userModel = task.getResult().getValue(UserModel.class);
                        Log.i(TAG, String.valueOf(userModel));
                        if (userModel == null) {
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
                                    Objects.requireNonNull(userPublicKeyNumber.getText()).toString());
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
                            userPhotoUrl.setText(userModel.getUserPhotoUrl());
                            userPublicKey.setText(userModel.getUserPublicKey());
                            userPublicKeyNumber.setText(String.valueOf(userModel.getUserPublicKeyNumber()));
                        }
                    }
                }
            });
        } else {
            // this should not happen but...
            Toast.makeText(getApplicationContext(),
                    "sign in a user before loading",
                    Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }
    }

    public void writeUserProfile(String userId, String name, String email, String photoUrl, String publicKey, String publicKeyNumber) {
        int publicKeyNumberInt;
        try {
            publicKeyNumberInt = Integer.parseInt(publicKeyNumber);
        } catch (NumberFormatException e) {
            publicKeyNumberInt = 0;
        }
        UserModel user = new UserModel(userId, name, email, photoUrl, publicKey, publicKeyNumberInt);
        //UserModel user = new UserModel(name, email, photoUrl, publicKey, publicKeyNumberInt);
        databaseUserReference.setValue(user);
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
                Intent intent = new Intent(DatabaseEditUserProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}