package by.kanber.pholter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

import by.kanber.pholter.models.GalleryFolder;
import by.kanber.pholter.adapters.GalleryFolderAdapter;
import by.kanber.pholter.adapters.GalleryImageAdapter;
import by.kanber.pholter.MainActivity;
import by.kanber.pholter.R;
import by.kanber.pholter.util.Utils;
import by.kanber.pholter.models.GalleryImage;

public class ImagePickerFragment extends Fragment {
    private OnFragmentInteractionListener mListener;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private Button addButton;
    private MainActivity activity;
    private GalleryFolderAdapter foldersAdapter;
    private GalleryImageAdapter imagesAdapter;

    private ArrayList<GalleryFolder> folders;
    private ArrayList<String> selectedImages;
    private boolean foldersMode = true;

    public ImagePickerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getContext();

        if (context != null)
            folders = Utils.getGallery(context);
        else
            folders = new ArrayList<>();

        selectedImages = new ArrayList<>();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_picker, container, false);
        activity = (MainActivity) getActivity();

        if (activity != null) {
            toolbar = view.findViewById(R.id.toolbar);
            recyclerView = view.findViewById(R.id.folders_list);
            addButton = view.findViewById(R.id.add_button);

            activity.setSupportActionBar(toolbar);
            toolbar.setTitle("Folders");
            toolbar.setNavigationIcon(R.drawable.ic_back);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!foldersMode) {
                        foldersMode = true;
                        recyclerView.setAdapter(foldersAdapter);
                    } else {
                        closeFragment();
                    }
                }
            });

            addButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedImages.size() > 0) {
                        onButtonPressed();
                        closeFragment();
                    }
                }
            });

            foldersAdapter = new GalleryFolderAdapter(activity, folders);
            foldersAdapter.setOnItemClickListener(new GalleryFolderAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    final ArrayList<GalleryImage> images = folders.get(position).getImages();
                    foldersMode = false;
                    imagesAdapter = new GalleryImageAdapter(activity, images);

                    imagesAdapter.setOnItemClickListener(new GalleryImageAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(int position) {
                            GalleryImage image = images.get(position);

                            if (!selectedImages.contains(image.getUri())) {
                                image.setSelected(true);
                                selectedImages.add(image.getUri());
                            } else {
                                image.setSelected(false);
                                selectedImages.remove(image.getUri());
                            }

                            updateScreen();
                        }
                    });

                    recyclerView.setAdapter(imagesAdapter);
                }
            });

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(activity, 3));
            recyclerView.setAdapter(foldersAdapter);

            view.setFocusableInTouchMode(true);
            view.requestFocus();
            view.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {
                    if (i == KeyEvent.KEYCODE_BACK && keyEvent.getAction() == KeyEvent.ACTION_DOWN)
                        if (!foldersMode) {
                            foldersMode = true;
                            recyclerView.setAdapter(foldersAdapter);
                            return true;
                        }

                    return false;
                }
            });
        }

        return view;
    }

    private void updateScreen() {
        String btnText = "Add ";

        if (selectedImages.size() == 0)
            btnText = "Nothing selected";

        if (selectedImages.size() == 1)
            btnText += "1 image";

        if (selectedImages.size() > 1)
            btnText += String.valueOf(selectedImages.size() + " images");

        addButton.setText(btnText);
        imagesAdapter.notifyDataSetChanged();
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onImagePickerFragmentInteraction(selectedImages);
        }
    }

    private void closeFragment() {
        activity.getSupportFragmentManager().popBackStack();
        activity.getSupportFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Fragment fragment = ((MainActivity) context).getSupportFragmentManager().findFragmentByTag("albumFragment");

        if (fragment instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) fragment;
        } else {
            throw new RuntimeException(fragment.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onImagePickerFragmentInteraction(ArrayList<String> images);
    }
}
