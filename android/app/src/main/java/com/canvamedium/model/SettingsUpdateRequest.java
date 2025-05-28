package com.canvamedium.model;

/**
 * Model class for settings update requests.
 */
public class SettingsUpdateRequest {
    private boolean notificationsEnabled;
    private boolean emailUpdatesEnabled;
    private String currentPassword;
    private String newPassword;

    /**
     * Default constructor for SettingsUpdateRequest.
     */
    public SettingsUpdateRequest() {
    }

    /**
     * Gets whether notifications are enabled.
     *
     * @return true if notifications are enabled, false otherwise
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets whether notifications are enabled.
     *
     * @param notificationsEnabled true to enable notifications, false to disable
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Gets whether email updates are enabled.
     *
     * @return true if email updates are enabled, false otherwise
     */
    public boolean isEmailUpdatesEnabled() {
        return emailUpdatesEnabled;
    }

    /**
     * Sets whether email updates are enabled.
     *
     * @param emailUpdatesEnabled true to enable email updates, false to disable
     */
    public void setEmailUpdatesEnabled(boolean emailUpdatesEnabled) {
        this.emailUpdatesEnabled = emailUpdatesEnabled;
    }

    /**
     * Gets the current password.
     *
     * @return the current password
     */
    public String getCurrentPassword() {
        return currentPassword;
    }

    /**
     * Sets the current password.
     *
     * @param currentPassword the current password
     */
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    /**
     * Gets the new password.
     *
     * @return the new password
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * Sets the new password.
     *
     * @param newPassword the new password
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 