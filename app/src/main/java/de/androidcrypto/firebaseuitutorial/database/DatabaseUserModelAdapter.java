package de.androidcrypto.firebaseuitutorial.database;

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

import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class DatabaseUserModelAdapter extends FirebaseRecyclerAdapter<
        UserModel, DatabaseUserModelAdapter.UserModelViewholder> {

    public List<UserModel> userList = new ArrayList<>();
    private boolean isChat;
    private String ownUserId;
    private Context context;

    public DatabaseUserModelAdapter(
            @NonNull FirebaseRecyclerOptions<UserModel> options, boolean isChat, String ownUserId, Context context) {
        super(options);
        this.isChat = isChat;
        this.ownUserId = ownUserId;
        this.context = context;
    }

    @Override
    protected void
    onBindViewHolder(@NonNull UserModelViewholder holder,
                     int position, @NonNull UserModel model) {
        if (!model.getUserId().equals(ownUserId)) {
            userList.add(model);
            holder.userNameEmail.setText(model.getUserName() + " (" + model.getUserMail() + ")");
            //holder.userNameEmail.setText(AndroidUtils.shortenString(model.getUserName() + " (" + model.getUserMail() + ")", 20));
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
                Intent intent = new Intent(context, DatabaseChatActivity.class);
                UserModel otherUserModel = new UserModel(model.getUserId(), model.getUserName(), model.getUserMail(), model.getUserPhotoUrl());
                AndroidUtils.passUserModelAsIntent(intent, otherUserModel);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        } else {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }
    }

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
    static class UserModelViewholder extends RecyclerView.ViewHolder {
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
        }
    }
}