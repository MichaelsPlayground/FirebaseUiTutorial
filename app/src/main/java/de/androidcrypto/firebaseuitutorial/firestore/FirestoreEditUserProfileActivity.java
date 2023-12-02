package de.androidcrypto.firebaseuitutorial.firestore;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirestoreEditUserProfileActivity extends AppCompatActivity {

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
    private com.google.android.material.textfield.TextInputEditText userId, userEmail, userPhotoUrl, userPublicKey, userPublicKeyNumber, userName;
    private TextView infoNoData;

    private static final String TAG = FirestoreEditUserProfileActivity.class.getSimpleName();

    // get the data from auth
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private DocumentReference documentReference;
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private View savedView;
    private final String CACHE_FOLDER = "crop";
    private String intermediateName = "1.jpg";
    private String resultName = "2.jpg";
    private Uri imageUriFull, imageUriCrop;
    private final String FILE_PROVIDER_AUTHORITY = "de.androidcrypto.firebaseuitutorial";
    private Uri intermediateProvider;
    private Uri resultProvider;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaActivityResultLauncher;
    private ActivityResultLauncher<Intent> cropActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_edit_user_profile);

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
        userPublicKey = findViewById(R.id.etFirestoreUserPublicKey);
        userPublicKeyNumber = findViewById(R.id.etFirestoreUserPublicKeyNumber);
        profileImageView = findViewById(R.id.ivUserProfileImage);

        // don't show the keyboard on startUp
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        Button cropImage = findViewById(R.id.btnFirestoreUserProfileImageCrop);
        Button uploadProfileImage = findViewById(R.id.btnFirestoreUserProfileImageUpload);

        Button savaData = findViewById(R.id.btnFirestoreUserSave);
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

        /*
        // use the external selector
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "click on profileImage");
                // Launch the photo picker and let the user choose only images.
                //https://developer.android.com/training/data-storage/shared/photopicker

                selectImageUriX = new SelectImageUri(v.getContext(), true);
            }
        });
*/
        cropImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "crop image");
                onCropImage();
            }
        });

        uploadProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "upload image");
                //uploadImage(imageUriFull);
                uploadImage(imageUriCrop);
            }
        });

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
                        writeUserProfile(authUserId, Objects.requireNonNull(userName.getText()).toString(),
                                Objects.requireNonNull(userEmail.getText()).toString(),
                                Objects.requireNonNull(userPhotoUrl.getText()).toString(),
                                Objects.requireNonNull(userPublicKey.getText()).toString(),
                                Objects.requireNonNull(userPublicKeyNumber.getText()).toString(),
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
                        saveBitmapFileToIntermediate(imageUriFull);

                        // start cropping directly
                        onCropImage();

/*
                        Bitmap inputImage = loadFromUri(intermediateProvider);
                        //Bitmap rotated = rotateBitmap(getResizedBitmap(inputImage, 800), imageUriFull);
                        Bitmap rotated = getResizedBitmap(inputImage, 500);
                        profileImageView.setImageBitmap(rotated);

                        int height = profileImageView.getHeight();
                        int width = profileImageView.getWidth();

                        //Bitmap inputImage = uriToBitmap(imageUriFull);
                        String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width) +
                                "\nOriginal Bitmap height: " + inputImage.getHeight() + " width: " + inputImage.getWidth() +
                                " res: " + (inputImage.getHeight() * inputImage.getWidth());
                        //tvFull.setText(imageInfo);
                        Log.d(TAG, "imageInfo: " + imageInfo);

 */
                    } else {
                        Log.d(TAG, "No media selected");
                    }
                });
