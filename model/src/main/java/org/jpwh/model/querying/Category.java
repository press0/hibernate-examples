package org.jpwh.model.querying;

import org.jpwh.model.Constants;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Category {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    protected String name;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    @org.hibernate.annotations.ForeignKey(name = "FK_CATEGORY_PARENT_ID")
    // The root of the tree has no parent, column has to be nullable!
    protected Category parent;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "CATEGORY_ITEM",
       joinColumns = @JoinColumn(name = "CATEGORY_ID"),
       inverseJoinColumns = @JoinColumn(name = "ITEM_ID"))
    @org.hibernate.annotations.ForeignKey(
        name = "FK_CATEGORY_ITEM_CATEGORY_ID",
        inverseName = "FK_CATEGORY_ITEM_ITEM_ID"
    )
    protected Set<Item> items = new HashSet<Item>();

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public Category(String name, Category parent) {
        this.name = name;
        this.parent = parent;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public Set<Item> getItems() {
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    // ...
}
