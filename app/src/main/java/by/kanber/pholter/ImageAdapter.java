package by.kanber.pholter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private Context context;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;
    private ArrayList<Image> images;
    private boolean isActionMode = false, isEditMode = false;

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageHolder;
        public TextView imageDescription;
        public FrameLayout imageBackFrameLayout;

        public ImageViewHolder(View itemView, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener) {
            super(itemView);

            imageBackFrameLayout = itemView.findViewById(R.id.image_back);
            imageHolder = itemView.findViewById(R.id.image_holder);
            imageDescription = itemView.findViewById(R.id.image_description);

            imageHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION)
                        clickListener.onItemClick(position);
                }
            });

            imageHolder.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION)
                        longClickListener.onItemLongClick(position);

                    return true;
                }
            });
        }
    }

    public ImageAdapter(Context context, ArrayList<Image> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);

        return new ImageViewHolder(view, clickListener, longClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Image image = images.get(position);
        String description = image.getDescription();

        int[] size = Utils.getImageSize(Uri.parse(image.getUri()));
        resize(holder.imageHolder, size);

        Glide.with(context).load(image.getUri()).into(holder.imageHolder);

        if (position == images.size() - 1 && description.equals(""))
            holder.imageDescription.setVisibility(View.GONE);
        else {
            holder.imageDescription.setVisibility(View.VISIBLE);

            if (position != images.size() - 1)
                description += "\n\n\n\n\n\n";
        }

        holder.imageDescription.setText(description);

        if (image.isSelected()) {
            holder.imageBackFrameLayout.setVisibility(View.VISIBLE);
            holder.imageHolder.setPadding(15, 15, 15, 15);
        } else {
            holder.imageBackFrameLayout.setVisibility(View.GONE);
            holder.imageHolder.setPadding(0, 0, 0, 0);
        }
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    private void resize(ImageView imageView, int[] size) {
        int width = Utils.getDisplayWidth(context);
        double heightMultiplier = size[0] * 1.0 / width;
        int height = (int) (size[1] / heightMultiplier);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageView.getLayoutParams();
        params.height = height;
        imageView.setLayoutParams(params);
    }

    public boolean isActionMode() {
        return isActionMode;
    }

    public void setActionMode(boolean actionMode) {
        isActionMode = actionMode;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        longClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
}
