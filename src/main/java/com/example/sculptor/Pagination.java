package com.example.sculptor;

import java.util.ArrayList;
import java.util.List;

public class Pagination<T> {

    private List<T> items = new ArrayList<>();

    private Integer page;

    private long size;

    private long totalItems;

    private int totalPages;

    private boolean hasNextPage;

    private boolean hasPreviousPage;

    public Integer getTotalPages() {
        return totalPages;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public List<T> getItems() {
        return items;
    }

    public Integer getPage() {
        return page;
    }

    public long getSize() {
        return size;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public static <T> Builder<T> builder(List<T> items) {
        return new Builder<>(items);
    }

    public static class Builder<T> {
        private final List<T> items;

        private int page;

        private long size;

        private long totalItems;

        private Builder(List<T> items) {
            this.items = items;
        }

        public Builder<T> page(int firstResult) {
            this.page = firstResult;
            return this;
        }

        public Builder<T> size(long size) {
            this.size = size;
            return this;
        }

        public Builder<T> totalItems(long totalItems) {
            this.totalItems = totalItems;
            return this;
        }

        public Pagination<T> build() {
            Pagination<T> pagination = new Pagination<>();
            pagination.items = this.items;
            pagination.page = Math.max(this.page + 1, 1);
            pagination.size = this.size;
            pagination.totalItems = this.totalItems;
            if (pagination.size != 0) {
                pagination.totalPages = (int) Math.ceilDiv(pagination.totalItems, pagination.size);
            }

            pagination.hasNextPage = pagination.page < pagination.totalPages;
            pagination.hasPreviousPage = pagination.page > 1;
            return pagination;
        }
    }
}
