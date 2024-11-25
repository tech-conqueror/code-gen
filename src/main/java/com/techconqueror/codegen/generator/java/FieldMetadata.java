package com.techconqueror.codegen.generator.java;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.TypeName;
import java.util.List;

/**
 * Represents metadata for a field in a Java class used in code generation.
 * This class contains the field's name, type, and associated annotations.
 */
public class FieldMetadata {

    /**
     * The name of the field.
     */
    private String name;

    /**
     * The type name of the field.
     */
    private TypeName typeName;

    /**
     * A list of annotations associated with the field.
     */
    private List<AnnotationSpec> annotations;

    /**
     * Constructs a new {@code FieldMetadata} instance.
     *
     * @param name        the name of the field.
     * @param typeName    the type name of the field.
     * @param annotations a list of {@link AnnotationSpec} representing annotations for the field.
     */
    public FieldMetadata(String name, TypeName typeName, List<AnnotationSpec> annotations) {
        this.name = name;
        this.typeName = typeName;
        this.annotations = annotations;
    }

    /**
     * Gets the name of the field.
     *
     * @return the field name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the field.
     *
     * @param name the new field name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the type name of the field.
     *
     * @return the field type name.
     */
    public TypeName getTypeName() {
        return typeName;
    }

    /**
     * Sets the type name of the field.
     *
     * @param typeName the new field type name.
     */
    public void setTypeName(TypeName typeName) {
        this.typeName = typeName;
    }

    /**
     * Gets the annotations associated with the field.
     *
     * @return a list of {@link AnnotationSpec}.
     */
    public List<AnnotationSpec> getAnnotations() {
        return annotations;
    }

    /**
     * Sets the annotations for the field.
     *
     * @param annotations a list of {@link AnnotationSpec}.
     */
    public void setAnnotations(List<AnnotationSpec> annotations) {
        this.annotations = annotations;
    }
}
