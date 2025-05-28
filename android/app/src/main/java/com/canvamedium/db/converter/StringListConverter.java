package com.canvamedium.db.converter;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Type converter for handling List<String> objects in Room
 */
public class StringListConverter {

    /**
     * Converts a JSON string to a List of Strings
     *
     * @param value the JSON string to convert
     * @return the List of String objects, or an empty list if value is null
     */
    @TypeConverter
    public static List<String> fromString(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    /**
     * Converts a List of Strings to a JSON string
     *
     * @param list the List of Strings to convert
     * @return the JSON string, or null if list is null
     */
    @TypeConverter
    public static String toString(List<String> list) {
        if (list == null) {
            return null;
        }
        Gson gson = new Gson();
        return gson.toJson(list);
    }
} 