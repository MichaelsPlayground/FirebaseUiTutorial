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
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Objects;

import de.androidcrypto.firebaseuitutorial.MainActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.database.DatabaseChatRecyclerAdapter;
import de.androidcrypto.firebaseuitutorial.models.MessageModel;
import de.androidcrypto.firebaseuitutorial.models.NotificationMessageModel;
import de.androidcrypto.firebaseuitutorial.models.RecentMessageModel;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirestoreChatActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private com.google.android.material.textfield.TextInputEditText edtMessage;
    private com.google.android.material.textfield.TextInputLayout edtMessageLayout;
    private CircleImageView profileImage;
    private TextView userName;
    private RecyclerView messagesList;

    private static String authUserId = "";
    private static String authUserEmail = "";
    private static String authDisplayName = "";
    private static String authProfileImage = "";
    private static String receiveUserId = "", receiveUserEmail = "", receiveUserDisplayName = "", receiveProfileImage = "";
    private static String roomId = "";

    static final String TAG = FirestoreChatActivity.class.getSimpleName();

    private CollectionReference messagesDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirestoreRecyclerAdapter firestoreRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firestore_chat);

        Toolbar toolbar = findViewById(R.id.tbChatFirestore);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(FirestoreChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        //Toolbar myToolbar = (Toolbar) findViewById(R.id.chatToolbar);
        //setSupportActionBar(myToolbar);
        
        profileImage = findViewById(R.id.ciChatFirestoreProfileImage);
        userName = findViewById(R.id.tvChatFirestoreUserName);

        edtMessageLayout = findViewById(R.id.etChatFirestoreMessageLayout);
        edtMessage = findViewById(R.id.etChatFirestoreMessage);
        messagesList = findViewById(R.id.rvChatFirestore);

        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(new LinearLayoutManager(this));

        // start with a disabled ui
        enableUiOnSignIn(false);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first but in MainActivity
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        loadSignedInUserData(mFirebaseAuth.getCurrentUser().getUid());

        // read data received from ListUserOnDatabase
        Intent intent = getIntent();
        receiveUserId = intent.getStringExtra("UID");
        if (receiveUserId != null) {
            Log.i(TAG, "selectedUid: " + receiveUserId);
        } else {
            receiveUserId = "";
        }
        receiveUserEmail = intent.getStringExtra("EMAIL");
        if (receiveUserEmail != null) {
            Log.i(TAG, "selectedEmail: " + receiveUserEmail);
        }
        receiveUserDisplayName = intent.getStringExtra("DISPLAYNAME");
        if (receiveUserDisplayName != null) {
            Log.i(TAG, "selectedDisplayName: " + receiveUserDisplayName);
        } else {
            receiveUserDisplayName = receiveUserEmail;
        }
        receiveProfileImage = intent.getStringExtra("PROFILE_IMAGE");
        if (receiveProfileImage != null) {
            Log.i(TAG, "selectedProfileImage: " + receiveProfileImage);
        } else {
            receiveProfileImage = "";
        }

        String receiveUserString = "Email: " + receiveUserEmail;
        receiveUserString += "\nUID: " + receiveUserId;
        receiveUserString += "\nDisplay Name: " + receiveUserDisplayName;
        //receiveUser.setText(receiveUserString);

        // fill toolbar
        userName.setText(receiveUserDisplayName);
        if (TextUtils.isEmpty(receiveProfileImage)){
            profileImage.setImageResource(R.drawable.person_icon);
        } else {
            //and this
            Glide.with(getApplicationContext()).load(receiveProfileImage).into(profileImage);
        }


        Log.i(TAG, "receiveUser: " + receiveUserString);
        // get own data
        authUserEmail = intent.getStringExtra("AUTH_EMAIL");
        authDisplayName = intent.getStringExtra("AUTH_DISPLAYNAME");
        authProfileImage = mFirebaseAuth.getCurrentUser().getPhotoUrl().toString();

        // Initialize Firebase Auth
        // mFirebaseAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        //mDatabase = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabase = FirebaseDatabase.getInstance().getReference();
        // see loadSignedInUserData as we use a new instance there

        messagesDatabase = FirebaseUtils.getFirestoreChatsReference();
        // TODO messagesDatabase.keepSynced(true);

        String finalReceiveUserString = receiveUserString;
        String finalReceiveUserString1 = receiveUserString;
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
                // retrieve the time string in GMT
                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //String millisInString  = dateFormat.format(new Date());

                //MessageModel messageModel = new MessageModel(messageString, actualTime, timestamp, authUserId, receiveUserId);
                MessageModel messageModel = new MessageModel(messageString, actualTime, actualTimeString, authUserId, receiveUserId);
                CollectionReference collectionReference = FirebaseUtils.getFirestoreChatroomReference(roomId);
                collectionReference.add(messageModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i(TAG, "DocumentSnapshot successfully written for roomId: " + roomId);
                        Toast.makeText(getApplicationContext(),
                                "message written to chatroom " + roomId,
                                Toast.LENGTH_SHORT).show();
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

                // store one notificationMessage per user
                NotificationMessageModel notificationMessageModel = new NotificationMessageModel(roomId, authUserId, authDisplayName, authUserEmail, authProfileImage, actualTime);
                FirebaseUtils.getFirestoreUserNotificationMessagesDocumentSetTask(receiveUserId, notificationMessageModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, "notification message reference written for receiveUserId: " + receiveUserId);
                        Toast.makeText(getApplicationContext(),
                                "notification message written to receiveUserId: " + receiveUserId,
                                Toast.LENGTH_SHORT).show();
                    }
                });
