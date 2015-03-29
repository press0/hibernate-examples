package org.jpwh.model.customsql;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class Image {

    @NotNull
    @Column(nullable = false)
    protected String filename;

    @NotNull
    protected int sizeX;

    @NotNull
    protected int sizeY;

    public Image() {
    }

    public Image(String filename, int sizeX, int sizeY) {
        this.filename = filename;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
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

    // Whenever value-types are managed in collections, overriding equals/hashCode is a good idea!

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        if (sizeX != image.sizeX) return false;
        if (sizeY != image.sizeY) return false;
        if (!filename.equals(image.filename)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + sizeX;
        result = 31 * result + sizeY;
        return result;
    }
    // ...
}
