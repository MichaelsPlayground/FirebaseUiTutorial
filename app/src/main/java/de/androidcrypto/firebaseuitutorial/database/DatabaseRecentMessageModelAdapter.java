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

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.RecentMessageModel;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class DatabaseRecentMessageModelAdapter extends FirebaseRecyclerAdapter<
        RecentMessageModel, DatabaseRecentMessageModelAdapter.RecentMessageModelViewholder> {
    private Context context;

    public DatabaseRecentMessageModelAdapter(
            @NonNull FirebaseRecyclerOptions<RecentMessageModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void
    onBindViewHolder(@NonNull RecentMessageModelViewholder holder,
                     int position, @NonNull RecentMessageModel model) {
        holder.message.setText(model.getChatMessage());
        holder.userNameEmail.setText(model.getUserName() + " (" + model.getUserEmail() + ")");
        holder.messageTime.setText(TimeUtils.getZoneDatedStringMediumLocale(model.getChatLastTime()));
        if (!TextUtils.isEmpty(model.getUserProfileImage())) {
            GlideApp.with(context)
                    .load(model.getUserProfileImage())
                    .into(holder.userProfileImage);
        }

        //holder.itemView.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            //navigate to chat activity

            Intent intent = new Intent(context, DatabaseChatActivity.class);
            intent.putExtra("UID", model.getUserId());
            intent.putExtra("EMAIL", model.getUserEmail());
            intent.putExtra("DISPLAYNAME", model.getUserName());
            intent.putExtra("AUTH_EMAIL", "test@test.com");
            intent.putExtra("AUTH_DISPLAYNAME", "authDisplayName");
            intent.putExtra("PROFILE_IMAGE", model.getUserProfileImage());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            ((Activity) context).finish();
        });
    }

    // Function to tell the class about the Card view in which the data will be shown
    @NonNull
    @Override
    public RecentMessageModelViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType) {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recent_message_item, parent, false);
        return new RecentMessageModelViewholder(view);
    }

    static class RecentMessageModelViewholder
            extends RecyclerView.ViewHolder {
        private ImageView userProfileImage;
        private TextView message, userNameEmail, messageTime;

        public RecentMessageModelViewholder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.ciUserProfileImage);
            userNameEmail = itemView.findViewById(R.id.tvUserNameEmail);
            message = itemView.findViewById(R.id.tvMessage);
            messageTime = itemView.findViewById(R.id.tvMessageTime);
        }
    }
}