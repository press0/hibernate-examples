@GenericGenerator(
    name = "ID_GENERATOR",
    strategy = "enhanced-sequence",
    parameters = {
        @Parameter(
            name = "sequence_name",
            value = "JPWH_SEQUENCE"
        )
})
package org.jpwh.helloworld;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
