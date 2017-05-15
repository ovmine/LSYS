package com.example.administrator.lsys_camera.recycler.list;

// 필터 선택 메뉴의 아이템들을 구성하는 클래스

public class ItemListFilter {
    private int image;
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public ItemListFilter(int image, String title) {
        this.title = title;
        this.image = image;
    }
}
