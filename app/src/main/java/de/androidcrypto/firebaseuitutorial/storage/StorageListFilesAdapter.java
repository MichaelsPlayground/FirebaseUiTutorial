package de.androidcrypto.firebaseuitutorial.storage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.androidcrypto.firebaseuitutorial.R;
import de.androidcrypto.firebaseuitutorial.utils.FirebaseUtils;

public class StorageListFilesAdapter extends RecyclerView.Adapter<StorageListFilesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<StorageReference> arrayList;
    private OnItemClickListener onItemClickListener;

    public StorageListFilesAdapter(Context context, ArrayList<StorageReference> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.file_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //Glide.with(context).load(arrayList.get(position).getUri()).into(holder.imageView);
        holder.textView.setText(arrayList.get(position).getName());
        StorageReference itemReference = arrayList.get(position).getParent();
        if (itemReference.equals(FirebaseUtils.getStorageCurrentUserStorageImagesReference())) {
            holder.imageView.setImageResource(R.drawable.outline_image_24);
        } else {
            holder.imageView.setImageResource(R.drawable.outline_file_copy_24);
        }
        holder.itemView.setOnClickListener(view -> onItemClickListener.onClick(arrayList.get(position)));
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public ArrayList<StorageReference> getArrayList() {
        return arrayList;
    }

    public void removeItem(int position) {
        arrayList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.list_item_image);
            textView = itemView.findViewById(R.id.list_item_title);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(StorageReference storageReference);
    }

}
