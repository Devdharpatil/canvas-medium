package com.canvamedium.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.canvamedium.R;
import com.canvamedium.adapter.TemplateAdapter;
import com.canvamedium.api.ApiClient;
import com.canvamedium.api.ApiService;
import com.canvamedium.model.Template;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity for displaying a list of templates and managing them.
 */
public class TemplateListActivity extends AppCompatActivity implements TemplateAdapter.OnTemplateClickListener {

    private static final int REQUEST_CREATE_TEMPLATE = 1001;
    private static final int REQUEST_EDIT_TEMPLATE = 1002;

    private ApiService apiService;
    private RecyclerView recyclerView;
    private TemplateAdapter adapter;
    private ProgressBar loadingIndicator;
    private TextView emptyView;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize views
        recyclerView = findViewById(R.id.template_recycler_view);
        loadingIndicator = findViewById(R.id.loading_indicator);
        emptyView = findViewById(R.id.empty_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        FloatingActionButton fabAddTemplate = findViewById(R.id.fab_add_template);

        // Initialize API service
        apiService = ApiClient.getClient().create(ApiService.class);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new TemplateAdapter(this, this);
        recyclerView.setAdapter(adapter);

        // Setup pull-to-refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadTemplates);

        // Setup FAB
        fabAddTemplate.setOnClickListener(v -> {
            Intent intent = new Intent(TemplateListActivity.this, TemplateBuilderActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_TEMPLATE);
        });

        // Load templates
        loadTemplates();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CREATE_TEMPLATE || requestCode == REQUEST_EDIT_TEMPLATE) &&
                resultCode == RESULT_OK) {
            // Reload templates after creation or edit
            loadTemplates();
        }
    }

    /**
     * Loads templates from the API.
     */
    private void loadTemplates() {
        showLoading(true);
        
        Map<String, Object> options = new HashMap<>();
        options.put("page", 0);
        options.put("size", 100);
        options.put("sortBy", "createdAt");
        options.put("sortDir", "desc");

        apiService.getTemplates(options).enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Map<String, Object> result = response.body();
                    if (result.containsKey("content")) {
                        List<Template> templates = (List<Template>) result.get("content");
                        adapter.setTemplates(templates);
                        
                        showEmptyView(templates.isEmpty());
                    } else {
                        showEmptyView(true);
                    }
                } else {
                    Toast.makeText(TemplateListActivity.this,
                            "Failed to load templates", Toast.LENGTH_SHORT).show();
                    showEmptyView(true);
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                showLoading(false);
                Toast.makeText(TemplateListActivity.this,
                        "Error loading templates: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyView(true);
            }
        });
    }

    /**
     * Shows or hides the loading indicator.
     *
     * @param isLoading Whether data is loading
     */
    private void showLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows or hides the empty view.
     *
     * @param isEmpty Whether the data list is empty
     */
    private void showEmptyView(boolean isEmpty) {
        emptyView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTemplateClick(Template template) {
        Intent intent = new Intent(this, TemplateBuilderActivity.class);
        intent.putExtra("template_id", template.getId());
        startActivityForResult(intent, REQUEST_EDIT_TEMPLATE);
    }
} 