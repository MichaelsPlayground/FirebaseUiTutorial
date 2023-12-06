package de.androidcrypto.firebaseuitutorial.storage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.FileInformation;
import de.androidcrypto.firebaseuitutorial.models.StorageFileModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.Okhttp3ProgressDownloaderNoDecrypt;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class StorageDownloadFilesAndImagesActivity extends AppCompatActivity {

    /**
     * This class is NOT using FirestoreUi for the upload purposes
     */

    private static final String TAG = StorageDownloadFilesAndImagesActivity.class.getSimpleName();
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private RadioButton rbDownloadFile, rbDownloadImage;
    private Button downloadFile, downloadImage;
    private LinearProgressIndicator downloadProgressIndicator;
    private RecyclerView storageRecyclerView;
    private Uri selectedFileUri;
    private String downloadSelector, downloadSelectedDownloadUrl;
    private String fileStorageReference; // is filled when sending the Intent(Intent.ACTION_OPEN_DOCUMENT), data from FirebaseUtil e.g. STORAGE_FILES_FOLDER_NAME ('files')

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_download_files_and_images);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etStorageUserSignedInUser);
        rbDownloadFile = findViewById(R.id.rbStorageDownloadFile);
        rbDownloadImage = findViewById(R.id.rbStorageDownloadImage);
        downloadFile = findViewById(R.id.btnStorageDownloadUnencryptedFile);
        downloadImage = findViewById(R.id.btnStorageDownloadUnencryptedImage);
        downloadProgressIndicator = findViewById(R.id.lpiStorageDownloadProgress);
        storageRecyclerView = findViewById(R.id.rvStorageDownload);

        // preset is the unencrypted files selection
        downloadSelector = FirebaseUtils.STORAGE_FILES_FOLDER_NAME;

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        /**
         * file type chooser
         */

        View.OnClickListener rbDownloadFileListener = null;
        rbDownloadFileListener = new View.OnClickListener() {
            public void onClick(View v) {
                downloadSectionVisibilityOff();
                downloadSelector = FirebaseUtils.STORAGE_FILES_FOLDER_NAME;
                downloadFile.setVisibility(View.VISIBLE);
            }
        };
        rbDownloadFile.setOnClickListener(rbDownloadFileListener);

        View.OnClickListener rbDownloadImageListener = null;
        rbDownloadImageListener = new View.OnClickListener() {
            public void onClick(View v) {
                downloadSectionVisibilityOff();
                downloadSelector = FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME;
                downloadImage.setVisibility(View.VISIBLE);
            }
        };
        rbDownloadImage.setOnClickListener(rbDownloadImageListener);

        /**
         * download section
         */

        downloadFile.setOnClickListener((v -> {
            downloadSelector = FirebaseUtils.STORAGE_FILES_FOLDER_NAME;
            downloadListFilesBtnClick();

        }));

        downloadImage.setOnClickListener((v -> {
            downloadSelector = FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME;
            downloadListFilesBtnClick();

            /*
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

             */
        }));
    }

    private void downloadListFilesBtnClick() {
        // downloadSelector contains the folder name on storage like FirebaseUtil.FILES_FOLDER_NAME = 'files_une'
        // first we list the available files in this folder on Firebase Cloud Storage for selection by click

        StorageReference ref;
        AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "downloadSelector: " + downloadSelector);

        if (downloadSelector.equals(FirebaseUtils.STORAGE_FILES_FOLDER_NAME)) {
            ref = FirebaseUtils.getStorageCurrentUserFilesReference();
        } else if(downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME)) {
            ref = FirebaseUtils.getStorageCurrentUserImagesReference();
        } else {
            // some data are wrong
            AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "something got wrong, aborted");
            return;
        }

        ref.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            //String actualUserId = FirebaseAuth.getInstance().getUid();
            //FirebaseStorage.getInstance().getReference().child(actualUserId).child("files").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                ArrayList<StorageFileModel> arrayList = new ArrayList<>();
                ArrayList<StorageReference> arrayListSR = new ArrayList<>();
                Iterator<StorageReference> i = listResult.getItems().iterator();
                StorageReference ref;
                while (i.hasNext()) {
                    ref = i.next();
                    arrayListSR.add(ref);
                    StorageFileModel sfm = new StorageFileModel();
                    sfm.setName(ref.getName());
                    ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            System.out.println("*** uri: " + uri + " ***");
                            sfm.setUri(uri);
                            arrayList.add(sfm);
                            System.out.println("arrayList added");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "Can not retrieve a DownloadUrl, aborted");
                            return;
                        }
                    });

                }

                StorageListFilesAdapter adapterSR = new StorageListFilesAdapter(StorageDownloadFilesAndImagesActivity.this, arrayListSR);
                storageRecyclerView.setLayoutManager(new LinearLayoutManager(StorageDownloadFilesAndImagesActivity.this));
                storageRecyclerView.setAdapter(adapterSR);
                storageRecyclerView.setVisibility(View.VISIBLE);
