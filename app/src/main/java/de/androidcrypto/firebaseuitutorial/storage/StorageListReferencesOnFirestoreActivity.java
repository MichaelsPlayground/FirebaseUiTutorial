package de.androidcrypto.firebaseuitutorial.storage;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;

import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.FileInformationModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class StorageListReferencesOnFirestoreActivity extends AppCompatActivity {

    /**
     * This class is NOT using FirestoreUi for the download purposes
     */

    private static final String TAG = StorageListReferencesOnFirestoreActivity.class.getSimpleName();
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private RadioButton rbListFiles, rbListImages, rbListResizedImages;
    private Button listFilesOrImages;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private String downloadSelector;

    private StorageListFirestoreFileReferencesModelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_list_references_on_firestore);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etStorageUserSignedInUser);
        rbListFiles = findViewById(R.id.rbStorageListReferencesFirestoreFiles);
        rbListImages = findViewById(R.id.rbStorageListReferencesFirestoreImages);
        rbListResizedImages = findViewById(R.id.rbStorageListReferencesFirestoreResizedImages);
        listFilesOrImages = findViewById(R.id.btnStorageListReferencesFirestoreFilesOrImages);
        progressBar = findViewById(R.id.pbStorageListReferencesFirestoreFiles);
        recyclerView = findViewById(R.id.rvStorageListReferencesFirestore);

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
         * list section
         */

        listFilesOrImages.setOnClickListener((v -> {
            listStorageFileReferencesOnFirestore();
        }));
    }

    private void listStorageFileReferencesOnFirestore() {
        CollectionReference listStorageFileReferencesFirestore;
        if (downloadSelector.equals(FirebaseUtils.STORAGE_FILES_FOLDER_NAME)) {
            listStorageFileReferencesFirestore = FirebaseUtils.getFirestoreCurrentUserCredentialsFilesReference();
        } else if(downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_FOLDER_NAME)) {
            listStorageFileReferencesFirestore = FirebaseUtils.getFirestoreCurrentUserCredentialsImagesReference();
        } else if(downloadSelector.equals(FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME)) {
            listStorageFileReferencesFirestore = FirebaseUtils.getFirestoreCurrentUserCredentialsImagesResizedReference();
        } else {
            // some data are wrong
            AndroidUtils.showToast(StorageListReferencesOnFirestoreActivity.this, "something got wrong, aborted");
            AndroidUtils.showSnackbarRedLong(listFilesOrImages, "something got wrong, aborted");
            return;
        }
        // This is a class provided by the FirebaseUI to make a
        // query in the database to fetch appropriate data

        com.google.firebase.firestore.Query orderedQuery = listStorageFileReferencesFirestore
                .orderBy("fileName");

        FirestoreRecyclerOptions<FileInformationModel> options
                = new FirestoreRecyclerOptions.Builder<FileInformationModel>()
                .setQuery(orderedQuery, FileInformationModel.class)
                .build();

        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new StorageListFirestoreFileReferencesModelAdapter(options, this);
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
                Intent intent = new Intent(StorageListReferencesOnFirestoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}