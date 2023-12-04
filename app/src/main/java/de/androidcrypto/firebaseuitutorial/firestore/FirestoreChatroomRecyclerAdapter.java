package de.androidcrypto.firebaseuitutorial.firestore;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.firestore.FirestoreChatActivity;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.ChatroomModel;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;
import de.hdodenhof.circleimageview.CircleImageView;

public class FirestoreChatroomRecyclerAdapter extends FirestoreRecyclerAdapter<ChatroomModel, FirestoreChatroomRecyclerAdapter.ChatroomModelViewHolder> {

    Context context;

    public FirestoreChatroomRecyclerAdapter(@NonNull FirestoreRecyclerOptions<ChatroomModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ChatroomModelViewHolder holder, int position, @NonNull ChatroomModel model) {
        boolean lastMessageSentByMe = model.getLastMessageSenderId().equals(FirebaseUtils.getCurrentUserId());

        if(lastMessageSentByMe) {
            holder.usernameEmailText.setText(model.getReceiverName() + " (" + model.getReceiverEmail() + ")");
            holder.lastMessageText.setText("You : " + model.getLastMessage());
            if (!TextUtils.isEmpty(model.getReceiverPhotoUrl())) {
                GlideApp.with(context)
                        .load(model.getReceiverPhotoUrl())
                        .into(holder.profileImage);
            }
        }
        else {
            holder.usernameEmailText.setText(model.getSenderName() + " (" + model.getSenderEmail() + ")");
            holder.lastMessageText.setText("Oth : " + model.getLastMessage());
            if (!TextUtils.isEmpty(model.getSenderPhotoUrl())) {
                GlideApp.with(context)
                        .load(model.getSenderPhotoUrl())
                        .into(holder.profileImage);
            }
        }
        holder.lastMessageTime.setText(TimeUtils.getZoneDatedStringMediumLocale(model.getLastMessageTime()));

        holder.itemView.setOnClickListener(v -> {
            //navigate to chat activity
            Intent intent = new Intent(context, FirestoreChatActivity.class);
            String otherUserId = FirebaseUtils.getFirestoreOtherUserIdFromChatroom(model.getUserIds());
            UserModel otherUserModel;
            if (lastMessageSentByMe) {
                otherUserModel = new UserModel(otherUserId, model.getReceiverName(), model.getReceiverEmail(), model.getReceiverPhotoUrl());
            } else {
                otherUserModel = new UserModel(otherUserId, model.getSenderName(), model.getSenderEmail(), model.getSenderPhotoUrl());
            }
            AndroidUtils.passUserModelAsIntent(intent, otherUserModel);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chatroom_recycler_row,parent,false);
        return new ChatroomModelViewHolder(view);
    }

    class ChatroomModelViewHolder extends RecyclerView.ViewHolder{
        TextView usernameEmailText;
        TextView lastMessageText;
        TextView lastMessageTime;
        CircleImageView profileImage;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameEmailText = itemView.findViewById(R.id.user_name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            lastMessageTime = itemView.findViewById(R.id.last_message_time_text);
            profileImage = itemView.findViewById(R.id.ciProfileImage);
        }
    }
}
