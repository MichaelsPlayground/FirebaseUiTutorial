package de.androidcrypto.firebaseuitutorial.database;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    public UserModelAdapter(
            @NonNull FirebaseRecyclerOptions<UserModel> options)
    {
        super(options);
    }

    // Function to bind the view in Card view (here "user.xml") with data in
    // model class (here "UserModel.class")
    @Override
    protected void
    onBindViewHolder(@NonNull UserModelViewholder holder,
                     int position, @NonNull UserModel model)
    {

        userList.add(model);

        // Add firstname from model class (here
        // "UserModel.class") to appropriate view in Card
        // view (here "user.xml")
        holder.userEmail.setText(model.getUserMail());

        // Add lastname from model class (here
        // "person.class")to appropriate view in Card
        // view (here "person.xml")
        holder.userDisplayName.setText(model.getUserName());
        holder.userId.setText(model.getUserId()); // dummy
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

    // Function to tell the class about the Card view (here
    // "person.xml")in
    // which the data will be shown
    @NonNull
    @Override
    public UserModelViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user, parent, false);
        return new UserModelViewholder(view);
    }

    // Sub Class to create references of the views in Card
    // view (here "person.xml")
    static class UserModelViewholder
            extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView userEmail, userDisplayName, userId;
        public UserModelViewholder(@NonNull View itemView)
        {
            super(itemView);
            userEmail = itemView.findViewById(R.id.userEmail);
            userDisplayName = itemView.findViewById(R.id.userDisplayName);
            userId = itemView.findViewById(R.id.userId); // dummy
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getBindingAdapterPosition(), userId.getText().toString());
        }
    }
}