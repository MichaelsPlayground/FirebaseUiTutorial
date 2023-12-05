package de.androidcrypto.firebaseuitutorial.storage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.FileInformation;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class StorageUploadFilesAndImagesActivity extends AppCompatActivity {

    private static final String TAG = StorageUploadFilesAndImagesActivity.class.getSimpleName();
    private RadioButton rbUploadFile, rbUploadImage;
    private Button uploadFile, uploadImage;
    private LinearProgressIndicator uploadProgressIndicator;
    private Uri selectedFileUri;
    private String fileStorageReference; // is filled when sending the Intent(Intent.ACTION_OPEN_DOCUMENT), data from FirebaseUtil e.g. STORAGE_FILES_FOLDER_NAME ('files')

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_upload_files_and_images);

        rbUploadFile = findViewById(R.id.rbStorageUploadFile);
        rbUploadImage = findViewById(R.id.rbStorageUploadImage);
        uploadFile = findViewById(R.id.btnStorageUploadUnencryptedFile);
        uploadImage = findViewById(R.id.btnStorageUploadUnencryptedImage);
        uploadProgressIndicator = findViewById(R.id.lpiStorageUploadProgress);


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

        }));

        uploadImage.setOnClickListener((v -> {
            // select a file in download folder and upload it to firebase cloud storage
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
            boolean pickerInitialUri = false;
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

            fileStorageReference = FirebaseUtils.STORAGE_FILES_FOLDER_NAME;
            fileUploadUnencryptedChooserActivityResultLauncher.launch(intent);
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
                            selectedFileUri = resultData.getData();
                            String fileStorageReferenceLocal = fileStorageReference;
                            fileStorageReference = ""; // clear after usage
                            FileInformation fileInformation = getFileInformationFromUri(selectedFileUri);
                            StorageReference ref;
                            if (fileStorageReferenceLocal.equals(FirebaseUtils.STORAGE_FILES_FOLDER_NAME)) {
                                ref = FirebaseUtils.getStorageCurrentUserFilesReference(fileInformation.getFileName());
                                fileInformation.setFileStorage(fileStorageReferenceLocal);
                            } else {
                                ref = FirebaseUtils.getStorageCurrentUserImagesReference(fileInformation.getFileName());
                                fileInformation.setFileStorage(fileStorageReferenceLocal);
                            }
                            // now upload the  file / image
                            ref.putFile(selectedFileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Toast.makeText(getApplicationContext(), "File uploaded with SUCCESS", Toast.LENGTH_SHORT).show();
                                    //ref.getDownloadUrl();
                                    // get download url
                                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            fileInformation.setDownloadUrl(uri);
                                            long actualTime = TimeUtils.getActualUtcZonedDateTime();
                                            String actualTimeString = TimeUtils.getZoneDatedStringMediumLocale(actualTime);
                                            fileInformation.setActualTime(actualTime);
                                            fileInformation.setTimestamp(actualTimeString);
                                            addFileInformationToDatabaseUserCollection(fileStorageReferenceLocal, fileInformation.getFileName(), fileInformation);
                                            addFileInformationToFirestoreUserCollection(fileStorageReferenceLocal, fileInformation.getFileName(), fileInformation);
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "File upload error: " + e.getMessage(), Toast.LENGTH_LONG).show();

                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    uploadProgressIndicator.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
                                    uploadProgressIndicator.setProgress(Math.toIntExact(snapshot.getBytesTransferred()));
                                }
                            });
                        }
                    }
                }
            });

    private FileInformation getFileInformationFromUri(Uri uri) {
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
        return new FileInformation(mimeType, fileName, fileSize);
    }

    private void addFileInformationToDatabaseUserCollection(String subfolder, String filename, FileInformation fileInformation) {
        /* todo add code
        FirebaseUtils.currentUserFilesCollectionReference(subfolder, filename)
                .set(fileInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        AndroidUtils.showToast(getApplicationContext(), "Filestore entry added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AndroidUtils.showToast(getApplicationContext(), "Filestore entry adding failed");
                    }
                });

         */
    }

    private void addFileInformationToFirestoreUserCollection(String subfolder, String filename, FileInformation fileInformation) {
        /* todo add code
        FirebaseUtils.currentUserFilesCollectionReference(subfolder, filename)
                .set(fileInformation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        AndroidUtils.showToast(getApplicationContext(), "Filestore entry added successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        AndroidUtils.showToast(getApplicationContext(), "Filestore entry adding failed");
                    }
                });

         */
    }

    private void uploadSectionVisibilityOff() {
        uploadFile.setVisibility(View.GONE);
        uploadImage.setVisibility(View.GONE);
    }

}