package by.kanber.pholter.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import by.kanber.pholter.R;
import by.kanber.pholter.model.GalleryImage;

public class GalleryImageAdapter extends RecyclerView.Adapter<GalleryImageAdapter.GalleryImagesViewHolder> {
    private Context context;
    private OnItemClickListener clickListener;
    private ArrayList<GalleryImage> images;

    public static class GalleryImagesViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageHolder;
        public FrameLayout imageSelectFrameLayout;

        public GalleryImagesViewHolder(View itemView, final OnItemClickListener listener) {
            super(itemView);

            imageSelectFrameLayout = itemView.findViewById(R.id.gallery_image_select);
            imageHolder = itemView.findViewById(R.id.gallery_image_holder);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });
        }
    }

    public GalleryImageAdapter(Context context, ArrayList<GalleryImage> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public GalleryImagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.gallery_image_item, parent, false);

        return new GalleryImagesViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryImagesViewHolder holder, int position) {
        GalleryImage image = images.get(position);

        Glide.with(context).load(image.getUri()).into(holder.imageHolder);

        if (image.isSelected()) {
            holder.imageSelectFrameLayout.setVisibility(View.VISIBLE);
            holder.imageHolder.setPadding(10, 10, 10, 10);
        } else {
            holder.imageSelectFrameLayout.setVisibility(View.GONE);
            holder.imageHolder.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
