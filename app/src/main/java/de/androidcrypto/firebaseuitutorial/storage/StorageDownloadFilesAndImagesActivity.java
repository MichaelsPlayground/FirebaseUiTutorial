package de.androidcrypto.firebaseuitutorial.storage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.StorageFileModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.Okhttp3ProgressDownloader;

public class StorageDownloadFilesAndImagesActivity extends AppCompatActivity {

    /**
     * This class is NOT using FirestoreUi for the download purposes
     */

    private static final String TAG = StorageDownloadFilesAndImagesActivity.class.getSimpleName();
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private RadioButton rbDownloadFile, rbDownloadImage;
    private Button downloadFileOrImage;
    private LinearProgressIndicator downloadProgressIndicator;
    private RecyclerView storageRecyclerView;
    private String downloadSelector, downloadSelectedDownloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_download_files_and_images);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etStorageUserSignedInUser);
        rbDownloadFile = findViewById(R.id.rbStorageDownloadFile);
        rbDownloadImage = findViewById(R.id.rbStorageDownloadImage);
        downloadFileOrImage = findViewById(R.id.btnStorageDownloadUnencryptedFileOrImage);
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
                downloadFileOrImage.setText("download a file");
            }
        };
        rbDownloadFile.setOnClickListener(rbDownloadFileListener);

        View.OnClickListener rbDownloadImageListener = null;
        rbDownloadImageListener = new View.OnClickListener() {
            public void onClick(View v) {
                downloadSectionVisibilityOff();
                downloadSelector = FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME;
                downloadFileOrImage.setText("download an image");
            }
        };
        rbDownloadImage.setOnClickListener(rbDownloadImageListener);

        /**
         * download section
         */

        downloadFileOrImage.setOnClickListener((v -> {
            downloadListFilesBtnClick();
        }));

    }

    private void downloadListFilesBtnClick() {
        // downloadSelector contains the folder name on storage like FirebaseUtil.FILES_FOLDER_NAME = 'files_une'
        // first we list the available files in this folder on Firebase Cloud Storage for selection by click

        StorageReference ref;
        if (downloadSelector.equals(FirebaseUtils.STORAGE_FILES_FOLDER_NAME)) {
            ref = FirebaseUtils.getStorageCurrentUserFilesReference();
        } else if(downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME)) {
            ref = FirebaseUtils.getStorageCurrentUserImagesReference();
        } else {
            // some data are wrong
            AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "something got wrong, aborted");
            AndroidUtils.showSnackbarRedLong(downloadFileOrImage, "something got wrong, aborted");
            return;
        }

        ref.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
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
                            sfm.setUri(uri);
                            arrayList.add(sfm);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "Can not retrieve a DownloadUrl, aborted");
                            AndroidUtils.showSnackbarRedLong(downloadFileOrImage,"Can not retrieve a DownloadUrl, aborted");
                            return;
                        }
                    });
                }

                StorageListFilesAdapter adapterSR = new StorageListFilesAdapter(StorageDownloadFilesAndImagesActivity.this, arrayListSR);
                storageRecyclerView.setLayoutManager(new LinearLayoutManager(StorageDownloadFilesAndImagesActivity.this));
                storageRecyclerView.setAdapter(adapterSR);
                storageRecyclerView.setVisibility(View.VISIBLE);

                adapterSR.setOnItemClickListener(new StorageListFilesAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(StorageReference storageReference) {
                        // get the download url from task
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // set progressIndicator to 0
                                downloadProgressIndicator.setProgress(0);
                                String title = null;
                                try {
                                    title = URLUtil.guessFileName(new URL(uri.toString()).toString(), null, null);
                                    downloadSelectedDownloadUrl = new URL(uri.toString()).toString();
                                } catch (MalformedURLException e) {
                                    AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "Malformed DownloadUrl, aborted");
                                    return;
                                }
                                // now select the folder and filename on device, we are using the file chooser of Android
                                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                if (downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME)) {
                                    intent.setType("image/*/*"); // for image
                                } else {
                                    intent.setType("*/*");
                                }

                                // Optionally, specify a URI for the file that should appear in the
                                // system file picker when it loads.
                                //boolean pickerInitialUri = false;
                                //intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);
                                String storeFilename = title;
                                intent.putExtra(Intent.EXTRA_TITLE, storeFilename);
                                fileDownloadSaverActivityResultLauncher.launch(intent);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                AndroidUtils.showToast(StorageDownloadFilesAndImagesActivity.this, "Error on retrieving the DownloadUrl, aborted");
                                AndroidUtils.showSnackbarRedLong(downloadFileOrImage,"Error on retrieving the DownloadUrl, aborted");
                                return;
                            }
                        });
                    }
                });
            }});
    }

    ActivityResultLauncher<Intent> fileDownloadSaverActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent resultData = result.getData();
                        // The result data contains a URI for the document or directory that
                        // the user selected.
                        Uri selectedUri = null;
                        if (resultData != null) {
                            selectedUri = resultData.getData();
                            // Perform operations on the document using its URI.
                           try {
                                downloadProgressIndicator.setMax(Math.toIntExact(100));
                                Okhttp3ProgressDownloader downloader = new Okhttp3ProgressDownloader(downloadSelectedDownloadUrl, downloadProgressIndicator, StorageDownloadFilesAndImagesActivity.this, selectedUri);
                                downloader.run();
                                Toast.makeText(StorageDownloadFilesAndImagesActivity.this, "Download success", Toast.LENGTH_SHORT).show();
                                AndroidUtils.showSnackbarGreenShort(downloadFileOrImage, "Download SUCCESS");
                            } catch (Exception e) {
                                Toast.makeText(StorageDownloadFilesAndImagesActivity.this, "Exception on download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                AndroidUtils.showSnackbarRedLong(downloadFileOrImage,"Exception on download the file, aborted");
                            }
                        } else {
                            AndroidUtils.showSnackbarRedLong(downloadFileOrImage,"No save file reference got, aborted");
                        }
                    }
                }
            });

    private void downloadSectionVisibilityOff() {
        // no entries
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
                    Toast.makeText(getApplicationContext(),
                            "Failed to reload user",
                            Toast.LENGTH_SHORT).show();
                    AndroidUtils.showSnackbarRedLong(downloadFileOrImage, "Failed to reload user");
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
                sb.append("Display name: ").append("no display name available");
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