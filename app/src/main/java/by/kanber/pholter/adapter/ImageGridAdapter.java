package by.kanber.pholter.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;

import by.kanber.pholter.interfaces.ItemTouchHelperAdapter;
import by.kanber.pholter.R;
import by.kanber.pholter.model.Image;

public class ImageGridAdapter extends RecyclerView.Adapter<ImageGridAdapter.ImageGridViewHolder> implements ItemTouchHelperAdapter {
    private Context context;
    private ArrayList<Image> images;
    private OnItemClickListener clickListener;
    private OnMoveCompleteListener moveCompleteListener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    public void setOnMoveCompleteListener(OnMoveCompleteListener listener) {
        moveCompleteListener = listener;
    }

    public static class ImageGridViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageHolder;

        public ImageGridViewHolder(View itemView, final OnItemClickListener clickListener) {
            super(itemView);

            imageHolder = itemView.findViewById(R.id.grid_image_holder);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION)
                        clickListener.onItemClick(position);
                }
            });
        }
    }

    public ImageGridAdapter(Context context, ArrayList<Image> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ImageGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_image_item, parent, false);

        return new ImageGridViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageGridViewHolder holder, int position) {
        Image image = images.get(position);

        Glide.with(context).load(image.getUri()).into(holder.imageHolder);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(images, i, i + 1);
        else
            for (int i = fromPosition; i > toPosition ; i--)
                Collections.swap(images, i, i - 1);

        moveCompleteListener.onMoveComplete();
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnMoveCompleteListener {
        void onMoveComplete();
    }
}
