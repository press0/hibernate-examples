package org.jpwh.model.collections.setofembeddablesorderby;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Image {

    @Column(nullable = false)
    protected String title;

    @Column(nullable = false)
    protected String filename;

    protected int sizeX;

    protected int sizeY;

    @org.hibernate.annotations.Parent
    protected Item item;

    public Image() {
    }

    public Image(String title, String filename, int sizeX, int sizeY) {
        this.title = title;
        this.filename = filename;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getSizeX() {
        return sizeX;
    }

    public void setSizeX(int sizeX) {
        this.sizeX = sizeX;
    }

    public int getSizeY() {
        return sizeY;
    }

    public void setSizeY(int sizeY) {
        this.sizeY = sizeY;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    // Whenever value-types are managed in collections, overriding equals/hashCode is a good idea!

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image other = (Image) o;

        if (!title.equals(other.title)) return false;
        if (!filename.equals(other.filename)) return false;
        if (sizeX != other.sizeX) return false;
        if (sizeY != other.sizeY) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + filename.hashCode();
        result = 31 * result + sizeX;
        result = 31 * result + sizeY;
        return result;
    }

    // ...
}
