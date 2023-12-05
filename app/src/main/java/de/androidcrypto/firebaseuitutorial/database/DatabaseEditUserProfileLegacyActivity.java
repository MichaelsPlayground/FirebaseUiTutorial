package de.androidcrypto.firebaseuitutorial.database;

import android.content.Intent;
import android.net.Uri;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class DatabaseEditUserProfileLegacyActivity extends AppCompatActivity {

    /**
     * This class is NOT using firestoreUi for the upload purposes
     */

    private CircleImageView profileImageView;
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private com.google.android.material.textfield.TextInputLayout userNameLayout;
    private com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userPublicKeyNumber, userName;
    private TextView infoNoData;

    private static final String TAG = DatabaseEditUserProfileLegacyActivity.class.getSimpleName();

    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private DatabaseReference databaseUserReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private View savedView;
    private Uri imageUriFull;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_edit_user_profile);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etDatabaseUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseUser);

        infoNoData = findViewById(R.id.tvDatabaseUserNoData);
        signedInUser = findViewById(R.id.etDatabaseUserSignedInUser);
        userId = findViewById(R.id.etDatabaseUserUserId);
        userEmail = findViewById(R.id.etDatabaseUserUserEmail);
        userNameLayout = findViewById(R.id.etDatabaseUserUserNameLayout);
        userName = findViewById(R.id.etDatabaseUserUserName);
        userPhotoUrl = findViewById(R.id.etDatabaseUserPhotoUrl);
        userPublicKey = findViewById(R.id.etDatabaseUserPublicKey);
        userPublicKeyNumber = findViewById(R.id.etDatabaseUserPublicKeyNumber);

        profileImageView = findViewById(R.id.ivUserProfileImage);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        Button savaData = findViewById(R.id.btnDatabaseUserSave);
        savedView = savaData.getRootView();

        // click on profile image to load a new one
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "click on profileImage");
                // Launch the photo picker and let the user choose only images.
                https://developer.android.com/training/data-storage/shared/photopicker
                pickMediaActivityResultLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        savaData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "save user data from database for user id: " + authUserId);
                saveData();
            }
        });

        /**
         * section for launcher
         */

        // android 13 photo picker
        // Registers a photo picker activity launcher in single-select mode.
        // https://developer.android.com/training/data-storage/shared/photopicker
        pickMediaActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        imageUriFull = uri;
                        // call legacy cropper
                        resizeImage(imageUriFull);
                    } else {
                        Log.d(TAG, "No media selected");
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            profileImageView.setImageURI(result.getUri());
            uploadImage(result.getUri());
        }
    }

    private void resizeImage(Uri data) {
        CropImage.activity(data)
                .setMultiTouchEnabled(true)
                .setAspectRatio(1 , 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                //.setMaxCropResultSize(512, 512)
                //.setOutputCompressQuality(50)
                .start(this);
    }

    /**
     * section uploading image to storage
     */

    private void uploadImage(Uri uri) {
        showProgressBar();
        StorageReference storageReference = FirebaseUtils.getStorageProfileImagesReference(firebaseAuth.getUid());
                storageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        final Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                        firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final String downloadUrl = uri.toString();
                                FirebaseUtils.getDatabaseUserFieldReference(authUserId, FirebaseUtils.DATABASE_USER_PHOTO_URL_FIELD)
                                        .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i(TAG, "downloadUrl: " + downloadUrl);
                                                    Toast.makeText(DatabaseEditUserProfileLegacyActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                    userPhotoUrl.setText(downloadUrl);
                                                    // Download directly from StorageReference using Glide
                                                    // (See MyAppGlideModule for Loader registration)
                                                    GlideApp.with(getApplicationContext())
                                                            .load(downloadUrl)
                                                            .into(profileImageView);
                                                    saveData();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(DatabaseEditUserProfileLegacyActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                                }
                                                hideProgressBar();
                                            }
                                        });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DatabaseEditUserProfileLegacyActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        hideProgressBar();
                    }
                });
    }

    private void saveData() {
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
                        Objects.requireNonNull(userPublicKeyNumber.getText()).toString(),
                        FirebaseUtils.USER_ONLINE,
                        TimeUtils.getActualUtcZonedDateTime()
                );
                Snackbar snackbar = Snackbar
                        .make(savedView, "data written to database", Snackbar.LENGTH_SHORT);
                snackbar.show();
                // additionally write the data to the auth database
                FirebaseUtils.writeToCurrentUserAuthData(userName.getText().toString(), userPhotoUrl.getText().toString());
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
            });
        } else {
            // this should not happen but...
            Toast.makeText(getApplicationContext(),
                    "sign in a user before loading",
                    Toast.LENGTH_SHORT).show();
            hideProgressBar();
        }
    }

    public void writeUserProfile(String userId, String name, String email, String photoUrl, String publicKey, String publicKeyNumber, String userOnlineString, long userLastOnlineTime) {
        int publicKeyNumberInt;
        try {
            publicKeyNumberInt = Integer.parseInt(publicKeyNumber);
        } catch (NumberFormatException e) {
            publicKeyNumberInt = 0;
        }
        UserModel user = new UserModel(userId, name, email, photoUrl, publicKey, publicKeyNumberInt, userOnlineString, userLastOnlineTime);
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
                Intent intent = new Intent(DatabaseEditUserProfileLegacyActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}