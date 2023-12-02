package de.androidcrypto.firebaseuitutorial.database;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.ItemClickListener;
import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.RecentMessageModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class DatabaseListUserRecentMessagesActivity extends AppCompatActivity implements ItemClickListener {
    // https://www.geeksforgeeks.org/how-to-populate-recyclerview-with-firebase-data-using-firebaseui-in-android-studio/

    private static final String TAG = DatabaseListUserRecentMessagesActivity.class.getSimpleName();

    private com.google.android.material.textfield.TextInputEditText signedInUser;

    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private DatabaseReference recentMessagesDatabase;
    private RecyclerView recyclerView;
    private DatabaseRecentMessageModelAdapter adapter; // Create Object of the Adapter class
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_list_user_recent_messages);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etDatabaseListUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseListUserRecentMessages);

        recyclerView = findViewById(R.id.rvDatabaseListUserRecentMessages);
        // To display the Recycler view linearlayout
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

    }

    private void listDatabaseUserRecentMessages() {
        recentMessagesDatabase = FirebaseUtils.getDatabaseUserRecentMessagesReference(authUserId);
        // This is a class provided by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        Query orderedQuery = recentMessagesDatabase
                .orderByChild("chatLastTime")
                .limitToLast(5);
        FirebaseRecyclerOptions<RecentMessageModel> options
                = new FirebaseRecyclerOptions.Builder<RecentMessageModel>()
                .setQuery(orderedQuery, RecentMessageModel.class)
                //.setQuery(recentMessagesDatabase, RecentMessageModel.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself
        adapter = new DatabaseRecentMessageModelAdapter(options, this);
        adapter.setClickListener(this);
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
        //adapter.startListening();
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override protected void onStop()
    {
        super.onStop();
        adapter.stopListening();
    }

    // called when clicking on recyclerview
    @Override
    public void onClick(View view, int position, String userId) {
        Log.i(TAG, "recyclerview clicked on position: " + position + " userId: " + userId);

        /*
        String uidSelected = uidList.get(position);
        String emailSelected = emailList.get(position);
        String displayNameSelected = displayNameList.get(position);

        Intent intent = new Intent(ListUserRecyclerviewActivity.this, ChatActivity.class);
        intent.putExtra("UID", uidSelected);
        intent.putExtra("EMAIL", emailSelected);
        intent.putExtra("DISPLAYNAME", displayNameSelected);
        startActivity(intent);
        finish();
        */
    }


    private void reload() {
        Objects.requireNonNull(FirebaseUtils.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(FirebaseUtils.getCurrentUser());
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
            String userData = String.format("Email: %s", authUserEmail);
            signedInUser.setText(userData);
            listDatabaseUserRecentMessages();
        } else {
            signedInUser.setText(null);
        }
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
                Intent intent = new Intent(DatabaseListUserRecentMessagesActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }


}