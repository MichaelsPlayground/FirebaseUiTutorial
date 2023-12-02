package de.androidcrypto.firebaseuitutorial.firestore;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.ItemClickListener;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.database.DatabaseChatActivity;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

// FirebaseRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class FirestoreUserModelAdapter extends FirestoreRecyclerAdapter<
        UserModel, FirestoreUserModelAdapter.UserModelViewholder> {

    private static ItemClickListener clickListener;
    public List<UserModel> userList = new ArrayList<>();
    private boolean isChat;
    private String ownUserId;
    private Context context;

    public FirestoreUserModelAdapter(
            @NonNull FirestoreRecyclerOptions<UserModel> options, boolean isChat, String ownUserId, Context context) {
        super(options);
        this.isChat = isChat;
        this.ownUserId = ownUserId;
        this.context = context;
    }

    // Function to bind the view in Card view (here "user.xml") with data in
    // model class (here "UserModel.class")
    @Override
    protected void
    onBindViewHolder(@NonNull UserModelViewholder holder,
                     int position, @NonNull UserModel model) {
        System.out.println("*** onBindViewHolder position: " + position);
        if (!model.getUserId().equals(ownUserId)) {
            userList.add(model);

            holder.userNameEmail.setText(AndroidUtils.shortenString(model.getUserName() + " (" + model.getUserMail() + ")", 20));
            // last online time
            long lastOnlineTime = model.getUserLastOnlineTime();
            if (lastOnlineTime > 1) {
                holder.userLastOnlineTime.setText(TimeUtils.getZoneDatedStringMediumLocale(lastOnlineTime));
            } else {
                holder.userLastOnlineTime.setText("not online");
            }
            if (isChat) {
                if (model.getUserOnlineString().equals("online")) {
                    holder.img_on.setVisibility(View.VISIBLE);
                    holder.img_off.setVisibility(View.GONE);
                } else {
                    holder.img_on.setVisibility(View.GONE);
                    holder.img_off.setVisibility(View.VISIBLE);
                }
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.GONE);
            }
            // if a PhotoUrl is available try to load the profile image
            if (!TextUtils.isEmpty(model.getUserPhotoUrl())) {
                GlideApp.with(context)
                        .load(model.getUserPhotoUrl())
                        .into(holder.userProfileImage);
            }

            //holder.itemView.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(v -> {
                //navigate to chat activity
            /*
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent,otherUserModel);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);*/
                System.out.println("*** you clicked on userId: " + model.getUserId() + " ***");
                Intent intent = new Intent(context, FirestoreChatActivity.class);
                intent.putExtra("UID", model.getUserId());
                intent.putExtra("EMAIL", model.getUserMail());
                intent.putExtra("DISPLAYNAME", model.getUserName());
                intent.putExtra("AUTH_EMAIL", "test@test.com");
                intent.putExtra("AUTH_DISPLAYNAME", "authDisplayName");
                intent.putExtra("PROFILE_IMAGE", model.getUserPhotoUrl());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                ((Activity)context).finish();
            });
        } else {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    // Function to tell the class about the Card view in which the data will be shown
    @NonNull
    @Override
    public UserModelViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType) {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new UserModelViewholder(view);
    }

    // Sub Class to create references of the views in Card
    // view (here "person.xml")
    static class UserModelViewholder
            extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView userNameEmail, userLastOnlineTime, userId;
        private ImageView userProfileImage;
        private ImageView img_on;
        private ImageView img_off;

        public UserModelViewholder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            userNameEmail = itemView.findViewById(R.id.userNameEmail);
            userLastOnlineTime = itemView.findViewById(R.id.userLastOnlineTime);
            userId = itemView.findViewById(R.id.userId); // dummy
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null)
                clickListener.onClick(view, getBindingAdapterPosition(), userId.getText().toString());
        }
    }
}