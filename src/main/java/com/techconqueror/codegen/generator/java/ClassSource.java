package com.techconqueror.codegen.generator.java;

import static org.springframework.util.StringUtils.capitalize;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.List;
import javax.lang.model.element.Modifier;

/**
 * A utility class for generating Java source code representations of classes using JavaPoet.
 * <p>
 * This class provides methods to construct Java class structures dynamically,
 * including annotations, fields, getter and setter methods, and optional constructors.
 * It leverages the {@code TypeSpec.Builder} from JavaPoet to define and build classes programmatically.
 * </p>
 */
public class ClassSource {

    /**
     * A builder for constructing the class structure.
     */
    private final TypeSpec.Builder CLASS_BUILDER;

    /**
     * Private constructor to initialize the class builder with a specified class name.
     *
     * @param className the name of the class to be created.
     */
    private ClassSource(String className) {
        this.CLASS_BUILDER = TypeSpec.classBuilder(className).addModifiers(Modifier.PUBLIC);
    }

    /**
     * Factory method to create a new {@code ClassSource} instance for the specified class name.
     *
     * @param className the name of the class to be created.
     * @return a new {@code ClassSource} instance.
     */
    public static ClassSource createClass(String className) {
        return new ClassSource(className);
    }

    /**
     * Adds a list of annotations to the class.
     *
     * @param annotations a list of {@code AnnotationSpec} instances to be added to the class.
     * @return the current {@code ClassSource} instance for method chaining.
     */
    public ClassSource addAnnotations(List<AnnotationSpec> annotations) {
        annotations.forEach(CLASS_BUILDER::addAnnotation);
        return this;
    }

    /**
     * Adds a list of fields to the class. For each field, getter and setter methods are also generated.
     *
     * @param fields a list of {@code FieldMetadata} instances representing the fields to add.
     * @return the current {@code ClassSource} instance for method chaining.
     */
    public ClassSource addFields(List<? extends FieldMetadata> fields) {
        fields.forEach(fieldMetadata -> {
            CLASS_BUILDER.addField(createField(fieldMetadata));
            CLASS_BUILDER.addMethods(createGetterSetterMethods(fieldMetadata));
        });
        return this;
    }

    /**
     * Optionally includes a no-argument constructor in the class.
     *
     * @param include {@code true} to include a no-argument constructor; {@code false} otherwise.
     * @return the current {@code ClassSource} instance for method chaining.
     */
    public ClassSource withNoArgConstructor(boolean include) {
        if (include) {
            CLASS_BUILDER.addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .build());
        }
        return this;
    }

    /**
     * Builds and returns the {@code TypeSpec} representation of the class.
     *
     * @return the constructed {@code TypeSpec} instance.
     */
    public TypeSpec build() {
        return CLASS_BUILDER.build();
    }

    /**
     * Creates a field definition for the given field metadata.
     *
     * @param fieldMetadata the metadata for the field to be created.
     * @return a {@code FieldSpec} representing the field.
     */
    private static FieldSpec createField(FieldMetadata fieldMetadata) {
        return FieldSpec.builder(fieldMetadata.getTypeName(), fieldMetadata.getName(), Modifier.PRIVATE)
                .addAnnotations(fieldMetadata.getAnnotations())
                .build();
    }

    /**
     * Creates getter and setter methods for the given field metadata.
     *
     * @param fieldMetadata the metadata of the field for which methods are generated.
     * @return an iterable containing the getter and setter {@code MethodSpec} instances.
     */
    private static Iterable<MethodSpec> createGetterSetterMethods(FieldMetadata fieldMetadata) {
        var getter = MethodSpec.methodBuilder("get" + capitalize(fieldMetadata.getName()))
                .addModifiers(Modifier.PUBLIC)
                .returns(fieldMetadata.getTypeName())
                .addStatement("return this.$N", fieldMetadata.getName())
                .build();

        var setter = MethodSpec.methodBuilder("set" + capitalize(fieldMetadata.getName()))
                .addModifiers(Modifier.PUBLIC)
                .addParameter(fieldMetadata.getTypeName(), fieldMetadata.getName())
                .addStatement("this.$N = $N", fieldMetadata.getName(), fieldMetadata.getName())
                .build();

        return List.of(getter, setter);
    }
}
