package com.techconqueror.codegen.generator.common;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.nio.file.Path;
import javax.lang.model.element.Modifier;

/**
 * Utility class for generating exception classes.
 * <p>
 * This class provides methods to programmatically generate custom exception classes using the {@code JavaPoet} library.
 * The generated classes are written to the specified output paths and are structured to include essential constructors
 * and a specified package name.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * String outputPath = "/path/to/output";
 * ExceptionGenerator.generateResourceNotFoundException(outputPath);
 * </pre>
 */
public class ExceptionGenerator {

    /**
     * Generates a custom exception class named {@code ResourceNotFoundException} and writes it to the specified output path.
     * The generated class is placed under the package {@code com.techconqueror.codegen.exception} and extends {@code RuntimeException}.
     * <p>
     * The generated class includes:
     * <ul>
     *   <li>A public constructor accepting a {@code String} message.</li>
     *   <li>A public constructor accepting a {@code String} message and a {@code Throwable} cause.</li>
     * </ul>
     * <p>
     * Example of the generated class:
     * <pre>
     * package com.techconqueror.codegen.exception;
     *
     * public class ResourceNotFoundException extends RuntimeException {
     *     public ResourceNotFoundException(String message) {
     *         super(message);
     *     }
     *
     *     public ResourceNotFoundException(String message, Throwable cause) {
     *         super(message, cause);
     *     }
     * }
     * </pre>
     *
     * @param outputPath the output directory where the {@code ResourceNotFoundException} class should be generated
     * @throws IOException if an error occurs while writing the generated class to the specified path
     */
    public static void generateResourceNotFoundException(String outputPath) throws IOException {
        // Define the package name and class name
        var packageName = "com.techconqueror.codegen.exception";
        var className = "ResourceNotFoundException";

        // Create the exception class
        var exceptionClass = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .superclass(RuntimeException.class)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String.class, "message")
                        .addStatement("super(message)")
                        .build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String.class, "message")
                        .addParameter(Throwable.class, "cause")
                        .addStatement("super(message, cause)")
                        .build())
                .build();

        // Build the Java file
        var javaFile = JavaFile.builder(packageName, exceptionClass).build();

        // Write to the output path
        javaFile.writeTo(Path.of(outputPath));
    }
}
