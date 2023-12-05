package de.androidcrypto.firebaseuitutorial.database;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.util.Arrays;
import java.util.Map;
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

public class DatabaseChatActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {
    static final String TAG = DatabaseChatActivity.class.getSimpleName();
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
    private UserModel otherUserModel;
    private static String chatroomId = "";
    private DatabaseReference messagesDatabase;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseRecyclerAdapter firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_chat);

        Toolbar toolbar = findViewById(R.id.tbChatDatabase);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // and this
                startActivity(new Intent(DatabaseChatActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        //Toolbar myToolbar = (Toolbar) findViewById(R.id.chatToolbar);
        //setSupportActionBar(myToolbar);

        //header = findViewById(R.id.tvChatDatabaseHeader);
        profileImage = findViewById(R.id.ciChatDatabaseProfileImage);
        userName = findViewById(R.id.tvChatDatabaseUserName);

        edtMessageLayout = findViewById(R.id.etChatDatabaseMessageLayout);
        edtMessage = findViewById(R.id.etChatDatabaseMessage);
        messagesList = findViewById(R.id.rvChatDatabase);

        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(new LinearLayoutManager(this));

        // start with a disabled ui
        enableUiOnSignIn(false);

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        // set the persistance first but in MainActivity
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
        authDisplayName = mFirebaseAuth.getCurrentUser().getDisplayName().toString();
        authUserEmail = mFirebaseAuth.getCurrentUser().getEmail().toString();
        try {
            authProfileImage = mFirebaseAuth.getCurrentUser().getPhotoUrl().toString();
        } catch (NullPointerException e) {
            // there is no photoUrl at this time
        }

        // Initialize Firebase Auth
        // mFirebaseAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Database
        // https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/
        // if the database location is not us we need to use the reference:
        //mDatabase = FirebaseDatabase.getInstance("https://fir-playground-1856e-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        // the following can be used if the database server location is us
        //mDatabase = FirebaseDatabase.getInstance().getReference();
        // see loadSignedInUserData as we use a new instance there

        // Create a instance of the database and get its reference
        messagesDatabase = FirebaseUtils.getDatabaseChatsReference();
        messagesDatabase.keepSynced(true);

        edtMessageLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showProgressBar();
                Log.i(TAG, "clickOnIconEnd");
                String messageString = edtMessage.getText().toString();
                if (TextUtils.isEmpty(messageString)) {
                    AndroidUtils.showSnackbarRedLong(view, "you need to enter minimum 1 char");
                    return;
                }
                Log.i(TAG, "message: " + messageString);
                // now we are going to send data to the database
                long actualTime = TimeUtils.getActualUtcZonedDateTime();
                String actualTimeString = TimeUtils.getZoneDatedStringMediumLocale(actualTime);
                // retrieve the time string in GMT
                //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //String millisInString  = dateFormat.format(new Date());

                //MessageModel messageModel = new MessageModel(messageString, actualTime, timestamp, authUserId, receiveUserId);
                MessageModel messageModel = new MessageModel(messageString, actualTime, actualTimeString, authUserId, receiveUserId);
                messagesDatabase.child(chatroomId).push().setValue(messageModel);
                // without push there is no new chatId key
                // mDatabaseReference.child("messages").child(chatroomId).setValue(messageModel);
                Toast.makeText(getApplicationContext(),
                        "message written to database: " + messageString,
                        Toast.LENGTH_SHORT).show();
                edtMessage.setText("");

                // store an entry in chatrooms
                // String chatroomId, List<String> userIds, long lastMessageTime, String lastMessageSenderId
                ChatroomModel chatroomModel = new ChatroomModel(chatroomId, Arrays.asList(authUserId, receiveUserId), actualTime, authUserId);
                chatroomModel.setLastMessage(messageString);
                chatroomModel.setSenderName(authDisplayName);
                chatroomModel.setSenderEmail(authUserEmail);
                chatroomModel.setSenderPhotoUrl(authProfileImage);
                chatroomModel.setReceiverName(receiveUserDisplayName);
                chatroomModel.setReceiverEmail(receiveUserEmail);
                chatroomModel.setReceiverPhotoUrl(receiveProfileImage);
                Map chatroomModelMap = FirebaseUtils.convertModelToMap(chatroomModel);
                FirebaseUtils.getDatabaseUserChatroomsReference(receiveUserId, authUserId).updateChildren(chatroomModelMap);
                FirebaseUtils.getDatabaseUserChatroomsReference(authUserId, receiveUserId).updateChildren(chatroomModelMap);

                // store the message in recentMessages database of the receiver
                // RecentMessageModel(String chatroomId, String chatMessage, String userId, String userName, String userEmail, String userProfileImage, long chatLastTime)
                RecentMessageModel recentMessageModel = new RecentMessageModel(chatroomId, messageString, authUserId, authDisplayName, authUserEmail, authProfileImage, actualTime);
                FirebaseUtils.getDatabaseUserRecentMessagesReference(receiveUserId)
                        .push().setValue(recentMessageModel);
                Log.d(TAG, "recent message reference written");
            }
        });

    }

    /**
     * service methods
     */

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
            } else {
                Log.i(TAG, "you need to select a receiveUser first");
            }
        } else {
            //signedInUser.setText("no user is signed in");
            authUserId = "";
            enableUiOnSignIn(false);
            firebaseRecyclerAdapter.stopListening();
        }
        // startListening begins when a user is logged in
        FirebaseAuth.getInstance().addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseRecyclerAdapter != null) {
            firebaseRecyclerAdapter.stopListening();
        }
        FirebaseAuth.getInstance().removeAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth auth) {
        if (isSignedIn()) {
            //getAuthUserCredentials();
        } else {
            Toast.makeText(this, "you need to sign in before chatting", Toast.LENGTH_SHORT).show();
        }
    }

    void setupChatRecyclerView(String ownUid, String receiverUid) {
        chatroomId = FirebaseUtils.getChatroomId(ownUid, receiverUid);
        messagesDatabase = FirebaseUtils.getDatabaseChatsReference(chatroomId);
        Query query = messagesDatabase
                .child(chatroomId)
                .orderByChild("messageTime");

        FirebaseRecyclerOptions<MessageModel> options = new FirebaseRecyclerOptions.Builder<MessageModel>()
                .setQuery(query, MessageModel.class)
                .build();

        firebaseRecyclerAdapter = new DatabaseChatRecyclerAdapter(options, getApplicationContext());
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(false); // true oldest element at bottom
        messagesList.setLayoutManager(manager);
        messagesList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                //messagesList.smoothScrollToPosition(0); // scroll to top document
                messagesList.smoothScrollToPosition(itemCount - 1); // scroll to last document
                messagesList.smoothScrollToPosition(firebaseRecyclerAdapter.getItemCount()); // scroll to last document
            }
        });
    }
    private boolean isSignedIn() {
        return FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void loadSignedInUserData(String mAuthUserId) {
        if (!mAuthUserId.equals("")) {
            DatabaseReference userDatabaseReference = FirebaseUtils.getDatabaseUserReference(mAuthUserId);
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

    // generates the room id and prepares for the query
    private void setDatabaseForRoom(String ownUid, String receiverUid) {
        // get the chatroomId by comparing 2 UID strings
        chatroomId = FirebaseUtils.getChatroomId(ownUid, receiverUid);
        /*
        String conversationString = "chat between " + ownUid + " (" + authDisplayName + ")"
                + " and " + receiveUserId + " (" + receiveUserDisplayName + ")"
                + " in room " + chatroomId;
         */
        String  conversationString = "DB chat with " + receiveUserDisplayName;
        //header.setText(conversationString);
        Log.i(TAG, conversationString);

        // get the last 50 messages from database
        // On the main screen of your app, you may want to show the 50 most recent chat messages.
        // With Firebase you would use the following query:
        Query query = messagesDatabase
                .child(chatroomId);
        //.limitToLast(10); // show the last 10 messages
        //.limitToLast(50); // show the last 50 messages
        // The FirebaseRecyclerAdapter binds a Query to a RecyclerView. When data is added, removed,
        // or changed these updates are automatically applied to your UI in real time.
        // First, configure the adapter by building FirebaseRecyclerOptions. In this case we will
        // continue with our chat example:
        FirebaseRecyclerOptions<MessageModel> options =
                new FirebaseRecyclerOptions.Builder<MessageModel>()
                        .setQuery(query, MessageModel.class)
                        .build();

        // Connecting object of required Adapter class to
        // the Adapter class itself
        firebaseRecyclerAdapter = new DatabaseChatRecyclerAdapter(options, this);
        //firebaseRecyclerAdapter = new ChatRecyclerAdapter(options, this);
        messagesList.setAdapter(firebaseRecyclerAdapter);
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