/*
        pickMediaActivityResultLauncher =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        imageUriFull = uri;
                        saveBitmapFileToIntermediate(imageUriFull);

                        Bitmap inputImage = loadFromUri(intermediateProvider);
                        //Bitmap rotated = rotateBitmap(getResizedBitmap(inputImage, 800), imageUriFull);
                        Bitmap rotated = getResizedBitmap(inputImage, 500);
                        profileImageView.setImageBitmap(rotated);

                        int height = profileImageView.getHeight();
                        int width = profileImageView.getWidth();

                        //Bitmap inputImage = uriToBitmap(imageUriFull);
                        String imageInfo = "height: " + height + " width: " + width + " resolution: " + (height * width) +
                                "\nOriginal Bitmap height: " + inputImage.getHeight() + " width: " + inputImage.getWidth() +
                                " res: " + (inputImage.getHeight() * inputImage.getWidth());
                        //tvFull.setText(imageInfo);
                        Log.d(TAG, "imageInfo: " + imageInfo);
                    } else {
                        Log.d(TAG, "No media selected");
                    }
                });
*/

        cropActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imageUriCrop = resultProvider;
                        Bitmap croppedImage = loadFromUri(resultProvider);
                        profileImageView.setImageBitmap(getResizedBitmap(croppedImage, 500));
                        String imageInfo = "Cropped Bitmap height: " + croppedImage.getHeight() + " width: " + croppedImage.getWidth() +
                                " res: " + (croppedImage.getHeight() * croppedImage.getWidth());
                        //tvCrop.setText(imageInfo);
                        Log.d(TAG, "imageInfo: " + imageInfo);

                        // directly upload image
                        uploadImage(imageUriCrop);
                    }
                });

        /*
        cropActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        imageUriCrop = resultProvider;
                        Bitmap croppedImage = loadFromUri(resultProvider);
                        profileImageView.setImageBitmap(getResizedBitmap(croppedImage, 500));
                        String imageInfo = "Cropped Bitmap height: " + croppedImage.getHeight() + " width: " + croppedImage.getWidth() +
                                " res: " + (croppedImage.getHeight() * croppedImage.getWidth());
                        //tvCrop.setText(imageInfo);
                        Log.d(TAG, "imageInfo: " + imageInfo);
                    }
                });
*/
    }

    /**
     * get an image from gallery
     */

    private void saveBitmapFileToIntermediate(Uri sourceUri) {
        Log.d(TAG, "saveBitmapFileToIntermediate from URI: " + sourceUri);
        try {
            Bitmap bitmap = loadFromUri(sourceUri);
            File imageFile = getPhotoFileUri(intermediateName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                intermediateProvider = FileProvider.getUriForFile(FirestoreEditUserProfileActivity.this, FILE_PROVIDER_AUTHORITY + ".provider", imageFile);
            else
                intermediateProvider = Uri.fromFile(imageFile);

            OutputStream out = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.close();
            Log.d(TAG, "intermediate file written to intermediateProvider: " + intermediateName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // Returns the File for a photo stored on disk given the fileName
    public File getPhotoFileUri(String fileName) {
        // see https://developer.android.com/reference/androidx/core/content/FileProvider
        File mediaStorageDir = new File(getCacheDir(), CACHE_FOLDER);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);
        Log.d(TAG, "getPhotoFileUri for fileName: " + fileName + " is: " + file.getAbsolutePath());
        return file;
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
     * section for image cropping
     */

    private void onCropImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission("com.android.camera", intermediateProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(intermediateProvider, "image/*");

            List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);

            int size = 0;

            if (list != null) {
                grantUriPermission(list.get(0).activityInfo.packageName, intermediateProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                size = list.size();
            }

            if (size == 0) {
                Toast.makeText(this, "Error, wasn't taken image!", Toast.LENGTH_SHORT).show();
            } else {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra("crop", "true");
                //intent.putExtra("scale", true);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                File photoFile = getPhotoFileUri(resultName);
                // wrap File object into a content provider
                // required for API >= 24
                // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
                resultProvider = FileProvider.getUriForFile(FirestoreEditUserProfileActivity.this, FILE_PROVIDER_AUTHORITY + ".provider", photoFile);
                intent.putExtra("return-data", false);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, resultProvider);
                intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

                Intent cropIntent = new Intent(intent);
                // this is using the default cropper
                ResolveInfo res = list.get(0);
                cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                grantUriPermission(res.activityInfo.packageName, resultProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                cropIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

                /*
                if (rbChooseCropperApplicationFixed0.isChecked()) {
                    ResolveInfo res = list.get(0);
                    cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    grantUriPermission(res.activityInfo.packageName, resultProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    cropIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                } else {
                    // granting the rights for all registered cropping apps
                    for (int i = 0; i < list.size(); i++) {
                        ResolveInfo res = list.get(i);
                        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        grantUriPermission(res.activityInfo.packageName, resultProvider, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }
                }
                 */
                cropActivityResultLauncher.launch(cropIntent);
            }
        } else {
            File photoFile = getPhotoFileUri(resultName);
            resultProvider = Uri.fromFile(photoFile);

            Intent intentCrop = new Intent("com.android.camera.action.CROP");
            intentCrop.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intentCrop.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentCrop.setDataAndType(intermediateProvider, "image/*");
            intentCrop.putExtra("crop", "true");
            intentCrop.putExtra("scale", true);
            intentCrop.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intentCrop.putExtra("noFaceDetection", true);
            intentCrop.putExtra("return-data", false);
            intentCrop.putExtra(MediaStore.EXTRA_OUTPUT, resultProvider);
            cropActivityResultLauncher.launch(intentCrop);
        }
    }

    /**
     * section uploading image to storage
     */

    private void uploadImage(Uri uri) {
        showProgressBar();
        //StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("profile_images").child(mAuth.getUid() + ".jpg");
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
                                //mDatabase.child("users").child(authUserId).child("userPhotoUrl")
                                        .setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.i(TAG, "downloadUrl: " + downloadUrl);
                                                    Toast.makeText(FirestoreEditUserProfileActivity.this, "Image saved in database successfuly", Toast.LENGTH_SHORT).show();
                                                    userPhotoUrl.setText(downloadUrl);
                                                    // Download directly from StorageReference using Glide
                                                    // (See MyAppGlideModule for Loader registration)
                                                    GlideApp.with(getApplicationContext())
                                                            .load(downloadUrl)
                                                            .into(profileImageView);
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(FirestoreEditUserProfileActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(FirestoreEditUserProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        Log.i(TAG, "load user data from firestore for user id: " + authUserId);
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
                    if (userModel == null) {
                        Log.i(TAG, "userModel is null, show message");
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
                        hideProgressBar();
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
                Intent intent = new Intent(FirestoreEditUserProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}