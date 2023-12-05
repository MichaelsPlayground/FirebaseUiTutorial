package de.androidcrypto.firebaseuitutorial.firestore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.ChatroomModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class FirestoreChatroomsActivity extends AppCompatActivity {
    private static final String TAG = FirestoreChatroomsActivity.class.getSimpleName();
    private RecyclerView recyclerView;

    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_chatrooms);

        Toolbar toolbar = findViewById(R.id.tbChatroomsFirestore);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Chatrooms");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(FirestoreChatroomsActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        recyclerView = findViewById(R.id.rvChatroomsFirestore);

        setupRecyclerView();

        recyclerView = findViewById(R.id.rvChatroomsFirestore);

        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //loadSignedInUserData(mFirebaseAuth.getCurrentUser().getUid());

    }

    void setupRecyclerView(){

        Query query = FirebaseUtils.getFirestoreAllChatroomCollectionReference()
                .whereArrayContains("userIds",FirebaseUtils.getCurrentUserId())
                .orderBy("lastMessageTime",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query,ChatroomModel.class).build();

        firestoreRecyclerAdapter = new FirestoreChatroomRecyclerAdapter(options,FirestoreChatroomsActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(FirestoreChatroomsActivity.this));
        recyclerView.setAdapter(firestoreRecyclerAdapter);
        firestoreRecyclerAdapter.startListening();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(firestoreRecyclerAdapter!=null)
            firestoreRecyclerAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.notifyDataSetChanged();
        }
    }
}