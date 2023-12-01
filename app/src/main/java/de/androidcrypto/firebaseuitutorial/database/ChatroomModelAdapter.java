package de.androidcrypto.firebaseuitutorial.database;

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

import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.ItemClickListener;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.ChatroomModel;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

// FirebaseRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class ChatroomModelAdapter extends FirebaseRecyclerAdapter<
        ChatroomModel, ChatroomModelAdapter.ChatroomModelViewholder> {

    private static ItemClickListener clickListener;
    public List<ChatroomModel> userList = new ArrayList<>();
    private boolean isChat;
    private String ownUserId;
    private Context context;

    public ChatroomModelAdapter(
            @NonNull FirebaseRecyclerOptions<ChatroomModel> options, boolean isChat, String ownUserId, Context context) {
        super(options);
        this.isChat = isChat;
        this.ownUserId = ownUserId;
        this.context = context;
    }

    // Function to bind the view in Card view with data in

    @Override
    protected void
    onBindViewHolder(@NonNull ChatroomModelViewholder holder,
                     int position, @NonNull ChatroomModel model) {

        boolean user1IsOwn = false;
        if (!model.getUserId1().equals(ownUserId)) {
            user1IsOwn = true;
        }
        String timeMessage;
        if (user1IsOwn) {
            // show userData2
            holder.userNameEmail.setText(AndroidUtils.shortenString(model.getUserName2() + " (" + model.getUserEmail2() + ")", 20));
            timeMessage = TimeUtils.getZoneDatedStringShortLocale(model.getChatLastTime());
            if (model.getLastMessageFromUserId().equals(ownUserId)) {
                timeMessage += " Wrt:" + model.getLastMessage();
            } else {
                timeMessage += " Rcvd:" + model.getLastMessage();
            }
            /*
            we need to lookup this in users database
            if (model.getUserOnlineString().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
             */
            // if a PhotoUrl is available try to load the profile image
            if (!TextUtils.isEmpty(model.getUserProfileImage2())) {
                GlideApp.with(context)
                        .load(model.getUserProfileImage2())
                        .into(holder.userProfileImage);
            }
        } else {
            // show userData1
            holder.userNameEmail.setText(AndroidUtils.shortenString(model.getUserName1() + " (" + model.getUserEmail1() + ")", 20));
            timeMessage = TimeUtils.getZoneDatedStringShortLocale(model.getChatLastTime());
            if (model.getLastMessageFromUserId().equals(ownUserId)) {
                timeMessage += "Wrt:" + model.getLastMessage();
            } else {
                timeMessage += "Rcvd::" + model.getLastMessage();
            }
            /*
            we need to lookup this in users database
            if (model.getUserOnlineString().equals("online")) {
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            } else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }
             */
            if (!TextUtils.isEmpty(model.getUserProfileImage1())) {
                GlideApp.with(context)
                        .load(model.getUserProfileImage1())
                        .into(holder.userProfileImage);
            }
        }
        holder.chatroomLastTimeMessage.setText(timeMessage);

        //holder.itemView.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            //navigate to chat activity
            /*
            System.out.println("*** you clicked on userId: " + model.getUserId() + " ***");
            Intent intent = new Intent(context, DatabaseChatActivity.class);
            intent.putExtra("UID", model.getUserId());
            intent.putExtra("EMAIL", model.getUserMail());
            intent.putExtra("DISPLAYNAME", model.getUserName());
            intent.putExtra("AUTH_EMAIL", "test@test.com");
            intent.putExtra("AUTH_DISPLAYNAME", "authDisplayName");
            intent.putExtra("PROFILE_IMAGE", model.getUserPhotoUrl());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ((Activity) context).finish();
            */
        });
    }


    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    // Function to tell the class about the Card view in which the data will be shown
    @NonNull
    @Override
    public ChatroomModelViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType) {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chatroom_item, parent, false);
        return new ChatroomModelViewholder(view);
    }

    // Sub Class to create references of the views in Card
    // view (here "person.xml")
    static class ChatroomModelViewholder
            extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView userNameEmail, chatroomLastTimeMessage, userId;
        private ImageView userProfileImage;
        private ImageView img_on;
        private ImageView img_off;

        public ChatroomModelViewholder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.userProfileImage);
            userNameEmail = itemView.findViewById(R.id.userNameEmail);
            chatroomLastTimeMessage = itemView.findViewById(R.id.chatroomLastTimeMessage);
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