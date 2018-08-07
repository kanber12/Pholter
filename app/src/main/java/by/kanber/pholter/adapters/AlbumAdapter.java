package by.kanber.pholter.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;

import by.kanber.pholter.interfaces.ItemTouchHelperAdapter;
import by.kanber.pholter.R;
import by.kanber.pholter.models.Album;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> implements ItemTouchHelperAdapter {
    private Context context;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;
    private OnStartDragListener dragListener;
    private OnMoveCompleteListener moveCompleteListener;
    private ArrayList<Album> albums;
    private boolean isActionsMode = false, isEditMode = false;

    public void setOnItemClickListener(OnItemClickListener listener) {
        clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        longClickListener = listener;
    }

    public void setOnStartDragListener(OnStartDragListener listener) {
        dragListener = listener;
    }

    public void setOnMoveCompleteListener(OnMoveCompleteListener listener) {
        moveCompleteListener = listener;
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        public ImageView albumThumbnailImageView;
        public TextView albumNameTextView;
        public FrameLayout albumSelectFrameLayout;

        public AlbumViewHolder(View itemView, final OnItemClickListener clickListener, final OnItemLongClickListener longClickListener, final OnStartDragListener dragListener) {
            super(itemView);

            albumNameTextView = itemView.findViewById(R.id.album_name_text);
            albumThumbnailImageView = itemView.findViewById(R.id.album_thumbnail_image);
            albumSelectFrameLayout = itemView.findViewById(R.id.album_back_layout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION)
                        clickListener.onItemClick(position);
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int position = getAdapterPosition();

                    if (position != RecyclerView.NO_POSITION)
                        longClickListener.onItemLongClick(position);

                    return true;
                }
            });

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN)
                        dragListener.onStartDrag(AlbumViewHolder.this);

                    if (event.getAction() == MotionEvent.ACTION_UP)
                        v.performClick();

                    return false;
                }
            });
        }
    }

    public AlbumAdapter(Context context, ArrayList<Album> albums) {
        this.context = context;
        this.albums = albums;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.album_item, parent, false);

        return new AlbumViewHolder(view, clickListener, longClickListener, dragListener);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);

        holder.albumNameTextView.setText(album.getName());

        if (!album.getThumbnail().equals(""))
            Glide.with(context).load(album.getThumbnail()).into(holder.albumThumbnailImageView);
        else
            holder.albumThumbnailImageView.setImageResource(R.drawable.no_thumb_placeholder);

        if (album.isSelected())
            holder.albumSelectFrameLayout.setVisibility(View.VISIBLE);
        else
            holder.albumSelectFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition)
            for (int i = fromPosition; i < toPosition; i++)
                Collections.swap(albums, i, i + 1);
        else
            for (int i = fromPosition; i > toPosition; i--)
                Collections.swap(albums, i, i - 1);

        moveCompleteListener.onMoveComplete();
        notifyItemMoved(fromPosition, toPosition);

        return true;
    }

    public boolean isActionsMode() {
        return isActionsMode;
    }

    public void setActionsMode(boolean actionsMode) {
        isActionsMode = actionsMode;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public interface OnStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnMoveCompleteListener {
        void onMoveComplete();
    }
}
