package de.androidcrypto.firebaseuitutorial.database;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.MessageModel;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class ChatRecyclerAdapter extends FirebaseRecyclerAdapter<MessageModel, ChatRecyclerAdapter.ModelViewHolder> {

    Context context;

    public ChatRecyclerAdapter(@NonNull FirebaseRecyclerOptions<MessageModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ModelViewHolder holder, int position, @NonNull MessageModel model) {
        if(model.getSenderId().equals(FirebaseUtils.getCurrentUserId())){
          holder.leftChatLayout.setVisibility(View.GONE);
          holder.rightChatLayout.setVisibility(View.VISIBLE);
          holder.rightChatTextview.setText(model.getMessage());
          holder.rightChatTimeTextview.setText(FirebaseUtils.timestampFullToString(model.getMessageTimestamp())); // added
       }else{
           holder.rightChatLayout.setVisibility(View.GONE);
           holder.leftChatLayout.setVisibility(View.VISIBLE);
           holder.leftChatTextview.setText(model.getMessage());
           holder.leftChatTimeTextview.setText(FirebaseUtils.timestampFullToString(model.getMessageTimestamp())); // added
       }
    }

    @NonNull
    @Override
    public ModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_message_recycler_row,parent,false);
        return new ModelViewHolder(view);
    }

    class ModelViewHolder extends RecyclerView.ViewHolder{

        LinearLayout leftChatLayout,rightChatLayout;
        TextView leftChatTextview,rightChatTextview;
        TextView leftChatTimeTextview, rightChatTimeTextview; // added

        public ModelViewHolder(@NonNull View itemView) {
            super(itemView);

            leftChatLayout = itemView.findViewById(R.id.left_chat_layout);
            rightChatLayout = itemView.findViewById(R.id.right_chat_layout);
            leftChatTextview = itemView.findViewById(R.id.left_chat_textview);
            rightChatTextview = itemView.findViewById(R.id.right_chat_textview);
            leftChatTimeTextview = itemView.findViewById(R.id.left_chat_time_textview); // added
            rightChatTimeTextview = itemView.findViewById(R.id.right_chat_time_textview); // added
        }
    }
}
