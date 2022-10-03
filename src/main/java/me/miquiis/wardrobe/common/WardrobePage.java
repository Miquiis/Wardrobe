package me.miquiis.wardrobe.common;

import me.miquiis.skinchangerapi.common.SkinLocation;

import java.util.List;

public class WardrobePage {

    public enum PageSort {
        ALPHABETIC,
        LAST_UPDATED,
        SLIM
    }

    private String searchBar;
    private PageSort pageSorted;
    private boolean isAscending;
    private List<SkinLocation> contents;
    private WardrobeTab wardrobeTab;
    private int page;

    public WardrobePage(String searchBar, PageSort pageSorted, boolean isAscending, List<SkinLocation> contents, WardrobeTab wardrobeTab, int page)
    {
        this.searchBar = searchBar;
        this.pageSorted = pageSorted;
        this.isAscending = isAscending;
        this.contents = contents;
        this.wardrobeTab = wardrobeTab;
        this.page = page;
    }

    public WardrobeTab getWardrobeTab() {
        return wardrobeTab;
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

    public String getSearchBar() {
        return searchBar;
    }
}