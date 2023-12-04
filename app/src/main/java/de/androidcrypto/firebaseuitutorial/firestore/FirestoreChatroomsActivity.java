package de.androidcrypto.firebaseuitutorial.firestore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.ChatroomModel;
import de.androidcrypto.firebaseuitutorial.models.MessageModel;
import de.androidcrypto.firebaseuitutorial.models.NotificationMessageModel;
import de.androidcrypto.firebaseuitutorial.models.RecentMessageModel;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;
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