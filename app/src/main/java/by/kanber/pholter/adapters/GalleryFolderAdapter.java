package by.kanber.pholter.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import by.kanber.pholter.models.GalleryFolder;
import by.kanber.pholter.R;

public class GalleryFolderAdapter extends RecyclerView.Adapter<GalleryFolderAdapter.GalleryFoldersViewHolder> {
    private Context context;
    private OnItemClickListener clickListener;
    private ArrayList<GalleryFolder> folders;

    public static class GalleryFoldersViewHolder extends RecyclerView.ViewHolder {
        public ImageView folderThumbImageView;
        public TextView folderNameTextView;

        public GalleryFoldersViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            folderNameTextView = itemView.findViewById(R.id.album_name_text);
            folderThumbImageView = itemView.findViewById(R.id.album_thumbnail_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION)
                        listener.onItemClick(position);
                }
            });
        }
    }

    public GalleryFolderAdapter(Context context, ArrayList<GalleryFolder> folders) {
        this.context = context;
        this.folders = folders;
    }

    @NonNull
    @Override
    public GalleryFoldersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);

        return new GalleryFoldersViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryFoldersViewHolder holder, int position) {
        GalleryFolder folder = folders.get(position);

        holder.folderNameTextView.setText(folder.getName());
        Glide.with(context).load(folder.getImages().get(0).getUri()).into(holder.folderThumbImageView);
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
