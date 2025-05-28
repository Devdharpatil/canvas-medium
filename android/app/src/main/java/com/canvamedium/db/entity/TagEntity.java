package com.canvamedium.db.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.canvamedium.db.converter.DateConverter;

import java.util.Date;

/**
 * Entity class for Tag data stored in Room database
 */
@Entity(
    tableName = "tags",
    indices = {
        @Index(value = {"count"}),
        @Index(value = {"created_at"}),
        @Index(value = {"is_synced"})
    }
)
@TypeConverters(DateConverter.class)
public class TagEntity {

    @PrimaryKey
    @NonNull
    private String name;

    @ColumnInfo(name = "count")
    private int count;

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @ColumnInfo(name = "is_synced")
    private boolean isSynced;

    @ColumnInfo(name = "last_sync_time")
    private Date lastSyncTime;

    /**
     * Default constructor for Room
     */
    public TagEntity() {
    }

    /**
     * Constructor with all fields
     */
    public TagEntity(@NonNull String name, int count, Date createdAt, Date updatedAt) {
        this.name = name;
        this.count = count;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isSynced = true;
        this.lastSyncTime = new Date();
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
} 