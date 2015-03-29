package org.jpwh.model.collections.mapofembeddables;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class Image {

    @Column(nullable = true) // Can be null, not part of PK!
    protected String title;

    @NotNull
    protected int sizeX;

    @NotNull
    protected int sizeY;

    public Image() {
    }

    public Image(String title, int sizeX, int sizeY) {
        this.title = title;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    // Whenever value-types are managed in collections, overriding equals/hashCode is a good idea!

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image other = (Image) o;

        if (sizeX != other.sizeX) return false;
        if (sizeY != other.sizeY) return false;
        if (!title.equals(other.title)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + sizeX;
        result = 31 * result + sizeY;
        return result;
    }

    // ...
}
