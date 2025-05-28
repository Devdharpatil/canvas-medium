package com.canvamedium.api;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Generic wrapper class for paginated API responses.
 *
 * @param <T> The type of content in the response
 */
public class ApiResponse<T> {

    @SerializedName("content")
    private T content;

    @SerializedName("pageable")
    private PageableInfo pageable;

    @SerializedName("totalElements")
    private int totalElements;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("last")
    private boolean last;

    @SerializedName("first")
    private boolean first;

    @SerializedName("empty")
    private boolean empty;

    @SerializedName("number")
    private int number;

    @SerializedName("size")
    private int size;

    /**
     * Gets the content of the response.
     *
     * @return The content
     */
    public T getContent() {
        return content;
    }

    /**
     * Sets the content of the response.
     *
     * @param content The content
     */
    public void setContent(T content) {
        this.content = content;
    }

    /**
     * Gets the pageable information.
     *
     * @return The pageable info
     */
    public PageableInfo getPageable() {
        return pageable;
    }

    /**
     * Sets the pageable information.
     *
     * @param pageable The pageable info
     */
    public void setPageable(PageableInfo pageable) {
        this.pageable = pageable;
    }

    /**
     * Gets the total number of elements.
     *
     * @return The total elements count
     */
    public int getTotalElements() {
        return totalElements;
    }

    /**
     * Sets the total number of elements.
     *
     * @param totalElements The total elements count
     */
    public void setTotalElements(int totalElements) {
        this.totalElements = totalElements;
    }

    /**
     * Gets the total number of pages.
     *
     * @return The total pages count
     */
    public int getTotalPages() {
        return totalPages;
    }

    /**
     * Sets the total number of pages.
     *
     * @param totalPages The total pages count
     */
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    /**
     * Checks if this is the last page.
     *
     * @return True if this is the last page
     */
    public boolean isLast() {
        return last;
    }

    /**
     * Sets whether this is the last page.
     *
     * @param last True if this is the last page
     */
    public void setLast(boolean last) {
        this.last = last;
    }

    /**
     * Checks if this is the first page.
     *
     * @return True if this is the first page
     */
    public boolean isFirst() {
        return first;
    }

    /**
     * Sets whether this is the first page.
     *
     * @param first True if this is the first page
     */
    public void setFirst(boolean first) {
        this.first = first;
    }

    /**
     * Checks if this page is empty.
     *
     * @return True if this page is empty
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * Sets whether this page is empty.
     *
     * @param empty True if this page is empty
     */
    public void setEmpty(boolean empty) {
        this.empty = empty;
    }

    /**
     * Gets the current page number (0-based).
     *
     * @return The page number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Sets the current page number.
     *
     * @param number The page number
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Gets the page size.
     *
     * @return The page size
     */
    public int getSize() {
        return size;
    }

    /**
     * Sets the page size.
     *
     * @param size The page size
     */
    public void setSize(int size) {
        this.size = size;
    }

    /**
     * Helper class to represent pageable information.
     */
    public static class PageableInfo {
        @SerializedName("offset")
        private int offset;

        @SerializedName("pageNumber")
        private int pageNumber;

        @SerializedName("pageSize")
        private int pageSize;

        @SerializedName("paged")
        private boolean paged;

        @SerializedName("unpaged")
        private boolean unpaged;

        /**
         * Gets the offset.
         *
         * @return The offset
         */
        public int getOffset() {
            return offset;
        }

        /**
         * Sets the offset.
         *
         * @param offset The offset
         */
        public void setOffset(int offset) {
            this.offset = offset;
        }

        /**
         * Gets the page number.
         *
         * @return The page number
         */
        public int getPageNumber() {
            return pageNumber;
        }

        /**
         * Sets the page number.
         *
         * @param pageNumber The page number
         */
        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        /**
         * Gets the page size.
         *
         * @return The page size
         */
        public int getPageSize() {
            return pageSize;
        }

        /**
         * Sets the page size.
         *
         * @param pageSize The page size
         */
        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        /**
         * Checks if this is paged.
         *
         * @return True if paged
         */
        public boolean isPaged() {
            return paged;
        }

        /**
         * Sets whether this is paged.
         *
         * @param paged True if paged
         */
        public void setPaged(boolean paged) {
            this.paged = paged;
        }

        /**
         * Checks if this is unpaged.
         *
         * @return True if unpaged
         */
        public boolean isUnpaged() {
            return unpaged;
        }

        /**
         * Sets whether this is unpaged.
         *
         * @param unpaged True if unpaged
         */
        public void setUnpaged(boolean unpaged) {
            this.unpaged = unpaged;
        }
    }
} 