package by.kanber.pholter.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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

import java.util.ArrayList;

import by.kanber.pholter.adapter.AlbumAdapter;
import by.kanber.pholter.database.DBHelper;
import by.kanber.pholter.util.ItemTouchHelperCallback;
import by.kanber.pholter.activity.MainActivity;
import by.kanber.pholter.R;
import by.kanber.pholter.util.Utils;
import by.kanber.pholter.model.Album;
import by.kanber.pholter.model.Image;


public class AlbumsListFragment extends Fragment implements AlbumFragment.OnFragmentInteractionListener {
    public static final String TAG = "CustomTag";
    public static final int TYPE_ADD = 0;
    public static final int TYPE_EDIT = 1;

    private RecyclerView recyclerView;
    private TextView noAlbumsTextView;
    private Toolbar toolbar;
    private AlbumAdapter adapter;
    private DBHelper helper;
    private MainActivity activity;
    private ItemTouchHelper touchHelper;

    private ArrayList<Album> albums;
    private ArrayList<Integer> selectedAlbums;
    private boolean isLandscape;

    public AlbumsListFragment() {}

    public static AlbumsListFragment newInstance(boolean orientation) {
        Bundle args = new Bundle();
        args.putBoolean("orientation", orientation);
        AlbumsListFragment fragment = new AlbumsListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            isLandscape = getArguments().getBoolean("orientation");

        activity = (MainActivity) getActivity();

        if (activity != null) {
            helper = activity.getHelper();
            selectedAlbums = new ArrayList<>();
            albums = Album.getAlbums(helper);
        }

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_albums_list, container, false);

        if (activity != null) {
            noAlbumsTextView = view.findViewById(R.id.no_albums_text);
            recyclerView = view.findViewById(R.id.albums_list);
            toolbar = view.findViewById(R.id.toolbar);
            adapter = new AlbumAdapter(activity, albums);

            activity.setSupportActionBar(toolbar);
            toolbar.setTitle(getString(R.string.app_name));
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(activity, getSpanCount()));
            recyclerView.setAdapter(adapter);

