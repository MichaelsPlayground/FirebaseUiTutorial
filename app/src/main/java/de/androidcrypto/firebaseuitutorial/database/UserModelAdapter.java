package de.androidcrypto.firebaseuitutorial.database;

import android.content.Intent;
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

import de.androidcrypto.firebaseuitutorial.ItemClickListener;
import de.androidcrypto.firebaseuitutorial.models.UserModel;
import de.androidcrypto.firebaseuitutorial.R;

// FirebaseRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class UserModelAdapter extends FirebaseRecyclerAdapter<
        UserModel, UserModelAdapter.UserModelViewholder> {

    private static ItemClickListener clickListener;
    public List<UserModel> userList = new ArrayList<>();
    private boolean ischat;
    private String ownUserId;

    public UserModelAdapter(
            @NonNull FirebaseRecyclerOptions<UserModel> options, boolean ischat, String ownUserId) {
        super(options);
        this.ischat = ischat;
        this.ownUserId = ownUserId;
    }

    // Function to bind the view in Card view (here "user.xml") with data in
    // model class (here "UserModel.class")
    @Override
    protected void
    onBindViewHolder(@NonNull UserModelViewholder holder,
                     int position, @NonNull UserModel model) {

        if (!model.getUserId().equals(ownUserId)) {
            userList.add(model);

            holder.userEmail.setText(model.getUserMail());
            holder.userDisplayName.setText(model.getUserName());
            //holder.userId.setText(model.getUserId()); // dummy

            //if (ischat){
            //    lastMessage(user.getId(), holder.last_msg);
            //} else {
            //    holder.last_msg.setVisibility(View.GONE);
            //}

            if (ischat) {
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
            //holder.itemView.setVisibility(View.VISIBLE);

            holder.itemView.setOnClickListener(v -> {
                //navigate to chat activity
            /*
            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent,otherUserModel);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);*/
                System.out.println("*** you clicked on userId: " + model.getUserId() + " ***");
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
        private TextView userEmail, userDisplayName, userId;
        private ImageView img_on;
        private ImageView img_off;

        public UserModelViewholder(@NonNull View itemView) {
            super(itemView);
            userEmail = itemView.findViewById(R.id.last_msg);
            userDisplayName = itemView.findViewById(R.id.username);
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