/*
                FirebaseUtils.getFirestoreUserNotificationMessagesAddTask(receiveUserId, authUserId, notificationMessageModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    //FirebaseUtils.getFirestoreUserNotificationMessagesTask(receiveUserId, notificationMessageModel).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i(TAG, "notification message reference written for receiveUserId: " + receiveUserId);
                        Toast.makeText(getApplicationContext(),
                                "notification message written to receiveUserId: " + receiveUserId,
                                Toast.LENGTH_SHORT).show();
                    }
                });
*/
            }
        });
    }

    private void userNotification(String receiveUserId, String senderUserId, long actualTime) {
        String currentUserId = FirebaseUtils.getCurrentUserId();
        // update only if a user is signed in
        if (!TextUtils.isEmpty(currentUserId)) {

            // Firebase

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("userOnlineString", senderUserId);
            hashMap.put("userLastOnlineTime", actualTime);
            /*
            actualUserDatabaseReference = FirebaseUtils.getDatabaseUserReference(currentUserId);
            actualUserDatabaseReference.updateChildren(hashMap);
*/
            // Firestore
            DocumentReference actualUsersNotificationFirebaseReference = FirebaseUtils.getFirestoreUserNotificationReference(receiveUserId);
            actualUsersNotificationFirebaseReference.update(hashMap);
        }
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
        //Query orderedQuery = query.orderBy("messageTime", Query.Direction.ASCENDING);

        CollectionReference collectionReference = FirebaseUtils.getFirestoreChatroomReference(roomId);
        Query orderedQuery = collectionReference.orderBy("messageTime", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<MessageModel> options = new FirestoreRecyclerOptions.Builder<MessageModel>()
                .setQuery(orderedQuery, MessageModel.class)
                .build();

        firestoreRecyclerAdapter = new FirestoreChatRecyclerAdapter(options, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(false); // true oldest element at bottom
        messagesList.setLayoutManager(manager);
        messagesList.setAdapter(firestoreRecyclerAdapter);
        firestoreRecyclerAdapter.startListening();
        firestoreRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                //messagesList.smoothScrollToPosition(0); // scroll to top document
                messagesList.smoothScrollToPosition(itemCount - 1); // scroll to last document
                messagesList.smoothScrollToPosition(firestoreRecyclerAdapter.getItemCount()); // scroll to last document
            }
        });
    }
/*
    private void attachRecyclerViewAdapter() {
        final RecyclerView.Adapter adapter = firebaseRecyclerAdapter;
        if (adapter != null) {
            Log.i(TAG, "attachRecyclerViewAdapter");
            // Scroll to bottom on new messages
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    //mBinding.messagesList.smoothScrollToPosition(adapter.getItemCount());
                    messagesList.smoothScrollToPosition(adapter.getItemCount());
                }
            });

            //mBinding.messagesList.setAdapter(adapter);
            messagesList.setAdapter(adapter);
        } else {
            Log.i(TAG, "attachRecyclerViewAdapter NOT set, firebaseRecyclerAdapter is null");
        }
    }
*/
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