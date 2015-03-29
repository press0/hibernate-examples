package org.jpwh.model.advanced;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@org.hibernate.annotations.Immutable
@org.hibernate.annotations.Subselect(
    value = "select i.ID as ITEMID, i.ITEM_NAME as NAME, " +
            "count(b.ID) as NUMBEROFBIDS " +
            "from ITEM i left outer join BID b on i.ID = b.ITEM_ID " +
            "group by i.ID, i.ITEM_NAME"
)
@org.hibernate.annotations.Synchronize({"Item", "Bid"}) // Case-sensitive!
// TODO: https://hibernate.atlassian.net/browse/HHH-8430
public class ItemBidSummary {

    @Id
    protected Long itemId;

    protected String name;

    protected long numberOfBids;

    public ItemBidSummary() {
    }

    // Getter methods...
    public Long getItemId() {
        return itemId;
    }

    public String getName() {
        return name;
    }

    public long getNumberOfBids() {
        return numberOfBids;
    }

    // ...
}