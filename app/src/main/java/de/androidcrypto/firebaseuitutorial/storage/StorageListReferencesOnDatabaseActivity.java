package de.androidcrypto.firebaseuitutorial.storage;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ProgressBar;
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

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.database.DatabaseRecentMessageModelAdapter;
import de.androidcrypto.firebaseuitutorial.models.FileInformation;
import de.androidcrypto.firebaseuitutorial.models.RecentMessageModel;
import de.androidcrypto.firebaseuitutorial.models.StorageFileModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.Okhttp3ProgressDownloader;

public class StorageListReferencesOnDatabaseActivity extends AppCompatActivity {

    /**
     * This class is NOT using FirestoreUi for the download purposes
     */

    private static final String TAG = StorageListReferencesOnDatabaseActivity.class.getSimpleName();
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private RadioButton rbListFiles, rbListImages, rbListResizedImages;
    private Button listFilesOrImages;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private String downloadSelector, downloadSelectedDownloadUrl;
    private DatabaseReference listStorageFileReferencesDatabase;
    private StorageListFileReferencesModelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_list_references_on_database);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etStorageUserSignedInUser);
        rbListFiles = findViewById(R.id.rbStorageListReferencesDatabaseFiles);
        rbListImages = findViewById(R.id.rbStorageListReferencesDatabaseImages);
        rbListResizedImages = findViewById(R.id.rbStorageListReferencesDatabaseResizedImages);
        listFilesOrImages = findViewById(R.id.btnStorageListReferencesDatabaseFilesOrImages);
        progressBar = findViewById(R.id.pbStorageListReferencesDatabaseFiles);
        recyclerView = findViewById(R.id.rvStorageListReferencesDatabase);

        // preset is the unencrypted files selection
        downloadSelector = FirebaseUtils.STORAGE_FILES_FOLDER_NAME;

        // To display the Recycler view linearlayout
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(false);
        layoutManager.setStackFromEnd(false);
        recyclerView.setLayoutManager(layoutManager);

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        /**
         * file type chooser
         */

        View.OnClickListener rbListFilesListener = null;
        rbListFilesListener = new View.OnClickListener() {
            public void onClick(View v) {
                downloadSectionVisibilityOff();
                downloadSelector = FirebaseUtils.STORAGE_FILES_FOLDER_NAME;
                listFilesOrImages.setText("list files");
            }
        };
        rbListFiles.setOnClickListener(rbListFilesListener);

        View.OnClickListener rbListImagesListener = null;
        rbListImagesListener = new View.OnClickListener() {
            public void onClick(View v) {
                downloadSectionVisibilityOff();
                downloadSelector = FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME;
                listFilesOrImages.setText("list images");
            }
        };
        rbListImages.setOnClickListener(rbListImagesListener);

        View.OnClickListener rbListResizedImagesListener = null;
        rbListResizedImagesListener = new View.OnClickListener() {
            public void onClick(View v) {
                downloadSectionVisibilityOff();
                downloadSelector = FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME;
                listFilesOrImages.setText("list images");
            }
        };
        rbListResizedImages.setOnClickListener(rbListResizedImagesListener);

        /**
         * download section
         */

        listFilesOrImages.setOnClickListener((v -> {
            //listFilesBtnClick();
            listStorageFileReferencesOnDatabase();
        }));

    }

    private void listStorageFileReferencesOnDatabase() {
        if (downloadSelector.equals(FirebaseUtils.STORAGE_FILES_FOLDER_NAME)) {
            listStorageFileReferencesDatabase = FirebaseUtils.getDatabaseCurrentUserCredentialsFilesReference();
        } else if(downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME)) {
            listStorageFileReferencesDatabase = FirebaseUtils.getDatabaseCurrentUserCredentialsImagesReference();
        } else if(downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME)) {
            listStorageFileReferencesDatabase = FirebaseUtils.getDatabaseCurrentUserCredentialsImagesResizedReference();
        } else {
            // some data are wrong
            AndroidUtils.showToast(StorageListReferencesOnDatabaseActivity.this, "something got wrong, aborted");
            AndroidUtils.showSnackbarRedLong(listFilesOrImages, "something got wrong, aborted");
            return;
        }
        // This is a class provided by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        Query orderedQuery = listStorageFileReferencesDatabase
                //.orderByKey();
                //.orderByChild("fn");
                //.orderByChild("metrics/fileName");
                .orderByChild("fileName");
                //.orderByChild("fileSize");
        //.orderByChild("mimeType");
        //.limitToLast(5);
        FirebaseRecyclerOptions<FileInformation> options
                = new FirebaseRecyclerOptions.Builder<FileInformation>()
                .setQuery(orderedQuery, FileInformation.class)
                //.setQuery(listStorageFileReferencesDatabase, FileInformation.class)
                //.setQuery(recentMessagesDatabase, RecentMessageModel.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new StorageListFileReferencesModelAdapter(options, this);
        // Connecting Adapter class with the Recycler view*/
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                //recyclerView.smoothScrollToPosition(0); // scroll to top document
                //recyclerView.smoothScrollToPosition(0); // scroll to top document
                //recyclerView.smoothScrollToPosition(0); // scroll to last document
                recyclerView.smoothScrollToPosition(itemCount - 1); // scroll to last document
                recyclerView.smoothScrollToPosition(adapter.getItemCount()); // scroll to last document
            }
        });
    }


    private void listFilesBtnClick() {
        // downloadSelector contains the folder name on storage like FirebaseUtil.FILES_FOLDER_NAME = 'files_une'
        // first we list the available files in this folder on Firebase Cloud Storage for selection by click

        StorageReference ref;
        if (downloadSelector.equals(FirebaseUtils.STORAGE_FILES_FOLDER_NAME)) {
            ref = FirebaseUtils.getStorageCurrentUserFilesReference();
        } else if(downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME)) {
            ref = FirebaseUtils.getStorageCurrentUserImagesReference();
        } else if(downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME)) {
            ref = FirebaseUtils.getStorageCurrentUserImagesResizedReference();
        } else {
            // some data are wrong
            AndroidUtils.showToast(StorageListReferencesOnDatabaseActivity.this, "something got wrong, aborted");
            AndroidUtils.showSnackbarRedLong(listFilesOrImages, "something got wrong, aborted");
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
                            AndroidUtils.showToast(StorageListReferencesOnDatabaseActivity.this, "Can not retrieve a DownloadUrl, aborted");
                            AndroidUtils.showSnackbarRedLong(listFilesOrImages,"Can not retrieve a DownloadUrl, aborted");
                            return;
                        }
                    });
                }

                StorageListFilesAdapter adapterSR = new StorageListFilesAdapter(StorageListReferencesOnDatabaseActivity.this, arrayListSR);
                recyclerView.setLayoutManager(new LinearLayoutManager(StorageListReferencesOnDatabaseActivity.this));
                recyclerView.setAdapter(adapterSR);
                recyclerView.setVisibility(View.VISIBLE);

                adapterSR.setOnItemClickListener(new StorageListFilesAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(StorageReference storageReference) {
                        // get the download url from task
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // set progressIndicator to 0
                                //listProgressIndicator.setProgress(0);
                                String title = null;
                                try {
                                    title = URLUtil.guessFileName(new URL(uri.toString()).toString(), null, null);
                                    downloadSelectedDownloadUrl = new URL(uri.toString()).toString();
                                } catch (MalformedURLException e) {
                                    AndroidUtils.showToast(StorageListReferencesOnDatabaseActivity.this, "Malformed DownloadUrl, aborted");
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
                                AndroidUtils.showToast(StorageListReferencesOnDatabaseActivity.this, "Error on retrieving the DownloadUrl, aborted");
                                AndroidUtils.showSnackbarRedLong(listFilesOrImages,"Error on retrieving the DownloadUrl, aborted");
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
                                //listProgressIndicator.setMax(Math.toIntExact(100));
/*
                                Okhttp3ProgressDownloader downloader = new Okhttp3ProgressDownloader(downloadSelectedDownloadUrl, listProgressIndicator, StorageListReferencesOnDatabaseActivity.this, selectedUri);
                                downloader.run();
                                Toast.makeText(StorageListReferencesOnDatabaseActivity.this, "Download success", Toast.LENGTH_SHORT).show();
                                AndroidUtils.showSnackbarGreenShort(listFilesOrImages, "Download SUCCESS");

 */
                            } catch (Exception e) {
                                Toast.makeText(StorageListReferencesOnDatabaseActivity.this, "Exception on download: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                AndroidUtils.showSnackbarRedLong(listFilesOrImages,"Exception on download the file, aborted");
                            }
                        } else {
                            AndroidUtils.showSnackbarRedLong(listFilesOrImages,"No save file reference got, aborted");
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
                    AndroidUtils.showSnackbarRedLong(listFilesOrImages, "Failed to reload user");
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
                Intent intent = new Intent(StorageListReferencesOnDatabaseActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}