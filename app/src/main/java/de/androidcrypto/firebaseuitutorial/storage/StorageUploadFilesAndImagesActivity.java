package de.androidcrypto.firebaseuitutorial.storage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.FileInformationModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class StorageUploadFilesAndImagesActivity extends AppCompatActivity {

    /**
     * This class is NOT using FirestoreUi for the upload purposes
     */

    private static final String TAG = StorageUploadFilesAndImagesActivity.class.getSimpleName();
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private TextView tvDownloadUrl;
    private RadioButton rbUploadFile, rbUploadImage;
    private Button uploadFile, uploadImage;
    private ImageButton copyDownloadUrlToClipboard;
    private LinearProgressIndicator uploadProgressIndicator;
    private LinearLayout llDownloadUrl;
    private Uri selectedFileUri;
    private String fileStorageReference; // is filled when sending the Intent(Intent.ACTION_OPEN_DOCUMENT), data from FirebaseUtil e.g. STORAGE_FILES_FOLDER_NAME ('files')

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_upload_files_and_images);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etStorageUserSignedInUser);
        rbUploadFile = findViewById(R.id.rbStorageUploadFile);
        rbUploadImage = findViewById(R.id.rbStorageUploadImage);
        uploadFile = findViewById(R.id.btnStorageUploadUnencryptedFile);
        uploadImage = findViewById(R.id.btnStorageUploadUnencryptedImage);
        copyDownloadUrlToClipboard = findViewById(R.id.btnStorageUploadCopyDownloadUrl);
        uploadProgressIndicator = findViewById(R.id.lpiStorageUploadProgress);
        tvDownloadUrl = findViewById(R.id.tvStorageUploadDownloadUrl);
        llDownloadUrl = findViewById(R.id.llStorageUploadDownloadUrl);

        /**
         * file type chooser
         */

        View.OnClickListener rbUploadFileListener = null;
        rbUploadFileListener = new View.OnClickListener() {
            public void onClick(View v) {
                uploadSectionVisibilityOff();
                uploadFile.setVisibility(View.VISIBLE);
            }
        };
        rbUploadFile.setOnClickListener(rbUploadFileListener);

        View.OnClickListener rbUploadImageListener = null;
        rbUploadImageListener = new View.OnClickListener() {
            public void onClick(View v) {
                uploadSectionVisibilityOff();
                uploadImage.setVisibility(View.VISIBLE);
            }
        };
        rbUploadImage.setOnClickListener(rbUploadImageListener);

        /**
         * upload section
         */

        uploadFile.setOnClickListener((v -> {
            llDownloadUrl.setVisibility(View.GONE);
            copyDownloadUrlToClipboard.setEnabled(false);
            // select a file in download folder and upload it to firebase cloud storage
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            //boolean pickerInitialUri = false;
            //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
            fileStorageReference = FirebaseUtils.STORAGE_FILES_FOLDER_NAME;
            fileUploadUnencryptedChooserActivityResultLauncher.launch(intent);
        }));

        uploadImage.setOnClickListener((v -> {
            copyDownloadUrlToClipboard.setEnabled(false);
            // select an image in download folder and upload it to firebase cloud storage
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            //boolean pickerInitialUri = false;
            //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
            fileStorageReference = FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME;
            fileUploadUnencryptedChooserActivityResultLauncher.launch(intent);
        }));

        copyDownloadUrlToClipboard.setOnClickListener((v -> {
            // Gets a handle to the clipboard service.
            ClipboardManager clipboard = (ClipboardManager)
                    getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("simple text", tvDownloadUrl.getText());
            // Set the clipboard's primary clip.
            clipboard.setPrimaryClip(clip);
            AndroidUtils.showToast(v.getContext(), "downloadUrl is copied to clipboard");
        }));
    }

    ActivityResultLauncher<Intent> fileUploadUnencryptedChooserActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        if (resultData != null) {
                            llDownloadUrl.setVisibility(View.VISIBLE);
                            selectedFileUri = resultData.getData();
                            String fileStorageReferenceLocal = fileStorageReference;
                            fileStorageReference = ""; // clear after usage
                            FileInformationModel fileInformationModel = getFileInformationFromUri(selectedFileUri);
                            StorageReference ref;
                            if (fileStorageReferenceLocal.equals(FirebaseUtils.STORAGE_FILES_FOLDER_NAME)) {
                                ref = FirebaseUtils.getStorageCurrentUserFilesReference(fileInformationModel.getFileName());
                                fileInformationModel.setFileStorage(fileStorageReferenceLocal);
                            } else {
                                ref = FirebaseUtils.getStorageCurrentUserImagesReference(fileInformationModel.getFileName());
                                fileInformationModel.setFileStorage(fileStorageReferenceLocal);
                            }
                            // now upload the  file / image
                            long actualTime = TimeUtils.getActualUtcZonedDateTime();
                            String actualTimeString = TimeUtils.getZoneDatedStringMediumLocale(actualTime);
                            ref.putFile(selectedFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(getApplicationContext(), "File uploaded with SUCCESS", Toast.LENGTH_SHORT).show();
                                    //ref.getDownloadUrl();
                                    // get download url
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            fileInformationModel.setDownloadUrlString(uri.toString());
                                            fileInformationModel.setActualTime(actualTime);
                                            fileInformationModel.setTimestamp(actualTimeString);
                                            saveFileInformationToDatabaseUserCredentials(fileStorageReferenceLocal, fileInformationModel.getFileName(), fileInformationModel);
                                            saveFileInformationToFirestoreUserCredentials(fileStorageReferenceLocal, fileInformationModel.getFileName(), fileInformationModel);
                                            tvDownloadUrl.setText(uri.toString());
                                            copyDownloadUrlToClipboard.setEnabled(true);
                                            AndroidUtils.showSnackbarGreenShort(uploadFile, "Upload SUCCESS");
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "File upload error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    AndroidUtils.showSnackbarRedLong(uploadFile, "File upload error: " + e.getMessage());
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    uploadProgressIndicator.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
                                    uploadProgressIndicator.setProgress(Math.toIntExact(snapshot.getBytesTransferred()));
                                }
                            });

                            // if it is an image upload a resized version
                            if (fileStorageReferenceLocal.equals(FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME)) {
                                Bitmap fullBitmap;
                                int scaleDivider = 4;
                                StorageReference refResized;
                                try {
                                    fullBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedFileUri);
                                    // 2.b or get a fixed size, e.g. width max 300 px
                                    if (fullBitmap.getWidth() > 300) {
                                        scaleDivider = fullBitmap.getWidth() / 300;
                                    }
                                    // 2. Get the downsized image content as a byte[]
                                    int scaleWidth = fullBitmap.getWidth() / scaleDivider;
                                    int scaleHeight = fullBitmap.getHeight() / scaleDivider;
                                    byte[] downsizedImageBytes =
                                            getDownsizedImageBytes(fullBitmap, scaleWidth, scaleHeight);
                                    FileInformationModel fileInformationModelResized = getFileInformationFromUri(selectedFileUri);
                                    fileInformationModelResized.setFileStorage(FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME);
                                    fileInformationModelResized.setFileSize(downsizedImageBytes.length);
                                    fileInformationModelResized.setActualTime(actualTime);
                                    fileInformationModelResized.setTimestamp(actualTimeString);
                                    // now upload the  resized image
                                    refResized = FirebaseUtils.getStorageCurrentUserImagesResizedReference(fileInformationModel.getFileName());
                                    refResized.putBytes(downsizedImageBytes).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            Toast.makeText(getApplicationContext(), "Resized image uploaded with SUCCESS", Toast.LENGTH_SHORT).show();
                                            //ref.getDownloadUrl();
                                            // get download url
                                            refResized.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                @Override
                                                public void onSuccess(Uri uri) {
                                                    fileInformationModelResized.setDownloadUrlString(uri.toString());
                                                    long actualTime = TimeUtils.getActualUtcZonedDateTime();
                                                    String actualTimeString = TimeUtils.getZoneDatedStringMediumLocale(actualTime);
                                                    fileInformationModelResized.setActualTime(actualTime);
                                                    fileInformationModelResized.setTimestamp(actualTimeString);
                                                    saveFileInformationToDatabaseUserCredentials(FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME, fileInformationModelResized.getFileName(), fileInformationModelResized);
                                                    saveFileInformationToFirestoreUserCredentials(FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME, fileInformationModelResized.getFileName(), fileInformationModelResized);
                                                    //tvDownloadUrl.setText(uri.toString());
                                                    //copyDownloadUrlToClipboard.setEnabled(true);
                                                    AndroidUtils.showSnackbarGreenShort(uploadFile, "Resized Image Upload SUCCESS");
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getApplicationContext(), "File upload error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                            AndroidUtils.showSnackbarRedLong(uploadFile, "File upload error: " + e.getMessage());
                                        }
                                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                            uploadProgressIndicator.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
                                            uploadProgressIndicator.setProgress(Math.toIntExact(snapshot.getBytesTransferred()));
                                        }
                                    });

                                } catch (IOException e) {
                                    // todo work on this
                                    throw new RuntimeException(e);
                                }

                            }

                        }
                    }
                }
            });

    private FileInformationModel getFileInformationFromUri(Uri uri) {
        /*
         * Get the file's content URI from the incoming Intent,
         * then query the server app to get the file's display name
         * and size.
         */
        Context context = getApplicationContext();
        if (context == null) return null;
        Cursor returnCursor = null;
        String mimeType = "";
        String fileName = "";
        long fileSize = 0;
        try {
            returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            mimeType = context.getContentResolver().getType(uri);
            /*
             * Get the column indexes of the data in the Cursor,
             * move to the first row in the Cursor, get the data,
             * and display it.
             */
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            fileName = returnCursor.getString(nameIndex);
            fileSize = returnCursor.getLong(sizeIndex);
        } catch (NullPointerException e) {
            //
        } finally {
            returnCursor.close();
        }
        return new FileInformationModel(mimeType, fileName, fileSize);
    }

    private byte[] getDownsizedImageBytes(Bitmap fullBitmap, int scaleWidth, int scaleHeight) throws IOException {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(fullBitmap, scaleWidth, scaleHeight, true);
        // 2. Instantiate the downsized image content as a byte[]
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] downsizedImageBytes = baos.toByteArray();
        return downsizedImageBytes;
    }

    private void saveFileInformationToDatabaseUserCredentials(String subfolder, String filename, FileInformationModel fileInformationModel) {
        FirebaseUtils.getDatabaseUsersCredentialsSubfolderReference(FirebaseUtils.getCurrentUserId(), subfolder)
                .child(AndroidUtils.fileNameForDatabaseChild(filename)).setValue(FirebaseUtils.convertModelToMap(fileInformationModel))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        AndroidUtils.showToast(getApplicationContext(), "Filestore entry added successfully to Realtime Database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AndroidUtils.showToast(getApplicationContext(), "Filestore entry adding to Realtime Database failed");
                    }
                });
    }

    private void saveFileInformationToFirestoreUserCredentials(String subfolder, String filename, FileInformationModel fileInformationModel) {
        FirebaseUtils.getFirestoreUsersCredentialsSubfolderReference(FirebaseUtils.getCurrentUserId(), subfolder)
                .document(AndroidUtils.fileNameForDatabaseChild(filename)).set(FirebaseUtils.convertModelToMap(fileInformationModel))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        AndroidUtils.showToast(getApplicationContext(), "Filestore entry added successfully to Firestore Database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AndroidUtils.showToast(getApplicationContext(), "Filestore entry adding to Realtime Database failed");
                    }
                });
    }

    private void uploadSectionVisibilityOff() {
        uploadFile.setVisibility(View.GONE);
        uploadImage.setVisibility(View.GONE);
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
                            "Failed to reload user",
                            Toast.LENGTH_SHORT).show();
                    AndroidUtils.showSnackbarRedLong(uploadFile, "Failed to reload user");
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String authDisplayName;
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Data from Auth Database").append("\n");
            sb.append("User id: ").append(user.getUid()).append("\n");
            sb.append("Email: ").append(user.getEmail()).append("\n");
            if (TextUtils.isEmpty(authDisplayName)) {
                sb.append("Display name: ").append("no display name available").append("\n");
            } else {
                sb.append("Display name: ").append(authDisplayName);
            }
            signedInUser.setText(sb.toString());
        } else {
            signedInUser.setText(null);
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
                Intent intent = new Intent(StorageUploadFilesAndImagesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}