package de.androidcrypto.firebaseuitutorial.storage;

import android.content.Context;
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
import de.androidcrypto.firebaseuitutorial.models.FileInformation;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;
import de.androidcrypto.firebaseuitutorial.utils.TimeUtils;

public class StorageListFileReferencesModelAdapter extends FirebaseRecyclerAdapter<
        FileInformation, StorageListFileReferencesModelAdapter.FileInformationModelViewholder> {
    private Context context;

    public StorageListFileReferencesModelAdapter(
            @NonNull FirebaseRecyclerOptions<FileInformation> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void
    onBindViewHolder(@NonNull FileInformationModelViewholder holder,
                     int position, @NonNull FileInformation model) {
        holder.message.setText(model.getMimeType());
        holder.userNameEmail.setText(model.getFileName());
        holder.messageTime.setText("Size: " + model.getFileSize() + " bytes");
        String mimeType = model.getMimeType();
        if (mimeType.startsWith("image")) {
            holder.fileTypeImage.setImageResource(R.drawable.outline_image_24);
        } else {
            holder.fileTypeImage.setImageResource(R.drawable.outline_file_present_24);
        }


        //holder.itemView.setVisibility(View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            //navigate to chat activity
/*
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

 */
        });
    }

    // Function to tell the class about the Card view in which the data will be shown
    @NonNull
    @Override
    public FileInformationModelViewholder
    onCreateViewHolder(@NonNull ViewGroup parent,
                       int viewType) {
        View view
                = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.storage_file_recycler_row, parent, false);
        return new FileInformationModelViewholder(view);
    }

    static class FileInformationModelViewholder
            extends RecyclerView.ViewHolder {
        private ImageView fileTypeImage;
        private TextView message, userNameEmail, messageTime;

        public FileInformationModelViewholder(@NonNull View itemView) {
            super(itemView);
            fileTypeImage = itemView.findViewById(R.id.ciFileTypeImage);
            userNameEmail = itemView.findViewById(R.id.tvFileName);
            message = itemView.findViewById(R.id.tvFileMimeType);
            messageTime = itemView.findViewById(R.id.tvFileSize);
        }
    }
}