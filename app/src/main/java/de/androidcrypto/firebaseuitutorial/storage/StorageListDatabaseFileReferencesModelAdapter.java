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
import de.androidcrypto.firebaseuitutorial.models.FileInformationModel;
import de.androidcrypto.firebaseuitutorial.utils.AndroidUtils;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class StorageListDatabaseFileReferencesModelAdapter extends FirebaseRecyclerAdapter<
        FileInformationModel, StorageListDatabaseFileReferencesModelAdapter.FileInformationModelViewholder> {
    private Context context;

    public StorageListDatabaseFileReferencesModelAdapter(
            @NonNull FirebaseRecyclerOptions<FileInformationModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void
    onBindViewHolder(@NonNull FileInformationModelViewholder holder,
                     int position, @NonNull FileInformationModel model) {
        holder.fileMimeType.setText("Mime type: " + model.getMimeType());
        holder.fileName.setText("Name: " + model.getFileName());
        holder.fileSize.setText("Size: " + model.getFileSize() + " bytes");
        String mimeType = model.getMimeType();
        if (mimeType.startsWith("image")) {
            holder.fileTypeImage.setImageResource(R.drawable.outline_image_24);
            if (model.getFileStorage().equals(FirebaseUtils.STORAGE_IMAGES_RESIZED_FOLDER_NAME)) {
                if (!TextUtils.isEmpty(model.getDownloadUrlString())) {
                    // Download directly from StorageReference using Glide
                    // (See MyAppGlideModule for Loader registration)
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
            // this functionality is not implemented
            AndroidUtils.showToast(context, "This method is not implemented - do whatever you want to do with the clicked element: " + model.getFileName());
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