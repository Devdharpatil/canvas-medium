package com.canvamedium.db.converter;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Type converter for handling Date objects in Room
 */
public class DateConverter {

    /**
     * Converts a timestamp to a Date object
     *
     * @param timestamp the timestamp to convert
     * @return the Date object, or null if timestamp is null
     */
    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    /**
     * Converts a Date object to a timestamp
     *
     * @param date the Date object to convert
     * @return the timestamp, or null if date is null
     */
    @TypeConverter
    public static Long fromDate(Date date) {
        return date == null ? null : date.getTime();
    }
} 