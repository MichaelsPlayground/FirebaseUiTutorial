package de.androidcrypto.firebaseuitutorial.database;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.ChatroomModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class DatabaseChatroomsActivity extends AppCompatActivity {
    private static final String TAG = DatabaseChatroomsActivity.class.getSimpleName();
    private RecyclerView recyclerView;
    private DatabaseReference chatroomsDatabase;
    private DatabaseChatroomRecyclerAdapter databaseChatroomRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_chatrooms);

        Toolbar toolbar = findViewById(R.id.tbChatroomsDatabase);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Chatrooms");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(DatabaseChatroomsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        recyclerView = findViewById(R.id.rvChatroomsDatabase);

        setupRecyclerView();

        recyclerView = findViewById(R.id.rvChatroomsDatabase);

        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //loadSignedInUserData(mFirebaseAuth.getCurrentUser().getUid());

    }

    void setupRecyclerView(){
        chatroomsDatabase = FirebaseUtils.getDatabaseUserChatroomsReference(FirebaseUtils.getCurrentUserId());
        Query orderedQuery = chatroomsDatabase
                .orderByChild("lastMessageTime");

        FirebaseRecyclerOptions<ChatroomModel> options = new FirebaseRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(orderedQuery, ChatroomModel.class)
                .build();

        databaseChatroomRecyclerAdapter = new DatabaseChatroomRecyclerAdapter(options, DatabaseChatroomsActivity.this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(databaseChatroomRecyclerAdapter);
        databaseChatroomRecyclerAdapter.startListening();
        databaseChatroomRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0); // scroll to top document
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if(databaseChatroomRecyclerAdapter !=null)
            databaseChatroomRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (databaseChatroomRecyclerAdapter != null) {
            databaseChatroomRecyclerAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (databaseChatroomRecyclerAdapter != null) {
            databaseChatroomRecyclerAdapter.notifyDataSetChanged();
        }
    }

}