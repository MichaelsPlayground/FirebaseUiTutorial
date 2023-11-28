package de.androidcrypto.firebaseuitutorial.database;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.firebaseuitutorial.ItemClickListener;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.MessageModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

// FirebaseRecyclerAdapter is a class provided by
// FirebaseUI. it provides functions to bind, adapt and show
// database contents in a Recycler View
public class MessageModelAdapter extends FirebaseRecyclerAdapter<
        MessageModel, MessageModelAdapter.MessageModelViewholder> {

    //private static ItemClickListener clickListener;
    //public List<MessageModel> userList = new ArrayList<>();
    public MessageModelAdapter(
            @NonNull FirebaseRecyclerOptions<MessageModel> options)
    {
        super(options);
    }

    // Function to bind the view in Card view (here "user.xml") with data in
    // model class (here "UserModel.class")
    @Override
    protected void
    onBindViewHolder(@NonNull MessageModelViewholder holder,
                     int position, @NonNull MessageModel model)
    {
        if(model.getSenderId().equals(FirebaseUtils.getCurrentUserId())){
            holder.leftChatLayout.setVisibility(View.GONE);
            holder.rightChatLayout.setVisibility(View.VISIBLE);
            holder.rightChatTextview.setText(model.getMessage());
            //holder.rightChatTimeTextview.setText(FirebaseUtils.timestampFullToString(model.getMessageTimestamp()));

            //Timestamp a = model.getMessageTimestamp();
            //String b = FirebaseUtils.timestampFullToString(a);
            //System.out.println("*** b: " + b);
            //holder.rightChatTimeTextview.setText(FirebaseUtils.timestampFullToString(model.getMessageTimestamp()));
        }else{
            holder.rightChatLayout.setVisibility(View.GONE);
            holder.leftChatLayout.setVisibility(View.VISIBLE);
            holder.leftChatTextview.setText(model.getMessage());
            //Timestamp a = model.getMessageTimestamp();
            //String b = FirebaseUtils.timestampFullToString(a);
            //System.out.println("*** b: " + b);
            //holder.leftChatTimeTextview.setText(FirebaseUtils.timestampFullToString(model.getMessageTimestamp()));
        }
/*
        userList.add(model);

        // Add firstname from model class (here
        // "UserModel.class") to appropriate view in Card
        // view (here "user.xml")
        holder.message.setText(model.getMessage());

        // Add lastname from model class (here
        // "person.class")to appropriate view in Card
        // view (here "person.xml")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String messageTime = dateFormat.format(model.getMessageTime());
        holder.messageTime.setText(messageTime);
        //holder.userId.setText(model.getUserId()); // dummy

 */
    }

    /*
    public void setClickListener(ItemClickListener itemClickListener) {
        clickListener = itemClickListener;
    }

     */

    // Function to tell the class about the Card view (here
    // "person.xml")in
    // which the data will be shown
    @NonNull
    @Override
    public MessageModelViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType)
    {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_message_recycler_row, parent, false);
        return new MessageModelViewholder(view);
    }

    // Sub Class to create references of the views in Card
    // view (here "person.xml")
    static class MessageModelViewholder
            extends RecyclerView.ViewHolder{

        LinearLayout leftChatLayout,rightChatLayout;
        TextView leftChatTextview,rightChatTextview;
        TextView leftChatTimeTextview, rightChatTimeTextview;


        //private TextView message, messageTime, userId;
        public MessageModelViewholder(@NonNull View itemView)
        {
            super(itemView);
            //message = itemView.findViewById(R.id.message_text);
            //messageTime = itemView.findViewById(R.id.name_text);
            //userId = itemView.findViewById(R.id.userId); // dummy
            //itemView.setOnClickListener(this);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            leftChatTimeTextview = itemView.findViewById(R.id.left_chat_time_textview);
            rightChatTimeTextview = itemView.findViewById(R.id.right_chat_time_textview);

        }
/*
        @Override
        public void onClick(View view) {
            if (clickListener != null) clickListener.onClick(view, getBindingAdapterPosition());
        }
        */

    }
}