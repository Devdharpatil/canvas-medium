package com.canvamedium.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.canvamedium.db.dao.ArticleDao;
import com.canvamedium.db.dao.CategoryDao;
import com.canvamedium.db.dao.TagDao;
import com.canvamedium.db.entity.ArticleEntity;
import com.canvamedium.db.entity.CategoryEntity;
import com.canvamedium.db.entity.TagEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room database for the CanvaMedium app.
 * This is the main access point for the underlying SQLite database.
 */
@Database(entities = {ArticleEntity.class, CategoryEntity.class, TagEntity.class}, 
          version = 1, exportSchema = true)
public abstract class CanvaMediumDatabase extends RoomDatabase {

    /**
     * Singleton instance of the database
     */
    private static volatile CanvaMediumDatabase INSTANCE;
    
    /**
     * The number of threads in the executor service
     */
    private static final int NUMBER_OF_THREADS = 4;
    
    /**
     * ExecutorService for running database operations asynchronously
     */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Get an instance of the ArticleDao
     * 
     * @return the ArticleDao
     */
    public abstract ArticleDao articleDao();

    /**
     * Get an instance of the CategoryDao
     * 
     * @return the CategoryDao
     */
    public abstract CategoryDao categoryDao();

    /**
     * Get an instance of the TagDao
     * 
     * @return the TagDao
     */
    public abstract TagDao tagDao();

    /**
     * Get the singleton instance of the database.
     * If the instance does not exist, it creates a new one.
     * 
     * @param context the application context
     * @return the singleton database instance
     */
    public static CanvaMediumDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (CanvaMediumDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    CanvaMediumDatabase.class, "canvamedium_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback for database creation events
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // If you want to populate the database with initial data when first created,
            // you can do it here using the databaseWriteExecutor

            databaseWriteExecutor.execute(() -> {
                // No initial data needed for now
            });
        }
        
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
            // You can perform actions when the database is opened
        }
    };
} 