package de.androidcrypto.firebaseuitutorial.storage;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import de.androidcrypto.firebaseuitutorial.R;

import java.util.ArrayList;

public class StorageListImageAdapter extends RecyclerView.Adapter<StorageListImageAdapter.ViewHolder> {

    private ArrayList<String> imageList;
    private ArrayList<String> imageNameList;

    public StorageListImageAdapter(ArrayList<String> imageList, ArrayList<String> imageNameList, Context context) {
        this.imageList = imageList;
        this.imageNameList = imageNameList;
        this.context = context;
    }

    private Context context;
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.images_list_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // loading the images from the position
        holder.imageName.setText(imageNameList.get(position));
        Glide.with(holder.itemView.getContext()).load(imageList.get(position)).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView imageName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ivListImage);
            imageName = itemView.findViewById(R.id.tvImageName);
        }
    }
}
