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
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.androidcrypto.firebaseuitutorial.GlideApp;
import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.models.FileInformation;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class StorageListFirestoreFileReferencesModelAdapter extends FirestoreRecyclerAdapter<
        FileInformation, StorageListFirestoreFileReferencesModelAdapter.FileInformationModelViewholder> {
    private Context context;

    public StorageListFirestoreFileReferencesModelAdapter(
            @NonNull FirestoreRecyclerOptions<FileInformation> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void
    onBindViewHolder(@NonNull FileInformationModelViewholder holder,
                     int position, @NonNull FileInformation model) {
        holder.fileMimeType.setText("Mime type: " + model.getMimeType());
        holder.fileName.setText("Name: " + model.getFileName());
        holder.fileSize.setText("Size: " + model.getFileSize() + " bytes");
        String mimeType = model.getMimeType();
        if (mimeType.startsWith("image")) {
            holder.fileTypeImage.setImageResource(R.drawable.outline_image_24);
            System.out.println("*** model.getFileStorage:" + model.getFileStorage());
            if (model.getFileStorage().equals(FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME)) {
                if (!TextUtils.isEmpty(model.getDownloadUrlString())) {
                    // Download directly from StorageReference using Glide
                    // (See MyAppGlideModule for Loader registration)
                    System.out.println("*** model.getDownloadUrlString:" + model.getDownloadUrlString());
                    GlideApp.with(context)
                            .load(model.getDownloadUrlString())
                            .into(holder.fileTypeImage);
                }
            }
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
        private TextView fileName, fileSize, fileMimeType;

        public FileInformationModelViewholder(@NonNull View itemView) {
            super(itemView);
            fileTypeImage = itemView.findViewById(R.id.ciFileTypeImage);
            fileName = itemView.findViewById(R.id.tvFileName);
            fileMimeType = itemView.findViewById(R.id.tvFileMimeType);
            fileSize = itemView.findViewById(R.id.tvFileSize);
        }
    }
}