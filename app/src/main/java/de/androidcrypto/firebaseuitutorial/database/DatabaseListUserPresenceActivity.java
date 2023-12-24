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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class DatabaseListUserPresenceActivity extends AppCompatActivity {
    private static final String TAG = DatabaseListUserPresenceActivity.class.getSimpleName();

    private com.google.android.material.textfield.TextInputEditText signedInUser;

    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private RecyclerView recyclerView;
    private DatabaseUserModelAdapter adapter; // Create Object of the Adapter class
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_list_user_presence);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etDatabaseListUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseListUser);

        // Create a instance of the database and get its reference
        DatabaseReference usersDatabase = FirebaseUtils.getDatabaseUsersReference(); // unsorted user list
        //DatabaseReference usersDatabase = FirebaseUtils.getDatabaseUsersPresenceReference();

        recyclerView = findViewById(R.id.rvDatabaseListUser);
        // To display the Recycler view linearlayout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // This is a class provided by the FirebaseUI to make a
        // query in the database to fetch appropriate data
        FirebaseRecyclerOptions<UserModel> options
                = new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(usersDatabase, UserModel.class)
                .build();
        // Connecting object of required Adapter class to
        // the Adapter class itself

        adapter = new DatabaseUserModelAdapter(options, true, FirebaseUtils.getCurrentUserId(), this);
        // Connecting Adapter class with the Recycler view*/
        recyclerView.setAdapter(adapter);

        // see https://firebase.google.com/docs/database/android/offline-capabilities?hl=en&authuser=0#section-presence
        DatabaseReference presenceRef = FirebaseUtils.getDatabaseUserPresenceReference(FirebaseUtils.getCurrentUserId());
        long lastOnline = TimeUtils.getActualUtcZonedDateTime();
        boolean isUserOnline = false;
        String userOnlineString = "offline";
// public UserModel(String userName, String userMail, String userId, String userPhotoUrl, boolean userOnline, String userOnlineString, long userLastOnlineTime) {
        UserModel model = new UserModel(
                authDisplayName,
                authUserEmail,
                FirebaseUtils.getCurrentUserId(),
                authPhotoUrl,
                isUserOnline,
                userOnlineString,
                lastOnline
        );
        presenceRef.onDisconnect().setValue(model);

/*
        String userId = FirebaseUtils.getCurrentUserId();
        // Since I can connect from multiple devices, we store each connection instance separately
        // any time that connectionsRef's value is null (i.e. has no children) I am offline
        final DatabaseReference myConnectionsRef = FirebaseUtils.getDatabaseUserPresenceReference(userId);

        // Stores the timestamp of my last disconnect (the last time I was seen online)
        final DatabaseReference lastOnlineRef = FirebaseUtils.getDatabaseUserPresenceReference(userId);

        final DatabaseReference connectedRef = FirebaseUtils.getDatabaseInfoConnected();
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                Log.d(TAG, "presenceCheckDatabase onDataChange connected: " + connected);
                AndroidUtils.showToast(DatabaseListUserPresenceActivity.this, "presenceCheckDatabase onDataChange connected: " + connected );
                if (connected) {
                    DatabaseReference con = myConnectionsRef.push();
                    // When this device disconnects, remove it
                    con.onDisconnect().removeValue();

                    // When I disconnect, update the last time I was seen online
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);

                    // Add this device to my connections list
                    // this value could contain info about the device or a timestamp too
                    con.setValue(Boolean.TRUE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Listener was cancelled at .info/connected");
                AndroidUtils.showToast(DatabaseListUserPresenceActivity.this, "Listener was cancelled at .info/connected");
            }
        });
*/

    }

    private void listDatabaseUser() {
        adapter.startListening();
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

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
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
            listDatabaseUser();
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
                Intent intent = new Intent(DatabaseListUserPresenceActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
}