            adapter.setOnItemClickListener(new AlbumAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    int id = albums.get(position).getId();

                    if (!adapter.isEditMode()) {
                        if (adapter.isActionsMode()) {
                            if (!selectedAlbums.contains(id)) {
                                selectedAlbums.add(id);
                                albums.get(position).setSelected(true);
                            } else {
                                selectedAlbums.remove(Integer.valueOf(id));
                                albums.get(position).setSelected(false);
                            }

                            if (selectedAlbums.size() == 0)
                                disableActionMode();
                            else {
                                toolbar.setTitle(String.valueOf(selectedAlbums.size()));
                                activity.invalidateOptionsMenu();
                            }

                            updateAlbums();
                        } else {
                            FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                            AlbumFragment fragment = AlbumFragment.newInstance(id, albums.get(position).getName());
                            transaction.replace(R.id.fragment_container, fragment, "albumFragment").addToBackStack("tag");
                            transaction.commit();
                        }
                    }
                }
            });

            adapter.setOnItemLongClickListener(new AlbumAdapter.OnItemLongClickListener() {
                @Override
                public void onItemLongClick(int position) {
                    if (!adapter.isActionsMode() && !adapter.isEditMode()) {
                        selectedAlbums.add(albums.get(position).getId());
                        albums.get(position).setSelected(true);
                        enableActionMode();
                        updateAlbums();
                    }
                }
            });

            adapter.setOnMoveCompleteListener(new AlbumAdapter.OnMoveCompleteListener() {
                @Override
                public void onMoveComplete() {
                    reindexAlbums();
                }
            });

            ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(adapter);
            touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(recyclerView);

            adapter.setOnStartDragListener(new AlbumAdapter.OnStartDragListener() {
                @Override
                public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                    if (adapter.isEditMode())
                        touchHelper.startDrag(viewHolder);
                }
            });

            checkIsEmpty();

            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        if (adapter.isActionsMode()) {
                            unselectAllAlbums();

                            return true;
                        }

                        if (adapter.isEditMode()) {
                            disableEditMode();

                            return true;
                        }
                    }

                    return false;
                }
            });
        }

        return view;
    }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (adapter.isActionsMode()) {
            inflater.inflate(R.menu.albums_list_action_mode_menu, menu);
            menu.findItem(R.id.albums_list_rename).setVisible(selectedAlbums.size() == 1);
        } else {
            if (adapter.isEditMode())
                inflater.inflate(R.menu.menu_done, menu);
            else {
                inflater.inflate(R.menu.main_menu, menu);
                menu.findItem(R.id.edit_list).setVisible(albums.size() > 1);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_item: showEnterAlbumDialog(TYPE_ADD, ""); return true;
            case R.id.edit_list: enableEditMode(); return true;
            case R.id.menu_done: disableEditMode(); return true;
            case R.id.albums_list_rename: startRenameAlbum(); return true;
            case R.id.albums_list_delete: showDeleteDialog(); return true;
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    public void onAlbumFragmentInteraction(int albumId, String thumbUri) {
        int position = getIndexById(albumId);

        if (position != -1) {
            if (!albums.get(position).getThumbnail().equals(thumbUri)) {
                albums.get(position).setThumbnail(thumbUri);
                Album.insertOrUpdateDB(helper, albums.get(position));
                updateAlbums();
            }
        }
    }

    private void showEnterAlbumDialog(final int type, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View enterAlbumNameView = activity.getLayoutInflater().inflate(R.layout.edit_text_dialog, null);
        final EditText enterAlbumNameEditText = enterAlbumNameView.findViewById(R.id.album_name_edit);
        enterAlbumNameEditText.setHint("Enter name");
        enterAlbumNameEditText.setMaxLines(1);
        enterAlbumNameEditText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
        enterAlbumNameEditText.append(name);
        String btnText = getButtonText(type);

        builder.setView(enterAlbumNameView)
                .setTitle(btnText + " album")
                .setNegativeButton("Cancel", null)
                .setPositiveButton(btnText, null);

        final AlertDialog enterNameDialog = builder.create();

        enterNameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) dialogInterface).getButton(DialogInterface.BUTTON_POSITIVE);
                enterAlbumNameEditText.requestFocus();

                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = enterAlbumNameEditText.getText().toString();

                        if (!Utils.isEmpty(name)) {
                            switchActions(type, name.trim());
                            enterNameDialog.cancel();
                        } else {
                            enterAlbumNameEditText.setText("");
                        }
                    }
                });

                activity.openKeyboard();
            }
        });

        enterNameDialog.show();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle("Confirm deleting")
                .setMessage("Are you sure you want to delete this album" + (selectedAlbums.size() == 1 ? "" : "s") + "? This action cannot be undone.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteAlbums();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
    }

    private void addAlbum(String name) {
        Album.insertOrUpdateDB(helper, new Album(albums.size(), name));
        Album album = Album.getLastAlbum(helper);
        albums.add(album);
        activity.invalidateOptionsMenu();
        updateAlbums();
    }

    private void startRenameAlbum() {
        int position = getIndexById(selectedAlbums.get(0));

        if (position != -1) {
            String name = albums.get(position).getName();
            showEnterAlbumDialog(TYPE_EDIT, name);
        }
    }

    private void renameAlbum(String name) {
        int position = getIndexById(selectedAlbums.get(0));

        if (position != -1) {
            albums.get(position).setName(name);
            albums.get(position).setSelected(false);
            Album.insertOrUpdateDB(helper, albums.get(position));
            disableActionMode();
            updateAlbums();
        }
    }

    private void reindexAlbums() {
        for (int i = 0; i < albums.size(); i++) {
            albums.get(i).setIndex(i);
            Album.insertOrUpdateDB(helper, albums.get(i));
        }
    }

    private void switchActions(int type, String name) {
        switch (type) {
            case TYPE_ADD: addAlbum(name); break;
            case TYPE_EDIT: renameAlbum(name); break;
        }
    }

    private String getButtonText(int type) {
        switch (type) {
            case TYPE_ADD: return "Add";
            case TYPE_EDIT: return "Rename";
        }

        return "";
    }

    private void deleteAlbums() {
        for (int id : selectedAlbums) {
            int position = getIndexById(id);

            if (position != -1) {
                Album album = albums.remove(position);
                deleteAssociatedImages(id);
                Album.deleteFromDB(helper, album);
            }
        }

        reindexAlbums();
        disableActionMode();
        updateAlbums();
    }

    private void deleteAssociatedImages(int id) {
        ArrayList<Image> images = Image.getImages(helper, id);

        for (Image image : images) {
            Image.deleteFromDB(helper, image);
        }
    }

    private void unselectAllAlbums() {
        for (int id : selectedAlbums) {
            int position = getIndexById(id);

            if (position != -1)
                albums.get(position).setSelected(false);
        }

        disableActionMode();
        updateAlbums();
    }

    private int getIndexById(int id) {
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getId() == id)
                return i;
        }

        return -1;
    }

    private void updateAlbums() {
        adapter.notifyDataSetChanged();
        checkIsEmpty();
    }

    private void enableEditMode() {
        adapter.setEditMode(true);
        toolbar.setTitle("Edit");
        activity.invalidateOptionsMenu();
    }

    private void disableEditMode() {
        adapter.setEditMode(false);
        toolbar.setTitle(getString(R.string.app_name));
        activity.invalidateOptionsMenu();
    }

    private void enableActionMode() {
        adapter.setActionsMode(true);
        toolbar.setTitle(String.valueOf(selectedAlbums.size()));
        toolbar.setNavigationIcon(R.drawable.ic_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unselectAllAlbums();
            }
        });
        Utils.changeStatusBarColor(activity, R.color.colorPrimaryAction);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimaryAction));
        activity.invalidateOptionsMenu();
    }

    private void disableActionMode() {
        adapter.setActionsMode(false);
        toolbar.setTitle(getString(R.string.app_name));
        Utils.changeStatusBarColor(activity, R.color.colorPrimary);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        toolbar.setNavigationIcon(null);
        toolbar.setNavigationOnClickListener(null);
        selectedAlbums.clear();
        activity.invalidateOptionsMenu();
    }

    private void checkIsEmpty() {
        if (albums.size() == 0)
            noAlbumsTextView.setVisibility(View.VISIBLE);
        else
            noAlbumsTextView.setVisibility(View.GONE);
    }

    private int getSpanCount() {
        return isLandscape ? 5 : 3;
    }
}