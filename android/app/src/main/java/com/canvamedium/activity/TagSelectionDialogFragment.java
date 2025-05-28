package com.canvamedium.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.canvamedium.R;
import com.canvamedium.adapter.TagAdapter;
import com.canvamedium.api.ApiResponse;
import com.canvamedium.api.RetrofitClient;
import com.canvamedium.api.TagService;
import com.canvamedium.model.Tag;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dialog fragment for selecting tags.
 */
public class TagSelectionDialogFragment extends AppCompatDialogFragment {

    private TagAdapter tagAdapter;
    private List<Tag> initialSelectedTags;
    private OnTagSelectionListener listener;

    /**
     * Interface to handle tag selection events.
     */
    public interface OnTagSelectionListener {
        void onTagsSelected(List<Tag> selectedTags);
    }

    /**
     * Creates a new instance of the dialog with initial selected tags.
     *
     * @param selectedTags The initially selected tags
     * @return A new instance of TagSelectionDialogFragment
     */
    public static TagSelectionDialogFragment newInstance(List<Tag> selectedTags) {
        TagSelectionDialogFragment fragment = new TagSelectionDialogFragment();
        fragment.initialSelectedTags = selectedTags != null ? selectedTags : new ArrayList<>();
        return fragment;
    }

    /**
     * Sets the listener for tag selection events.
     *
     * @param listener The listener
     */
    public void setOnTagSelectionListener(OnTagSelectionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_tag_selection, null);
        
        RecyclerView recyclerView = view.findViewById(R.id.recycler_tags);
        EditText searchInput = view.findViewById(R.id.edit_search_tag);
        Button searchButton = view.findViewById(R.id.button_search);
        Button newTagButton = view.findViewById(R.id.button_new_tag);
        
        tagAdapter = new TagAdapter(requireContext(), (tag, isSelected) -> {
            // Tag click handled internally by the adapter's selection mode
        });
        
        tagAdapter.setSelectionMode(true);
        if (initialSelectedTags != null) {
            tagAdapter.setSelectedTags(initialSelectedTags);
        }
        
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(tagAdapter);
        
        // Load popular tags initially
        loadPopularTags();
        
        // Set up search
        searchButton.setOnClickListener(v -> {
            String query = searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchTags(query);
            } else {
                loadPopularTags();
            }
        });
        
        // Set up new tag button
        newTagButton.setOnClickListener(v -> showCreateTagDialog());
        
        builder.setView(view)
               .setTitle("Select Tags")
               .setPositiveButton("Apply", (dialog, which) -> {
                   if (listener != null) {
                       listener.onTagsSelected(tagAdapter.getSelectedTags());
                   }
               })
               .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        
        return builder.create();
    }

    /**
     * Loads popular tags from the API.
     */
    private void loadPopularTags() {
        TagService tagService = RetrofitClient.createService(TagService.class);
        tagService.getPopularTags(20).enqueue(new Callback<List<Tag>>() {
            @Override
            public void onResponse(@NonNull Call<List<Tag>> call, @NonNull Response<List<Tag>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tagAdapter.setTags(response.body());
                } else {
                    Toast.makeText(requireContext(), "Failed to load tags", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Tag>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Searches for tags matching the query.
     *
     * @param query The search query
     */
    private void searchTags(String query) {
        TagService tagService = RetrofitClient.createService(TagService.class);
        tagService.searchTags(query, 0, 50).enqueue(new Callback<ApiResponse<List<Tag>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<Tag>>> call, @NonNull Response<ApiResponse<List<Tag>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tagAdapter.setTags(response.body().getContent());
                } else {
                    Toast.makeText(requireContext(), "Failed to search tags", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<Tag>>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Shows a dialog to create a new tag.
     */
    private void showCreateTagDialog() {
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_tag, null);
        EditText tagNameInput = view.findViewById(R.id.edit_tag_name);
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Create New Tag")
            .setView(view)
            .setPositiveButton("Create", (dialog, which) -> {
                String tagName = tagNameInput.getText().toString().trim();
                if (!tagName.isEmpty()) {
                    createTag(tagName);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * Creates a new tag with the given name.
     *
     * @param tagName The tag name
     */
    private void createTag(String tagName) {
        Tag newTag = new Tag(tagName, tagName.toLowerCase().replace(' ', '-'));
        
        TagService tagService = RetrofitClient.createService(TagService.class);
        tagService.createTag(newTag).enqueue(new Callback<Tag>() {
            @Override
            public void onResponse(@NonNull Call<Tag> call, @NonNull Response<Tag> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Tag createdTag = response.body();
                    List<Tag> currentTags = new ArrayList<>(tagAdapter.getSelectedTags());
                    currentTags.add(createdTag);
                    tagAdapter.setSelectedTags(currentTags);
                    Toast.makeText(requireContext(), "Tag created", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Failed to create tag", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Tag> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 