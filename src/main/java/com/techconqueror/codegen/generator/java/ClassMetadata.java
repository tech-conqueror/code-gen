package com.techconqueror.codegen.generator.java;

import com.squareup.javapoet.AnnotationSpec;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents metadata for a Java class that is used in code generation.
 * This class encapsulates the class name, annotations, fields, and additional
 * configurations like whether a no-args constructor is required.
 */
public class ClassMetadata {

    /**
     * The name of the class.
     */
    private String name;

    /**
     * A list of annotations to be applied to the class.
     */
    private List<AnnotationSpec> annotations;

    /**
     * Indicates whether a no-arguments constructor is needed for the class.
     */
    private Boolean isNoArgsConstructorNeeded;

    /**
     * A list of field metadata that describes the fields in the class.
     */
    private List<? extends FieldMetadata> fields;

    /**
     * Constructs a new instance of {@code ClassMetadata}.
     *
     * @param name                      the name of the class.
     * @param isNoArgsConstructorNeeded whether a no-args constructor is required.
     * @param fields                    a list of {@link FieldMetadata} describing the class fields.
     */
    public ClassMetadata(String name, Boolean isNoArgsConstructorNeeded, List<? extends FieldMetadata> fields) {
        this.name = name;
        this.annotations = new ArrayList<>();
        this.isNoArgsConstructorNeeded = isNoArgsConstructorNeeded;
        this.fields = fields;
    }

    /**
     * Gets the name of the class.
     *
     * @return the class name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the class.
     *
     * @param name the new class name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the list of annotations to be applied to the class.
     *
     * @return a list of {@link AnnotationSpec}.
     */
    public List<AnnotationSpec> getAnnotations() {
        return annotations;
    }

    /**
     * Sets the list of annotations to be applied to the class.
     *
     * @param annotations a list of {@link AnnotationSpec}.
     */
    public void setAnnotations(List<AnnotationSpec> annotations) {
        this.annotations = annotations;
    }

    /**
     * Checks if a no-arguments constructor is required for the class.
     *
     * @return {@code true} if a no-args constructor is needed; {@code false} otherwise.
     */
    public Boolean getIsNoArgsConstructorNeeded() {
        return isNoArgsConstructorNeeded;
    }

    /**
     * Specifies whether a no-arguments constructor is required for the class.
     *
     * @param isNoArgsConstructorNeeded {@code true} if a no-args constructor is needed; {@code false} otherwise.
     */
    public void setIsNoArgsConstructorNeeded(Boolean isNoArgsConstructorNeeded) {
        this.isNoArgsConstructorNeeded = isNoArgsConstructorNeeded;
    }

    /**
     * Gets the metadata for the fields in the class.
     *
     * @return a list of {@link FieldMetadata}.
     */
    public List<? extends FieldMetadata> getFields() {
        return fields;
    }

    /**
     * Sets the metadata for the fields in the class.
     *
     * @param fields a list of {@link FieldMetadata}.
     */
    public void setFields(List<? extends FieldMetadata> fields) {
        this.fields = fields;
    }
}
