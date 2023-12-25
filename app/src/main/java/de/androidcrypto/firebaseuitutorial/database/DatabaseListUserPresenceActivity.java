package de.androidcrypto.firebaseuitutorial.database;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
    private DatabaseReference usersPresenceReference; // all users
    private DatabaseReference userPresenceReference; // the current signed in user reference
    private DatabaseReference userConnectedReference;
    private ValueEventListener userListValueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_list_user_presence);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etDatabaseListUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseListUser);
        recyclerView = findViewById(R.id.rvDatabaseListUser);

        // To display the Recycler view linearlayout
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void listDatabaseUser() {
        usersPresenceReference = FirebaseUtils.getDatabaseUsersPresenceReference();
        userPresenceReference = FirebaseUtils.getDatabaseUserPresenceReference(authUserId);
        userConnectedReference = FirebaseUtils.getDatabaseInfoConnected();

        // make an entry for the connection
        userConnectedReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                if (connected) {
                    //onlineStatus.onDisconnect().removeValue();
                    userPresenceReference.onDisconnect().setValue(setUserModel(false));
                    userPresenceReference.setValue(setUserModel(true));
                } else {
                    userPresenceReference.setValue(setUserModel(false));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        FirebaseRecyclerOptions<UserModel> options
                = new FirebaseRecyclerOptions.Builder<UserModel>()
                .setQuery(usersPresenceReference, UserModel.class)
                .build();
        adapter = new DatabaseUserModelAdapter(options, true, authUserId, this);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private UserModel setUserModel (boolean isUserOnline) {
        String userOnlineString = "";
        long lastOnline = TimeUtils.getActualUtcZonedDateTime();
        if (isUserOnline) {
            userOnlineString = FirebaseUtils.USER_ONLINE;
        } else {
            userOnlineString = FirebaseUtils.USER_OFFLINE;
        }
        UserModel model = new UserModel(
                authDisplayName,
                authUserEmail,
                FirebaseUtils.getCurrentUserId(),
                authPhotoUrl,
                isUserOnline,
                userOnlineString,
                lastOnline
        );
        return model;
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

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume for user " + authUserId);
        if (userConnectedReference != null) userConnectedReference.addValueEventListener(userListValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause for user " + authUserId);
        if (userConnectedReference != null) userConnectedReference.removeEventListener(userListValueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userConnectedReference != null) userConnectedReference.removeEventListener(userListValueEventListener);
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
            authDisplayName = user.getDisplayName();
            if (TextUtils.isEmpty(authDisplayName)) authDisplayName = "unknown";
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