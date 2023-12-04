package de.androidcrypto.firebaseuitutorial.firestore;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.ChatroomModel;
import de.androidcrypto.firebaseuitutorial.models.MessageModel;
import de.androidcrypto.firebaseuitutorial.models.RecentMessageModel;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirestoreChatActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    static final String TAG = FirestoreChatActivity.class.getSimpleName();
    private com.google.android.material.textfield.TextInputEditText edtMessage;
    private com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    private CircleImageView profileImage;
    private TextView userName;
    private static String authUserId = "";
    private static String authUserEmail = "";
    private static String authDisplayName = "";
    private static String authProfileImage = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "", receiveProfileImage = "";
    private UserModel otherUserModel;
    private static String roomId = "";
    private FirebaseAuth mFirebaseAuth;
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;
    private ChatroomModel chatroomModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_chat);
        //LinearLayout llView = findViewById(R.layout.activity_firestore_chat);
        Toolbar toolbar = findViewById(R.id.tbChatFirestore);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat with ");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FirestoreChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
        
        profileImage = findViewById(R.id.ciChatFirestoreProfileImage);
        userName = findViewById(R.id.tvChatFirestoreUserName);

        edtMessageLayout = findViewById(R.id.etChatFirestoreMessageLayout);
        edtMessage = findViewById(R.id.etChatFirestoreMessage);
        recyclerView = findViewById(R.id.rvChatFirestore);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // start with a disabled ui
        enableUiOnSignIn(false);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistence first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        loadSignedInUserData(mFirebaseAuth.getCurrentUser().getUid());

        otherUserModel = AndroidUtils.getUserModelFromIntent(getIntent());
        if (otherUserModel != null) {
            receiveUserId = otherUserModel.getUserId();
            receiveUserDisplayName = otherUserModel.getUserName();
            receiveUserEmail = otherUserModel.getUserMail();
            receiveProfileImage = otherUserModel.getUserPhotoUrl();
            Log.d(TAG, "incoming intent otherPhotoUrl: " + receiveProfileImage);
            if (!TextUtils.isEmpty(receiveProfileImage)) {
                GlideApp.with(this)
                        .load(receiveProfileImage)
                        .into(profileImage);
            } else {
                Log.d(TAG, "cannot set receiver profile image");
                profileImage.setImageResource(R.drawable.person_icon);
            }
        } else {
            Toast.makeText(this, "could not get the  data from other user, aborted", Toast.LENGTH_SHORT).show();
        }

        roomId = FirebaseUtils.getChatroomId(FirebaseUtils.getCurrentUserId(), receiveUserId);
        System.out.println("*** getOrCreateChatroomModel ***");
        getOrCreateChatroomModel();

        String receiveUserString = "Email: " + receiveUserEmail;
        receiveUserString += "\nUID: " + receiveUserId;
        receiveUserString += "\nDisplay Name: " + receiveUserDisplayName;
        //receiveUser.setText(receiveUserString);

        // fill toolbar
        userName.setText(receiveUserDisplayName);

        Log.i(TAG, "receiveUser: " + receiveUserString);
        // get own data
        //authUserEmail = intent.getStringExtra("AUTH_EMAIL");
        authDisplayName = mFirebaseAuth.getCurrentUser().getDisplayName().toString();
        authUserEmail = mFirebaseAuth.getCurrentUser().getEmail().toString();
        authProfileImage = mFirebaseAuth.getCurrentUser().getPhotoUrl().toString();

        // TODO messagesDatabase.keepSynced(true);

        edtMessageLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showProgressBar();
                Log.i(TAG, "clickOnIconEnd");
                String messageString = edtMessage.getText().toString();
                Log.i(TAG, "message: " + messageString);
                // now we are going to send data to the database
                long actualTime = TimeUtils.getActualUtcZonedDateTime();
                String actualTimeString = TimeUtils.getZoneDatedStringMediumLocale(actualTime);
                MessageModel messageModel = new MessageModel(messageString, actualTime, actualTimeString, authUserId, receiveUserId);
                CollectionReference collectionReference = FirebaseUtils.getFirestoreChatroomCollectionReference(roomId);
                collectionReference.add(messageModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i(TAG, "DocumentSnapshot successfully written for roomId: " + roomId);
                        Toast.makeText(view.getContext(),
                                "message written to chatroom " + roomId,
                                Toast.LENGTH_SHORT).show();

                        // update the chatroom
                        //getOrCreateChatroomModel();
                        chatroomModel.setLastMessageTime(TimeUtils.getActualUtcZonedDateTime());
                        chatroomModel.setLastMessageSenderId(FirebaseUtils.getCurrentUserId());
                        chatroomModel.setLastMessage(messageString);
                        chatroomModel.setSenderName(authDisplayName);
                        chatroomModel.setSenderEmail(authUserEmail);
                        chatroomModel.setSenderPhotoUrl(authProfileImage);
                        chatroomModel.setReceiverName(otherUserModel.getUserName());
                        chatroomModel.setReceiverEmail(otherUserModel.getUserMail());
                        chatroomModel.setReceiverPhotoUrl(otherUserModel.getUserPhotoUrl());
                        FirebaseUtils.getFirestoreChatroomReference(roomId).set(chatroomModel);
                    }
                });
                edtMessage.setText("");

                // store the message in recentMessages database of the receiver
                // RecentMessageModel(String chatroomId, String chatMessage, String userId, String userName, String userEmail, String userProfileImage, long chatLastTime)
                RecentMessageModel recentMessageModel = new RecentMessageModel(roomId, messageString, authUserId, authDisplayName, authUserEmail, authProfileImage, actualTime);
                CollectionReference recentMessageCollectionReference = FirebaseUtils.getFirestoreUserRecentMessagesReference(receiveUserId);
                recentMessageCollectionReference.add(recentMessageModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i(TAG, "recent message reference written for receiveUserId: " + receiveUserId);
                        Toast.makeText(getApplicationContext(),
                                "recent message written to receiveUserId: " + receiveUserId,
                                Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }

    void getOrCreateChatroomModel() {
        System.out.println("*** getOrCreateChatroomModel roomId: " + roomId + " ***");
        FirebaseUtils.getFirestoreChatroomReference(roomId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                chatroomModel = task.getResult().toObject(ChatroomModel.class);
                if (chatroomModel == null) {
                    //first time chat
                    System.out.println("*** firstTimeChat in chatroom " + roomId + " ***");
                    chatroomModel = new ChatroomModel(
                            roomId,
                            Arrays.asList(FirebaseUtils.getCurrentUserId(), receiveUserId),
                            TimeUtils.getActualUtcZonedDateTime(),
                            ""
                    );
                    FirebaseUtils.getFirestoreChatroomReference(roomId).set(chatroomModel);
                } else {
                    // do nothing, we have read the chatroomModel
                }
            } else {
                AndroidUtils.showSnackbarRedLong(recyclerView, "could not create a chatroomID, aborted");
            }
        });
    }

    /**
     * service methods
     */

    // compare two strings and build a new string: if a < b: ab if a > b: ba, if a = b: ab
    private String getRoomId(String a, String b) {
        int compare = a.compareTo(b);
        if (compare > 0) return b + "_" + a;
        else return a + "_" + b;
    }

    /**
     * basic
     */

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if(currentUser != null){
            if (!receiveUserId.equals("")) {
                Log.i(TAG, "onStart prepare database for chat");
                reload();
                enableUiOnSignIn(true);
                setupChatRecyclerView(currentUser.getUid(), receiveUserId);
                /*
                setDatabaseForRoom(currentUser.getUid(), receiveUserId);
                firebaseRecyclerAdapter.startListening();
                attachRecyclerViewAdapter();

                 */
            } else {
                //header.setText("you need to select a receiveUser first");
                Log.i(TAG, "you need to select a receiveUser first");
            }
        } else {
            //signedInUser.setText("no user is signed in");
            authUserId = "";
            enableUiOnSignIn(false);
            firestoreRecyclerAdapter.stopListening();
        }
        // startListening begins when a user is logged in
        //firebaseRecyclerAdapter.startListening();
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreRecyclerAdapter != null) {
            firestoreRecyclerAdapter.stopListening();
        }
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        if (isSignedIn()) {
            //getAuthUserCredentials();
        } else {
            Toast.makeText(this, "you need to sign in before chatting", Toast.LENGTH_SHORT).show();
            //auth.signInAnonymously().addOnCompleteListener(new SignInResultNotifier(this));
        }
    }


    void setupChatRecyclerView(String ownUid, String receiverUid) {

        roomId = FirebaseUtils.getChatroomId(ownUid, receiverUid);
        com.google.firebase.firestore.Query query = FirebaseUtils.getFirestoreChatroomQuery(roomId);

        CollectionReference collectionReference = FirebaseUtils.getFirestoreChatroomCollectionReference(roomId);
        Query orderedQuery = collectionReference.orderBy("messageTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<MessageModel> options = new FirestoreRecyclerOptions.Builder<MessageModel>()
                .setQuery(orderedQuery, MessageModel.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreChatRecyclerAdapter(options, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(false); // true oldest element at bottom
        manager.setStackFromEnd(true);


        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(firestoreRecyclerAdapter);
        firestoreRecyclerAdapter.startListening();
        firestoreRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                //recyclerView.smoothScrollToPosition(0); // scroll to top document
                recyclerView.smoothScrollToPosition(itemCount - 1); // scroll to last document
                recyclerView.smoothScrollToPosition(firestoreRecyclerAdapter.getItemCount()); // scroll to last document
            }
        });
    }

    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void loadSignedInUserData(String mAuthUserId) {
        if (!mAuthUserId.equals("")) {
            DatabaseReference userDatabaseReference = FirebaseUtils.getDatabaseUserReference(mAuthUserId);
            //DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            //mDatabase.child("users").child(mAuthUserId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            userDatabaseReference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    //hideProgressBar();
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting data", task.getException());
                    } else {
                        // check for a null value means no user data were saved before
                        UserModel userModel = task.getResult().getValue(UserModel.class);
                        Log.i(TAG, String.valueOf(userModel));
                        if (userModel == null) {
                            Log.i(TAG, "userModel is null, show message");
                        } else {
                            Log.i(TAG, "userModel email: " + userModel.getUserMail());
                            authUserId = mAuthUserId;
                            authUserEmail = userModel.getUserMail();
                            authDisplayName = userModel.getUserName();
                        }
                    }
                }
            });
        } else {
            Toast.makeText(getApplicationContext(),
                    "sign in a user before loading",
                    Toast.LENGTH_SHORT).show();
            //hideProgressBar();
        }
    }

    private void reload() {
        Objects.requireNonNull(mFirebaseAuth.getCurrentUser()).reload().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    updateUI(mFirebaseAuth.getCurrentUser());
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
        //hideProgressBar();
        if (user != null) {
            authUserId = user.getUid();
            authUserEmail = user.getEmail();
            if (user.getDisplayName() != null) {
                authDisplayName = Objects.requireNonNull(user.getDisplayName()).toString();
            } else {
                authDisplayName = "no display name available";
            }
            String userData = String.format("Email: %s", authUserEmail);
            userData += "\nUID: " + authUserId;
            userData += "\nDisplay Name: " + authDisplayName;
            //signedInUser.setText(userData);
            Log.i(TAG, "authUser: " + userData);
        } else {
            //signedInUser.setText(null);
            authUserId = "";
        }
    }

    private void enableUiOnSignIn(boolean userIsSignedIn) {
        if (!userIsSignedIn) {
            //header.setText("you need to be signed in before starting a chat");
            edtMessageLayout.setEnabled(userIsSignedIn);
        } else {
            edtMessageLayout.setEnabled(userIsSignedIn);
        }
    }

}