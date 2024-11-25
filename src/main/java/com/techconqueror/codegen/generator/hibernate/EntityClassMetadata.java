package com.techconqueror.codegen.generator.hibernate;

import com.techconqueror.codegen.generator.java.ClassMetadata;
import com.techconqueror.codegen.generator.java.FieldMetadata;
import java.util.List;

/**
 * Represents metadata for an entity class, extending the {@link ClassMetadata} class to include
 * additional Hibernate-specific information such as the table name.
 * <p>
 * This class is designed to store and manage information required for generating Hibernate entity classes,
 * including the name, fields, whether a no-argument constructor is needed, and the corresponding table name.
 * </p>
 */
public class EntityClassMetadata extends ClassMetadata {

    /**
     * The name of the database table associated with this entity.
     */
    private String tableName;

    /**
     * Constructs a new {@code EntityClassMetadata} instance with the specified parameters.
     *
     * @param name      the name of the entity class.
     * @param fields    a list of {@link FieldMetadata} objects representing the fields of the entity.
     * @param tableName the name of the database table associated with this entity.
     */
    public EntityClassMetadata(String name, List<? extends FieldMetadata> fields, String tableName) {
        super(name, true, fields);
        this.tableName = tableName;
    }

    /**
     * Returns the name of the database table associated with this entity.
     *
     * @return the table name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the name of the database table associated with this entity.
     *
     * @param tableName the table name to set.
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
