package org.jpwh.test.simple;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CRUDMetadataOverrideXML extends CRUD {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SimpleXMLOverridePU");
    }

    @Test
    @Override
    public void storeAndQueryItems() throws Exception {
        super.storeAndQueryItems();
    }

    @Test
    public void checkMetadataOverride() throws Exception {
        // Use the Hibernate metadata API to find the overriden SQL column name, JPA doesn't
        // support access to the SQL details
        // TODO: No idea how to access SQL mapping details with Hibernate 4.3 metamodel API...
        /*
        Property nameProperty = itemClass.getpgetProperty("name");
        Column nameColumn = (Column) nameProperty.getColumnIterator().next();

        // The name of the column is from the XML descriptor
        assertEquals(nameColumn.getName(), "ITEM_NAME");

        // But Bean Validation annotations are still recognized!
        assertEquals(nameColumn.isNullable(), false);
        assertEquals(nameColumn.getLength(), 255);
        */
    }
}
