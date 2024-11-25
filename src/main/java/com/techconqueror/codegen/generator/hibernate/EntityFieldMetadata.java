package com.techconqueror.codegen.generator.hibernate;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import com.techconqueror.codegen.generator.java.FieldMetadata;
import java.util.List;

/**
 * Represents metadata for a field in a Hibernate entity, extending the {@link FieldMetadata} class
 * to specialize for fields used in Hibernate entities.
 * <p>
 * This class allows the inclusion of field-specific annotations such as {@code @Column}, {@code @Id}, or {@code @GeneratedValue}
 * that are specific to Hibernate and JPA.
 * </p>
 */
public class EntityFieldMetadata extends FieldMetadata {

    /**
     * Constructs a new {@code EntityFieldMetadata} instance with the specified parameters.
     *
     * @param name        the name of the field.
     * @param typeName    the type name of the field.
     * @param annotations a list of {@link AnnotationSpec} objects representing the annotations for the field.
     */
    public EntityFieldMetadata(String name, TypeName typeName, List<AnnotationSpec> annotations) {
        super(name, typeName, annotations);
    }
}
