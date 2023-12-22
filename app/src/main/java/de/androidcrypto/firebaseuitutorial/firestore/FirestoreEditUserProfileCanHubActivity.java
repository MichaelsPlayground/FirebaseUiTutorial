package de.androidcrypto.firebaseuitutorial.firestore;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirestoreEditUserProfileCanHubActivity extends AppCompatActivity {

    private static final String TAG = FirestoreEditUserProfileCanHubActivity.class.getSimpleName();

    /*
    This class uses Glide to download and show the image
    https://egemenhamutcu.medium.com/displaying-images-from-firebase-storage-using-glide-for-kotlin-projects-3e4950f6c103
    https://itecnote.com/tecnote/java-using-firebase-storage-image-with-glide/
    https://firebaseopensource.com/projects/firebase/firebaseui-android/storage/readme
     */

    /**
     * This class is NOT using firestoreUi for the upload purposes
     */

    private CircleImageView profileImageView;
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private com.google.android.material.textfield.TextInputLayout userNameLayout;
    private com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userName;
    private TextView infoNoData;

    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private DocumentReference documentReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private View savedView;
    private Uri imageUriFull;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaActivityResultLauncher;
    private ActivityResultLauncher<CropImageContractOptions> cropImageActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_edit_user_profile_canhub);
        //setCropImageView(com.canhub.cropper.R.layout.crop_image_view);
        //com.canhub.cropper.databinding.CropImageViewBinding

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etFirestoreUserSignedInUser);
        progressBar = findViewById(R.id.pbFirestoreUser);

        infoNoData = findViewById(R.id.tvFirestoreUserNoData);
        signedInUser = findViewById(R.id.etFirestoreUserSignedInUser);
        userId = findViewById(R.id.etFirestoreUserUserId);
        userEmail = findViewById(R.id.etFirestoreUserUserEmail);
        userNameLayout = findViewById(R.id.etFirestoreUserUserNameLayout);
        userName = findViewById(R.id.etFirestoreUserUserName);
        userPhotoUrl = findViewById(R.id.etFirestoreUserPhotoUrl);

        profileImageView = findViewById(R.id.ivUserProfileImage);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        Button savaData = findViewById(R.id.btnFirestoreUserSave);
        savedView = savaData.getRootView();

        // click on profile image to load a new one
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "click on profileImage");

                CropImageOptions cropImageOptions = new CropImageOptions();
                cropImageOptions.imageSourceIncludeGallery = true;
                cropImageOptions.imageSourceIncludeCamera = false;
                cropImageOptions.cropperLabelText = "hello";
                cropImageOptions.intentChooserTitle = "chooserTitle";
                cropImageOptions.activityTitle = "Act title";

                cropImageOptions.fixAspectRatio = true;
                cropImageOptions.aspectRatioX = 1;
                cropImageOptions.aspectRatioY = 1;
                //cropImageOptions.maxCropResultHeight = 500;
                //cropImageOptions.maxCropResultWidth = 500;

                //cropImageOptions.outputRequestHeight = 500;
                //cropImageOptions.outputRequestWidth = 500;

                cropImageOptions.multiTouchEnabled = true;
                cropImageOptions.autoZoomEnabled = true;

                //cropImageOptions.guidelines = CropImageView.Guidelines.ON;
                cropImageOptions.guidelines = com.canhub.cropper.CropImageView.Guidelines.ON;
                CropImageContractOptions cropImageContractOptions = new CropImageContractOptions(imageUriFull, cropImageOptions);
                cropImageActivityResultLauncher.launch(cropImageContractOptions);

            }
        });

        savaData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "save user data from database for user id: " + authUserId);
                //savedView = view;
                saveData();
            }
        });

        /**
         * section for launcher
         */

        cropImageActivityResultLauncher = registerForActivityResult(new CropImageContract(), result -> {
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (result != null) {

                // Use the returned uri.
                Uri uriContent = result.getUriContent();
                String uriFilePath = result.getUriFilePath(getApplicationContext(), false);
                //Bitmap cropped = BitmapFactory.decodeFile(uri.getUriFilePath(getApplicationContext(), true));
                //iv1.setImageUriAsync(uriContent);

                Bitmap cropped = getResizedBitmap(BitmapFactory.decodeFile(result.getUriFilePath(getApplicationContext(), true)),500);
                //profileImageView.setImageBitmap(cropped);
                //imageView2.setImageBitmap(cropped);
                profileImageView.setImageBitmap(cropped);
                //profileImageView.setImageURI(uriContent);

                //uploadImage(uriContent);
                uploadImageBitmap(cropped);


                System.out.println("uriContent: " + uriContent);
                System.out.println("uriFilePath: " + uriFilePath);
                System.out.println("piv height: " + profileImageView.getHeight() + " width: " + profileImageView.getWidth());

            } else {
                System.out.println("*** error ***");
            }
        });

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
                        //resizeImage(imageUriFull);
                    } else {
                        Log.d(TAG, "No media selected");
                    }
                });
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /**
     * section uploading image to storage
     */

    private void uploadImageBitmap(Bitmap bitmap) {
        showProgressBar();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] data = baos.toByteArray();
        StorageReference storageReference = FirebaseUtils.getStorageProfileImagesReference(firebaseAuth.getUid());
        storageReference.putBytes(data)
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
                                                    Toast.makeText(FirestoreEditUserProfileCanHubActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                    userPhotoUrl.setText(downloadUrl);
                                                    // Download directly from StorageReference using Glide
                                                    // (See MyAppGlideModule for Loader registration)
                                                    GlideApp.with(getApplicationContext())
                                                            .load(downloadUrl)
                                                            .into(profileImageView);
                                                    saveData();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(FirestoreEditUserProfileCanHubActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(FirestoreEditUserProfileCanHubActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        hideProgressBar();
                    }
                });
    }

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
                                                    Toast.makeText(FirestoreEditUserProfileCanHubActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                    userPhotoUrl.setText(downloadUrl);
                                                    // Download directly from StorageReference using Glide
                                                    // (See MyAppGlideModule for Loader registration)
                                                    GlideApp.with(getApplicationContext())
                                                            .load(downloadUrl)
                                                            .into(profileImageView);
                                                    saveData();
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(FirestoreEditUserProfileCanHubActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(FirestoreEditUserProfileCanHubActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                        Objects.requireNonNull(""),
                        Objects.requireNonNull(""),
                        FirebaseUtils.USER_ONLINE,
                        TimeUtils.getActualUtcZonedDateTime()
                );
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
        if (!TextUtils.isEmpty(authUserId)) {
            documentReference = FirebaseUtils.getFirestoreUserReference(authUserId);
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    hideProgressBar();
                    Log.i(TAG, "success on loading data from firestore database");
                    System.out.println("*** " + documentSnapshot.toString());
                    String un = (String) documentSnapshot.get("userName");
                    System.out.println("*** un: " + un);
                    UserModel userModel = documentSnapshot.toObject(UserModel.class);
                    Log.d(TAG, "User model: " + String.valueOf(userModel));
                    if ((userModel == null) || (userModel.getUserId() == null)) {
                        Log.i(TAG, "userModel is null, show message");
                        infoNoData.setVisibility(View.VISIBLE);
                        // get data from user
                        userId.setText(authUserId);
                        userEmail.setText(authUserEmail);
                        userName.setText(FirebaseUtils.usernameFromEmail(authUserEmail));
                        userPhotoUrl.setText(authPhotoUrl);

                        // automatically save a new dataset
                        showProgressBar();
                        writeUserProfile(authUserId, Objects.requireNonNull(userName.getText()).toString(),
                                Objects.requireNonNull(userEmail.getText()).toString(),
                                Objects.requireNonNull(userPhotoUrl.getText()).toString(),
                                "",
                                "",
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
        FirebaseUtils.getFirestoreUserReference(userId).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "DocumentSnapshot successfully written for userId: " + userId);
                        Snackbar snackbar = Snackbar
                                .make(savedView, "data written to database", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "Error writing document for userId: " + userId, e);
                        Snackbar snackbar = Snackbar
                                .make(savedView, "Error on write user data to database", Snackbar.LENGTH_SHORT);
                        snackbar.show();
                    }
                });
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
                Intent intent = new Intent(FirestoreEditUserProfileCanHubActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}