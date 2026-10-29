package com.simats.smartnetworkanalyzerfrontend.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PaginatedResponse<T> {
    @SerializedName("items")
    public List<T> items;

    @SerializedName("total")
    public int total;

    @SerializedName("page")
    public int page;

    @SerializedName("page_size")
    public int pageSize;

    public List<T> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }
}
