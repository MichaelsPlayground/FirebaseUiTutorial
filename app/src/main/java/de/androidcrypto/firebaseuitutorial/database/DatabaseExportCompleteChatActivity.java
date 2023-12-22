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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.MessageModel;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class DatabaseExportCompleteChatActivity extends AppCompatActivity {
    // https://www.geeksforgeeks.org/how-to-populate-recyclerview-with-firebase-data-using-firebaseui-in-android-studio/

    private static final String TAG = DatabaseExportCompleteChatActivity.class.getSimpleName();
    private com.google.android.material.textfield.TextInputEditText signedInUser;
    private static String authUserId = "", authUserEmail, authDisplayName, authPhotoUrl;
    private ListView userListView;
    private ProgressBar progressBar;
    private DatabaseReference usersDatabaseReference;
    private FirebaseListAdapter<UserModel> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_export_complete_chat);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        signedInUser = findViewById(R.id.etDatabaseListUserSignedInUser);
        progressBar = findViewById(R.id.pbDatabaseExportCompleteChat);

        // Create a instance of the database and get its reference
        usersDatabaseReference = FirebaseUtils.getDatabaseUsersReference();
        userListView = findViewById(R.id.lvDatabaseExportCompleteChat);

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
                // now we are collecting the complete chat
                collectCompleteChat(listUserId);
            }
        });
    }

    private void collectCompleteChat(String userId) {
        // this is getting all documents in the messages chatroom with the user
        String chatroomId = FirebaseUtils.getChatroomId(authUserId, userId);
        List<MessageModel> messageModelsList = new ArrayList<>();
        DatabaseReference messagesDatabase = FirebaseUtils.getDatabaseChatsReference(chatroomId);
        Query query = messagesDatabase
                .child(chatroomId)
                .orderByChild("messageTime");

        /*
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                String key = snapshot.getKey();
                MessageModel messageModel = snapshot.getValue(MessageModel.class);
                System.out.println("* key: " + key + ":" + messageModel.getMessage());
                messageModelsList.add(messageModel);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // do nothing
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                // do nothing
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // do nothing
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // do nothing
            }
        });
         */
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    MessageModel model = ds.getValue(MessageModel.class);
                    messageModelsList.add(model);
                }
                Log.d("TAG", "number of chats: " + String.valueOf(snapshot.getChildrenCount()));
                prepareOutput(messageModelsList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // do nothing
            }
        };
        query.addListenerForSingleValueEvent(valueEventListener);
    }

    private String prepareOutput(List<MessageModel> messageModelsList) {
        int numberOfMessages = messageModelsList.size();
        System.out.println("*** numberOfMessages: " + numberOfMessages);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numberOfMessages; i++) {
            MessageModel model = messageModelsList.get(i);
            sb.append(String.format("%03d: ", (i + 1)));
            sb.append(TimeUtils.getZoneDatedStringShortLocale(model.getMessageTime())).append(" | ");
            sb.append(model.getMessage());
            sb.append("\n");
        }
        String completeChat = sb.toString();
        System.out.println(completeChat);
        return completeChat;
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
                Intent intent = new Intent(DatabaseExportCompleteChatActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}