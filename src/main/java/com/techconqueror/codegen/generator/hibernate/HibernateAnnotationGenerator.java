package com.techconqueror.codegen.generator.hibernate;

import com.squareup.javapoet.AnnotationSpec;
import jakarta.persistence.*;

/**
 * A utility class for generating Hibernate-specific annotations using the JavaPoet library.
 * This class provides methods to create common JPA annotations such as @Entity, @Table, @Id, @GeneratedValue
 * and @Column, allowing programmatic generation of Hibernate mapping metadata.
 */
public class HibernateAnnotationGenerator {

    /**
     * Generates the {@code @Entity} annotation.
     *
     * <p>The {@code @Entity} annotation is used to mark a class as a JPA entity,
     * making it eligible for persistence by a JPA provider.</p>
     *
     * @return an {@link AnnotationSpec} representing the {@code @Entity} annotation.
     */
    public static AnnotationSpec createEntityAnnotation() {
        return AnnotationSpec.builder(Entity.class).build();
    }

    /**
     * Generates the {@code @Table} annotation with the specified table name.
     *
     * <p>The {@code @Table} annotation is used to specify the database table
     * name that the entity maps to.</p>
     *
     * @param tableName the name of the database table to map the entity to.
     * @return an {@link AnnotationSpec} representing the {@code @Table} annotation.
     */
    public static AnnotationSpec createTableAnnotation(String tableName) {
        return AnnotationSpec.builder(Table.class)
                .addMember("name", "$S", tableName)
                .build();
    }

    /**
     * Generates the {@code @Id} annotation.
     *
     * <p>The {@code @Id} annotation is used to mark a field as the primary key
     * of the entity.</p>
     *
     * @return an {@link AnnotationSpec} representing the {@code @Id} annotation.
     */
    public static AnnotationSpec createIdAnnotation() {
        return AnnotationSpec.builder(Id.class).build();
    }

    /**
     * Generates the {@code @GeneratedValue} annotation.
     *
     * <p>The {@code @GeneratedValue} annotation specifies how the primary key value
     * is generated. This method sets the strategy to {@code GenerationType.IDENTITY},
     * indicating that the database will generate the primary key value.</p>
     *
     * @return an {@link AnnotationSpec} representing the {@code @GeneratedValue} annotation.
     */
    public static AnnotationSpec createGeneratedValueAnnotation() {
        return AnnotationSpec.builder(GeneratedValue.class)
                .addMember("strategy", "$T.IDENTITY", GenerationType.class)
                .build();
    }

    /**
     * Generates the {@code @Column} annotation with optional nullable and length constraints.
     *
     * <p>The {@code @Column} annotation is used to specify the mapping between a field
     * and a column in the database. This method allows setting whether the column can
     * accept {@code null} values and optionally defining the column's length.</p>
     *
     * @param name the name of the column.
     * @param isNullable whether the column is nullable.
     * @param length     the length of the column, or {@code null} if not applicable.
     * @return an {@link AnnotationSpec} representing the {@code @Column} annotation.
     */
    public static AnnotationSpec createColumnAnnotation(String name, boolean isNullable, Integer length) {
        var builder = AnnotationSpec.builder(Column.class)
                .addMember("name", "$S", name)
                .addMember("nullable", "$L", isNullable);

        if (length != null) {
            builder.addMember("length", "$L", length);
        }

        return builder.build();
    }
}
