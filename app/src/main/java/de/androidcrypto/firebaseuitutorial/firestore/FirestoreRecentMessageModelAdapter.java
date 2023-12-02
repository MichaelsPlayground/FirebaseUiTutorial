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

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.ItemClickListener;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.database.DatabaseChatActivity;
import de.androidcrypto.firebaseuitutorial.models.RecentMessageModel;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

// FirestoreRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class FirestoreRecentMessageModelAdapter extends FirestoreRecyclerAdapter<
        RecentMessageModel, FirestoreRecentMessageModelAdapter.RecentMessageModelViewholder> {

    private static ItemClickListener clickListener;
    private Context context;

    public FirestoreRecentMessageModelAdapter(
            @NonNull FirestoreRecyclerOptions<RecentMessageModel> options, Context context) {
        super(options);
        this.context = context;
    }

    // Function to bind the view in Card view with data in

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

            System.out.println("*** you clicked on userId: " + model.getUserId() + " ***");

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


    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
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

    // Sub Class to create references of the views in Card
    // view (here "person.xml")
    static class RecentMessageModelViewholder
            extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView userProfileImage;
        private TextView message, userNameEmail, messageTime;

        public RecentMessageModelViewholder(@NonNull View itemView) {
            super(itemView);
            userProfileImage = itemView.findViewById(R.id.ciUserProfileImage);
            userNameEmail = itemView.findViewById(R.id.tvUserNameEmail);
            message = itemView.findViewById(R.id.tvMessage);
            messageTime = itemView.findViewById(R.id.tvMessageTime);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                //clickListener.onClick(view, getBindingAdapterPosition(), userId.getText().toString());
            }
        }
    }
}