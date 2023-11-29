package de.androidcrypto.firebaseuitutorial.database;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class DatabaseListUserLvActivity extends AppCompatActivity {
    // https://www.geeksforgeeks.org/how-to-populate-recyclerview-with-firebase-data-using-firebaseui-in-android-studio/

    private static final String TAG = DatabaseListUserLvActivity.class.getSimpleName();

    private com.google.android.material.textfield.TextInputEditText signedInUser;

    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;

    private ListView userListView;
    //private UserModelAdapter adapter; // Create Object of the Adapter class
    private ProgressBar progressBar;
    private DatabaseReference usersDatabaseReference;
    private FirebaseListAdapter<UserModel> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_list_user_lv);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etDatabaseListUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseListUser);

        // Create a instance of the database and get its reference
        usersDatabaseReference = FirebaseUtils.getDatabaseUsersReference();
        userListView = findViewById(R.id.lvDatabaseListUser);

        usersDatabaseReference.keepSynced(true);
        List<UserModel> userModelList = new ArrayList<>();
        List<String> emailList = new ArrayList<>();
        List<String> displayNameList = new ArrayList<>();
        List<String> profileImageList = new ArrayList<>();

        // this is the new way
        FirebaseListOptions<UserModel> listAdapterOptions;
        listAdapterOptions = new FirebaseListOptions.Builder<UserModel>()
                .setLayout(android.R.layout.simple_list_item_1)
                .setQuery(usersDatabaseReference, UserModel.class)
                .build();

        listAdapter = new FirebaseListAdapter<UserModel>(listAdapterOptions) {
            @Override
            protected void populateView(@NonNull View v, @NonNull UserModel model, int position) {
                String email = model.getUserMail();
                String displayName = model.getUserName();
                String profileImage = model.getUserPhotoUrl();
                String onlineStatusString = model.getUserOnlineString();
                System.out.println("email: " + email + " status: " + onlineStatusString);
                emailList.add(email);
                displayNameList.add(displayName);
                profileImageList.add(profileImage);
                ((TextView) v.findViewById(android.R.id.text1)).setText(displayName + " (" + email + ") is " + onlineStatusString);
                listAdapter.notifyDataSetChanged();
                // if the user is the authUser save email and displayName
                //if ()
                String uid = listAdapter.getRef(position).getKey();
                Log.i(TAG, "uid/key: " + uid);
                if (uid.equals(authUserId)) {
                    authDisplayName = displayName;
                    authUserEmail = email;
                }
            }
        };
        userListView.setAdapter(listAdapter);

        boolean listAllUsers = true;
        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String listUserId = listAdapter.getRef(position).getKey();
                if (!listAllUsers) {
                    if (listUserId.equals(authUserId)) {
                        // when not all users are listed avoid clicking yourself
                        Toast.makeText(getApplicationContext(),
                                "you cannot chat with yourself, choose another user",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Log.i(TAG, "userListView clicked on pos: " + position);
                //Intent intent = new Intent(DatabaseListUserLvActivity.this, ChatDatabaseActivity.class);
                Intent intent = new Intent(DatabaseListUserLvActivity.this, DatabaseChatActivity.class);
                intent.putExtra("UID", listAdapter.getRef(position).getKey());
                intent.putExtra("EMAIL", emailList.get(position));
                intent.putExtra("DISPLAYNAME", displayNameList.get(position));
                intent.putExtra("AUTH_EMAIL", authUserEmail);
                intent.putExtra("AUTH_DISPLAYNAME", authDisplayName);
                intent.putExtra("PROFILE_IMAGE", profileImageList.get(position));
                startActivity(intent);
                finish();
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
        listAdapter.startListening();
    }

    // Function to tell the app to stop getting
    // data from database on stopping of the activity
    @Override protected void onStop()
    {
        super.onStop();
        listAdapter.stopListening();
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
            //listDatabaseUser();
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
                Intent intent = new Intent(DatabaseListUserLvActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}