package com.fluidtouch.noteshelf.clipart.pixabay.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Sreenu on 26/04/19
 */
public class ClipartResponse {
    @SerializedName("totalHits")
    @Expose
    private Integer totalHits;
    @SerializedName("hits")
    @Expose
    private List<Clipart> hits = null;
    @SerializedName("total")
    @Expose
    private Integer total;

    public Integer getTotalHits() {
        return totalHits;
    }

    public void setTotalHits(Integer totalHits) {
        this.totalHits = totalHits;
    }

    public List<Clipart> getCliparts() {
        return hits;
    }

    public void setCliparts(List<Clipart> hits) {
        this.hits = hits;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}
