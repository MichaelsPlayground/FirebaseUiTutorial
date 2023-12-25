package de.androidcrypto.firebaseuitutorial.database;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserPresenceModel;

// from Tutorial https://programtown.com/how-to-build-online-user-presence-system-in-android-using-realtime-database/

public class DatabaseListUserPresenceLvActivity extends AppCompatActivity {
    private static final String TAG = DatabaseListUserPresenceLvActivity.class.getSimpleName();

    FirebaseUser user;
    FirebaseDatabase db;
    DatabaseReference usersListRef,onlineStatus, connectedRef;
    ValueEventListener userListValueEventListener;

    ListView userListView;
    ArrayList<String> userListItems;
    ArrayAdapter<String> adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_list_user_presence_lv);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.sub_toolbar);
        setSupportActionBar(myToolbar);

        userListView = findViewById(R.id.user_list);
        userListItems = new ArrayList<String>();
        db = FirebaseDatabase.getInstance();
        usersListRef = db.getReference("presence");
        user = FirebaseAuth.getInstance().getCurrentUser();
        addToUserList(user);
        populateUserList();

    }

    private void addToUserList(FirebaseUser user) {
        usersListRef.child(user.getUid()).setValue(new UserPresenceModel(user.getDisplayName(),"Online"));
        onlineStatus = db.getReference("presenceLv/"+user.getUid()+"/onlineStatus");
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    //onlineStatus.onDisconnect().removeValue();
                    onlineStatus.onDisconnect().setValue("offline");
                    onlineStatus.setValue("Online");
                } else {
                    onlineStatus.setValue("offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void populateUserList() {
        userListValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userListItems.clear();
                //first check datasnap shot exist
                //then add all users except current/self user
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        if (ds.exists() && !ds.getKey().equals(user.getUid())){
                            String name = ds.child("name").getValue(String.class);
                            String onlineStatus = ds.child("onlineStatus").getValue(String.class);
                            userListItems.add(name+" status : "+onlineStatus);
                            Log.d(TAG, "new entry: " + name+" status : "+onlineStatus);
                        }
                    }
                    Log.d(TAG, "userListItems count: " + userListItems.size());
                }

                adapter = new ArrayAdapter<String>(DatabaseListUserPresenceLvActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1, userListItems);
                userListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        usersListRef.addValueEventListener(userListValueEventListener);
    }

    private void setOnlineStatus(boolean status) {
        if (status) {
            onlineStatus.onDisconnect().setValue("offline");
            onlineStatus.setValue("Online");
        } else {
            onlineStatus.setValue("offline");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume for user " + user.getDisplayName());
        setOnlineStatus(true);
        usersListRef.addValueEventListener(userListValueEventListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause for user " + user.getDisplayName());
        setOnlineStatus(false);
        usersListRef.removeEventListener(userListValueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        usersListRef.removeEventListener(userListValueEventListener);
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
                Intent intent = new Intent(DatabaseListUserPresenceLvActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

}