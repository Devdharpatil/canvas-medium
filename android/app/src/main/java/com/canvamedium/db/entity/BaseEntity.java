package com.canvamedium.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.TypeConverters;

import com.canvamedium.db.converter.DateConverter;

import java.util.Date;

/**
 * Abstract base entity class with common audit fields.
 * This class is designed to be extended by other entity classes
 * to provide consistent timestamp handling.
 */
@TypeConverters(DateConverter.class)
public abstract class BaseEntity {

    @ColumnInfo(name = "created_at")
    private Date createdAt;

    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    @ColumnInfo(name = "is_synced")
    private boolean isSynced;

    @ColumnInfo(name = "last_sync_time")
    private Date lastSyncTime;

    /**
     * Initialize audit fields with default values.
     * This method should be called in entity constructors.
     */
    protected void initAuditFields() {
        Date now = new Date();
        this.createdAt = now;
        this.updatedAt = now;
        this.isSynced = false;
        this.lastSyncTime = null;
    }

    /**
     * Update the audit fields when the entity is modified.
     * This method should be called before saving changes to the database.
     */
    public void updateAuditFields() {
        this.updatedAt = new Date();
        this.isSynced = false;
    }

    /**
     * Mark the entity as synced with the server.
     * This method should be called after successful synchronization.
     */
    public void markSynced() {
        this.isSynced = true;
        this.lastSyncTime = new Date();
    }

    /**
     * Check if the entity is synced with the server.
     *
     * @return true if synced, false otherwise
     */
    public boolean isSynced() {
        return isSynced;
    }

    /**
     * Set the synced status.
     *
     * @param synced the synced status
     */
    public void setSynced(boolean synced) {
        this.isSynced = synced;
    }

    /**
     * Get the creation timestamp.
     *
     * @return the creation timestamp
     */
    public Date getCreatedAt() {
        return createdAt;
    }

    /**
     * Set the creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Get the last update timestamp.
     *
     * @return the last update timestamp
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set the last update timestamp.
     *
     * @param updatedAt the last update timestamp
     */
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get the last synchronization timestamp.
     *
     * @return the last synchronization timestamp
     */
    public Date getLastSyncTime() {
        return lastSyncTime;
    }

    /**
     * Set the last synchronization timestamp.
     *
     * @param lastSyncTime the last synchronization timestamp
     */
    public void setLastSyncTime(Date lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
} 