/*
                // this is using class SwipeToDeleteCallback
                // see: https://www.digitalocean.com/community/tutorials/android-recyclerview-swipe-to-delete-undo
                SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getContext()) {
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {


                        //final int position = viewHolder.getAdapterPosition(); // getAdapterPosition is deprecated
                        final int position = viewHolder.getBindingAdapterPosition();
                        final StorageReference item = adapterSR.getArrayList().get(position);
                        //final String item = mAdapter.getData().get(position);

                        adapterSR.removeItem(position);
                        // todo remove from Storage and Firestore


                        System.out.println("actual contents of the arraylist");
                    }
                };

                ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
                itemTouchhelper.attachToRecyclerView(storageRecyclerView);

 */

                adapterSR.setOnItemClickListener(new StorageListFilesAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(StorageReference storageReference) {
                        System.out.println("*** clicked on name: " + storageReference.getName());

                        // get the download url from task
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // Got the download URL for 'users/me/profile.png'
                                System.out.println("*** uri: " + uri + " ***");

                                // set progressIndicator to 0
                                downloadProgressIndicator.setProgress(0);
                                //DownloadManager.Request request = new DownloadManager.Request(uri);
                                String title = null;
                                try {
                                    title = URLUtil.guessFileName(new URL(uri.toString()).toString(), null, null);
                                    downloadSelectedDownloadUrl = new URL(uri.toString()).toString();
                                } catch (MalformedURLException e) {
                                    //throw new RuntimeException(e);
                                    AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "Malformed DownloadUrl, aborted");
                                    return;
                                }
                                // now select the folder and filename on device, we are using the file chooser of Android
                                System.out.println("*** before intent ***");
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                intent.setType("*/*");
                                //intent.setType("image/*/*"); // for image

                                // Optionally, specify a URI for the file that should appear in the
                                // system file picker when it loads.
                                //boolean pickerInitialUri = false;
                                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
                                String storeFilename = title;
                                //String storeFilename = "file3a.txt";
                                //intent.putExtra(Intent.EXTRA_TITLE, storeFilename);
                                intent.putExtra(Intent.EXTRA_TITLE, storeFilename);
                                fileDownloadSaverActivityResultLauncherXX.launch(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "Error on retrieving the DownloadUrl, aborted");
                                return;
                            }
                        });
                    }
                });
            }});
    }

    ActivityResultLauncher<Intent> fileDownloadSaverActivityResultLauncherXX = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        System.out.println("fileDownloadSaverActivityResultLauncher");
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri selectedUri = null;
                        if (resultData != null) {
                            selectedUri = resultData.getData();
                            // Perform operations on the document using its URI.
                            Toast.makeText(StorageDownloadFilesAndImagesActivity.this, "You selected this file for download: " + selectedUri.toString(), Toast.LENGTH_SHORT).show();

                            //String cacheFilename = fileInformation.getFileName() + ".enc";
                            //String encryptedFilename = new File(getContext().getCacheDir(), encryptedFilename;
                            try {
                                //Okhttp3Progress.main();
                                //String cacheFilename = "mt_cook.jpg";
                                //String storageFilename = new File(getContext().getCacheDir(), cacheFilename).getAbsolutePath();
                                //System.out.println("*** storageFilename: " + storageFilename);
                                downloadProgressIndicator.setMax(Math.toIntExact(100));
                                System.out.println("*** download of: " + downloadSelectedDownloadUrl);
                                Okhttp3ProgressDownloaderNoDecrypt downloader = new Okhttp3ProgressDownloaderNoDecrypt(downloadSelectedDownloadUrl, downloadProgressIndicator, StorageDownloadFilesAndImagesActivity.this, selectedUri);
                                downloader.run();
                                System.out.println("*** fileDownloadDecryptSaverActivityResultLauncherXX success");
                                Toast.makeText(StorageDownloadFilesAndImagesActivity.this, "fileDownloadDecryptSaverActivityResultLauncherXX success", Toast.LENGTH_SHORT).show();
                                //Okhttp3ProgressCallback.main(storageFilename);
                            } catch (Exception e) {
                                //throw new RuntimeException(e);
                                System.out.println("*** fileDownloadDecryptSaverActivityResultLauncherXX FAILURE " + e.getMessage());
                                Toast.makeText(StorageDownloadFilesAndImagesActivity.this, "Exception on download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            System.out.println("*** resultData is NULL ***");
                        }
                    }
                }
            });


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
                                            AndroidUtils.showSnackbarGreenShort(downloadFile, "download SUCCESS");
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "File download error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    AndroidUtils.showSnackbarRedLong(downloadFile, "File download error: " + e.getMessage());
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                    downloadProgressIndicator.setMax(Math.toIntExact(snapshot.getTotalByteCount()));
                                    downloadProgressIndicator.setProgress(Math.toIntExact(snapshot.getBytesTransferred()));
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

    private void downloadSectionVisibilityOff() {
        downloadImage.setVisibility(View.GONE);
        downloadImage.setVisibility(View.GONE);
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
                    AndroidUtils.showSnackbarRedLong(downloadFile, "Failed to reload user");
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
                Intent intent = new Intent(StorageDownloadFilesAndImagesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}