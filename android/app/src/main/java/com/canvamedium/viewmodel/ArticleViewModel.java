package com.canvamedium.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.canvamedium.db.entity.ArticleEntity;
import com.canvamedium.repository.ArticleRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * ViewModel for Article data that survives configuration changes.
 */
public class ArticleViewModel extends AndroidViewModel {

    private final ArticleRepository articleRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    /**
     * Constructor for ArticleViewModel
     *
     * @param application the application context
     */
    public ArticleViewModel(@NonNull Application application) {
        super(application);
        articleRepository = new ArticleRepository(application);
    }

    /**
     * Get all articles
     *
     * @return LiveData emitting the list of all articles
     */
    public LiveData<List<ArticleEntity>> getAllArticles() {
        return articleRepository.getAllArticles();
    }

    /**
     * Get bookmarked articles
     *
     * @return LiveData emitting the list of bookmarked articles
     */
    public LiveData<List<ArticleEntity>> getBookmarkedArticles() {
        return articleRepository.getBookmarkedArticles();
    }

    /**
     * Get articles by category
     *
     * @param categoryId the category ID
     * @return LiveData emitting the list of articles in the category
     */
    public LiveData<List<ArticleEntity>> getArticlesByCategory(long categoryId) {
        return articleRepository.getArticlesByCategory(categoryId);
    }

    /**
     * Search articles by query
     *
     * @param query the search query
     * @return LiveData emitting the list of articles matching the query
     */
    public LiveData<List<ArticleEntity>> searchArticles(String query) {
        return articleRepository.searchArticles(query);
    }

    /**
     * Toggle bookmark status of an article
     *
     * @param articleId the ID of the article
     * @param isBookmarked whether to bookmark or unbookmark the article
     */
    public void toggleBookmark(long articleId, boolean isBookmarked) {
        disposables.add(
            articleRepository.toggleBookmark(articleId, isBookmarked)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    () -> {
                        // Successfully bookmarked/unbookmarked
                    },
                    throwable -> {
                        errorMessage.setValue("Error updating bookmark: " + throwable.getMessage());
                    }
                )
        );
    }

    /**
     * Refresh articles from the server
     */
    public void refreshArticles() {
        // The repository will handle refreshing when getAllArticles is called
        // This method can be used to force a refresh
        isLoading.setValue(true);
        
        // Can add additional refresh logic here if needed in the future
        
        isLoading.setValue(false);
    }

    /**
     * Check if data is currently loading
     *
     * @return LiveData emitting loading state
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Get error message
     *
     * @return LiveData emitting error messages
     */
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Clean up resources when ViewModel is no longer used
     */
    @Override
    protected void onCleared() {
        super.onCleared();
        disposables.clear();
    }
} 