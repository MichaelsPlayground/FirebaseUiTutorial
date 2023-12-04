package de.androidcrypto.firebaseuitutorial.firestore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.ChatroomModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirestoreChatroomsActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private TextView userName;
    private RecyclerView recyclerView;

    private static String authUserId = "";
    private static String authUserEmail = "";
    private static String authDisplayName = "";
    private static String authProfileImage = "";
    private static String roomId = "";

    static final String TAG = FirestoreChatroomsActivity.class.getSimpleName();

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

        //Toolbar myToolbar = (Toolbar) findViewById(R.id.chatToolbar);
        //setSupportActionBar(myToolbar);

        profileImage = findViewById(R.id.ciChatroomsFirestoreProfileImage);
        userName = findViewById(R.id.tvChatroomsFirestoreUserName);
        recyclerView = findViewById(R.id.rvChatroomsFirestore);

        //edtMessageLayout = findViewById(R.id.etChatroomsFirestoreMessageLayout);
        //edtMessage = findViewById(R.id.etChatroomsFirestoreMessage);

        // start with a disabled ui
        //enableUiOnSignIn(false);


        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //loadSignedInUserData(mFirebaseAuth.getCurrentUser().getUid());

        // fill toolbar
        userName.setText("empty");
        if (TextUtils.isEmpty("")) {
            profileImage.setImageResource(R.drawable.person_icon);
        } else {
            //and this
            Glide.with(getApplicationContext()).load("").into(profileImage);
        }

    }

    void setupRecyclerView(){

        Query query = FirebaseUtils.getFirestoreAllChatroomCollectionReference()
                .whereArrayContains("userIds",FirebaseUtils.getCurrentUserId())
                .orderBy("lastMessageTime",Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(query,ChatroomModel.class).build();
/*
        Query query = FirebaseUtils.getFirestoreAllChatroomCollectionReference()
                .whereArrayContains("userIds",FirebaseUtils.getCurrentUserId())
                .orderBy("lastMessageTime",Query.Direction.DESCENDING);
  */

        /*
        CollectionReference cr = FirebaseUtils.getFirestoreAllChatroomCollectionReference();
FirestoreRecyclerOptions<ChatroomModel> options = new FirestoreRecyclerOptions.Builder<ChatroomModel>()
                .setQuery(cr,ChatroomModel.class).build();
         */

        firestoreRecyclerAdapter = new FirestoreChatroomRecyclerAdapter(options,FirestoreChatroomsActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(FirestoreChatroomsActivity.this));
        recyclerView.setAdapter(firestoreRecyclerAdapter);
        firestoreRecyclerAdapter.startListening();
        System.out.println("*** setupRecyclerView done");
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