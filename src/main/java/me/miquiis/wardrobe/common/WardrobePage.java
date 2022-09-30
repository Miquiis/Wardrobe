package me.miquiis.wardrobe.common;

import me.miquiis.skinchangerapi.common.SkinLocation;

import java.util.List;

public class WardrobePage {

    public enum PageSort {
        ALPHABETIC,
        LAST_UPDATED,
        SLIM
    }

    private PageSort pageSorted;
    private boolean isAscending;
    private List<SkinLocation> contents;
    private int page;

    public WardrobePage(PageSort pageSorted, boolean isAscending, List<SkinLocation> contents, int page)
    {
        this.pageSorted = pageSorted;
        this.isAscending = isAscending;
        this.contents = contents;
        this.page = page;
    }

    public List<SkinLocation> getContents() {
        return contents;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public PageSort getPageSorted() {
        return pageSorted;
    }

    public int getPage() {
        return page;
    }
}