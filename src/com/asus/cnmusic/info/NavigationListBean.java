package com.asus.cnmusic.info;

/**
 * Created by Jun on 2015/5/16.
 */
public class NavigationListBean {
    private String title;
    private int titleImageId;

    public NavigationListBean(String title, int titleImageId) {
        this.title = title;
        this.titleImageId = titleImageId;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitleImageId(int titleImageId) {
        this.titleImageId = titleImageId;
    }
    public int getTitleImageId() {
        return titleImageId;
    }
}
