package by.kanber.pholter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class AlbumFragment extends Fragment implements ImagePickerFragment.OnFragmentInteractionListener {
    private RecyclerView recyclerView;
    private TextView noImagesText;
    private Toolbar toolbar;
    private DBHelper helper;
    private MainActivity activity;
    private ImageAdapter adapter;
    private OnFragmentInteractionListener listener;
    private ItemTouchHelper touchHelper;

    private ArrayList<Image> images;
    private ArrayList<Integer> selectedImages;
    private int albumId;
    private String albumName;

    public AlbumFragment() {}

    public static AlbumFragment newInstance(int id, String name) {
        Bundle args = new Bundle();
        args.putInt("albumId", id);
        args.putString("albumName", name);
        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            albumId = getArguments().getInt("albumId");
            albumName = getArguments().getString("albumName");
        }

        activity = (MainActivity) getActivity();

        if (activity != null) {
            helper = activity.getHelper();
            selectedImages = new ArrayList<>();
            images = Image.getImages(helper, albumId);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);

        if (activity != null) {
            noImagesText = view.findViewById(R.id.no_images_text);
            recyclerView = view.findViewById(R.id.images_list);
            toolbar = view.findViewById(R.id.toolbar);

            activity.setSupportActionBar(toolbar);
            toolbar.setTitle(albumName);
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adapter.isActionMode())
                        unselectAllImages();
                    else
                        closeFragment();
                }
            });

            adapter = new ImageAdapter(activity, images);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(activity));
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    int id = images.get(position).getId();

                    if (adapter.isActionMode()) {
                        if (!selectedImages.contains(id)) {
                            selectedImages.add(id);
                            images.get(position).setSelected(true);
                        } else {
                            selectedImages.remove(Integer.valueOf(id));
                            images.get(position).setSelected(false);
                        }

                        if (selectedImages.size() == 0)
                            disableActionMode();
                        else
                            toolbar.setTitle(String.valueOf(selectedImages.size()));

                        updateImages();
                    } else {
                        Toast.makeText(activity, "Photo preview", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            adapter.setOnItemLongClickListener(new ImageAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(int position) {
                    if (!adapter.isActionMode()) {
                        selectedImages.add(images.get(position).getId());
                        images.get(position).setSelected(true);
                        enableActionMode();
                        updateImages();
                    }
                }
            });

            changeThumbnail();
            checkIsEmpty();

            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (adapter.isActionMode()) {
                        unselectAllImages();

                        return true;
                    }

                    if (adapter.isEditMode()) {
                        disableEditMode();

                        return true;
                    }

                    return false;
                }
            });
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (adapter.isActionMode()) {
            inflater.inflate(R.menu.images_list_action_mode_menu, menu);
        } else {
            if (adapter.isEditMode())
                inflater.inflate(R.menu.menu_done, menu);
            else {
                inflater.inflate(R.menu.main_menu, menu);

                menu.findItem(R.id.edit_list).setVisible(images.size() > 0);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item: showImagePicker(); return true;
            case R.id.edit_list: enableEditMode(); return true;
            case R.id.menu_done: disableEditMode(); return true;
            case R.id.images_list_delete: showDeleteDialog(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onImagePickerFragmentInteraction(ArrayList<String> selectedImages) {
        for (String uri : selectedImages) {
            Image image = new Image(0, uri, albumId);

            Image.insertOrUpdateDB(helper, image);
            image = Image.getLastImage(helper);
            images.add(0, image);
        }

        reindexImages();
        updateImages();
    }

    private void addDescription(int position, String description) {
        images.get(position).setDescription(description);
        Image.insertOrUpdateDB(helper, images.get(position));
        updateImages();
    }

    private void enableActionMode() {
        adapter.setActionMode(true);
        toolbar.setTitle(String.valueOf(selectedImages.size()));
        Utils.changeStatusBarColor(activity, R.color.colorPrimaryAction);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryAction));
        activity.invalidateOptionsMenu();
    }

    private void disableActionMode() {
        adapter.setActionMode(false);
        toolbar.setTitle(albumName);
        Utils.changeStatusBarColor(activity, R.color.colorPrimary);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        selectedImages.clear();
        activity.invalidateOptionsMenu();
    }

    private void enableEditMode() {
        adapter.setEditMode(true);
        toolbar.setNavigationIcon(null);
        toolbar.setTitle("Edit");
        activity.invalidateOptionsMenu();

        ImageGridAdapter gridAdapter = new ImageGridAdapter(activity, images);
        GridLayoutManager editManager = new GridLayoutManager(activity, 3);
        recyclerView.setLayoutManager(editManager);
        recyclerView.setAdapter(gridAdapter);

        ItemTouchHelperCallback callback = new ItemTouchHelperCallback(gridAdapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        gridAdapter.setOnItemClickListener(new ImageGridAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                showEnterDescriptionDialog(position, images.get(position).getDescription());
            }
        });

        gridAdapter.setOnMoveCompleteListener(new ImageGridAdapter.OnMoveCompleteListener() {
            @Override
            public void onMoveComplete() {
                reindexImages();
            }
        });
    }

    private void disableEditMode() {
        adapter.setEditMode(false);
        toolbar.setTitle(albumName);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        activity.invalidateOptionsMenu();

        recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        recyclerView.setAdapter(adapter);
        touchHelper.attachToRecyclerView(null);
    }

    private void showEnterDescriptionDialog(final int position, String description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View enterImageDescriptionView = activity.getLayoutInflater().inflate(R.layout.edit_text_dialog, null);
        final EditText enterDescriptionEditText = enterImageDescriptionView.findViewById(R.id.album_name_edit);
        enterDescriptionEditText.setHint("Enter text");
        enterDescriptionEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
        enterDescriptionEditText.append(description);

        builder.setView(enterImageDescriptionView)
                .setTitle("Add description")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Add", null);

        final AlertDialog enterNameDialog = builder.create();

        enterNameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                enterDescriptionEditText.requestFocus();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String description = enterDescriptionEditText.getText().toString();

                        addDescription(position, description);
                        enterNameDialog.cancel();
                    }
                });

                activity.openKeyboard();
            }
        });

        enterNameDialog.show();
    }

    private void showImagePicker() {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        ImagePickerFragment fragment = new ImagePickerFragment();
        transaction.replace(R.id.fragment_container, fragment, "imagePickerFragment").addToBackStack("tag");
        transaction.commit();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Confirm deleting")
                .setMessage("Are you sure you want to delete this image" + (selectedImages.size() == 1 ? "" : "s") + "? This action cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteImages();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void updateImages() {
        adapter.notifyDataSetChanged();
        checkIsEmpty();
    }

    private void reindexImages() {
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setIndex(i);
            Image.insertOrUpdateDB(helper, images.get(i));
        }

        changeThumbnail();
    }

    private void deleteImages() {
        for (int id : selectedImages) {
            int position = getIndexById(id);
            Image image = images.remove(position);
            Image.deleteFromDB(helper, image);
        }

        disableActionMode();
        reindexImages();
        updateImages();
    }

    private void unselectAllImages() {
        for (int id : selectedImages) {
            int position = getIndexById(id);
            images.get(position).setSelected(false);
        }

        disableActionMode();
        updateImages();
    }

    private void changeThumbnail() {
        String uri = "";

        if (images.size() != 0)
            uri = images.get(0).getUri();

        interact(uri);
    }

    private int getIndexById(int id) {
        for (int i = 0; i < images.size(); i++) {
            if (images.get(i).getId() == id)
                return i;
        }

        return -1;
    }

    private void checkIsEmpty() {
        if (images.size() == 0)
            noImagesText.setVisibility(View.VISIBLE);
        else
            noImagesText.setVisibility(View.GONE);
    }

    private void closeFragment() {
        activity.getSupportFragmentManager().popBackStack();
        activity.getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    private void interact(String uri) {
        if (listener != null)
            listener.onAlbumFragmentInteraction(albumId, uri);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("albumsListFragment");

        if (fragment instanceof OnFragmentInteractionListener)
            listener = (OnFragmentInteractionListener) fragment;
        else
            throw new RuntimeException(fragment.toString() + "must implement OnFragmentInteractionListener");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        listener = null;
        closeFragment();
    }

    public interface OnFragmentInteractionListener {
        void onAlbumFragmentInteraction(int albumId, String thumbUri);
    }
}