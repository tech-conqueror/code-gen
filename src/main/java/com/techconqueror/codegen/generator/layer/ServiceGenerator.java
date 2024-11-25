package com.techconqueror.codegen.generator.layer;

import com.squareup.javapoet.*;
import com.techconqueror.codegen.generator.hibernate.EntityClassMetadata;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.springframework.stereotype.Service;

/**
 * A utility class for generating service layer classes for specified entities.
 * <p>
 * The generated service class adheres to the standard conventions of the service layer in a
 * Spring application. It provides methods for basic CRUD operations and delegates data access
 * to the corresponding repository layer.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * EntityClassMetadata metadata = new EntityClassMetadata("User", ...);
 * String outputPath = "/path/to/output";
 * ServiceGenerator.generateService(metadata, outputPath);
 * </pre>
 * </p>
 */
public class ServiceGenerator {

    /**
     * Generates a service implementation for the given entity and writes it to the specified output path.
     * <p>
     * The generated service class includes:
     * <ul>
     *   <li>CRUD methods: {@code findAll}, {@code findById}, {@code save}, {@code update}, {@code deleteById}.</li>
     *   <li>Integration with the repository layer for database access.</li>
     *   <li>Custom exception handling for resource not found scenarios using {@code ResourceNotFoundException}.</li>
     *   <li>Partial update method using JSON Patch operations, integrating with a utility class {@code JsonMergePatchUtils}.</li>
     * </ul>
     * <p>
     * Example of the generated service class:
     * <pre>
     * package com.techconqueror.codegen.service;
     *
     * &#064;Service
     * public class UserService {
     *
     *     private final UserRepository repository;
     *
     *     public UserService(UserRepository repository) {
     *         this.repository = repository;
     *     }
     *
     *     public List&lt;User&gt; findAll() {
     *         return repository.findAll();
     *     }
     *
     *     public User findById(Long id) {
     *         return repository.findById(id)
     *             .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
     *     }
     *
     *     public User save(User entity) {
     *         return repository.save(entity);
     *     }
     *
     *     public User update(Long id, User entity) {
     *         if (!repository.existsById(id)) {
     *             throw new ResourceNotFoundException("User not found with id: " + id);
     *         }
     *         entity.setId(id);
     *         return repository.save(entity);
     *     }
     *
     *     public void deleteById(Long id) {
     *         repository.deleteById(id);
     *     }
     *
     *     public void partialUpdate(Long id, List&lt;JsonPatchOperation&gt; patchOperations) {
     *         User userEntity = repository.findById(id)
     *             .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
     *         repository.save(JsonMergePatchUtils.patchEntity(userEntity, User.class, patchOperations));
     *     }
     * }
     * </pre>
     *
     * @param metadata   the metadata of the entity for which the service implementation is to be generated.
     *                   This includes the entity name and package structure.
     * @param outputPath the directory path where the generated service file will be saved.
     * @throws IOException if an error occurs while writing the service file.
     */
    public static void generateService(EntityClassMetadata metadata, String outputPath) throws IOException {
        String entityName = metadata.getName();
        String repositoryName = entityName + "Repository";
        String serviceName = entityName + "Service";

        // Create a JavaPoet TypeSpec for the service class
        TypeSpec service = TypeSpec.classBuilder(serviceName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addField(
                        ClassName.get("com.techconqueror.codegen.repository", repositoryName),
                        "repository",
                        Modifier.PRIVATE,
                        Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(
                                ClassName.get("com.techconqueror.codegen.repository", repositoryName), "repository")
                        .addStatement("this.repository = repository")
                        .build())
                // Find all
                .addMethod(MethodSpec.methodBuilder("findAll")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(
                                ClassName.get(List.class),
                                ClassName.get("com.techconqueror.codegen.entity", entityName)))
                        .addStatement("return repository.findAll()")
                        .build())
                // Find by ID
                .addMethod(MethodSpec.methodBuilder("findById")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get("com.techconqueror.codegen.entity", entityName))
                        .addParameter(Long.class, "id")
                        .addStatement(
                                "return repository.findById(id).orElseThrow(() -> new $T(\"$L not found with id: \" + id))",
                                ClassName.get("com.techconqueror.codegen.exception", "ResourceNotFoundException"),
                                entityName)
                        .build())
                // Save (Create or Update)
                .addMethod(MethodSpec.methodBuilder("save")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get("com.techconqueror.codegen.entity", entityName))
                        .addParameter(ClassName.get("com.techconqueror.codegen.entity", entityName), "entity")
                        .addStatement("return repository.save(entity)")
                        .build())
                // Full Update
                .addMethod(MethodSpec.methodBuilder("update")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get("com.techconqueror.codegen.entity", entityName))
                        .addParameter(Long.class, "id")
                        .addParameter(ClassName.get("com.techconqueror.codegen.entity", entityName), "entity")
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("if (!repository.existsById(id))")
                                .addStatement(
                                        "throw new $T(\"$L not found with id: \" + id)",
                                        ClassName.get(
                                                "com.techconqueror.codegen.exception", "ResourceNotFoundException"),
                                        entityName)
                                .endControlFlow()
                                .addStatement("entity.setId(id)") // Ensure ID consistency
                                .addStatement("return repository.save(entity)")
                                .build())
                        .build())
                // Partial update
                .addMethod(MethodSpec.methodBuilder("partialUpdate")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addParameter(Long.class, "id")
                        .addParameter(
                                ParameterizedTypeName.get(
                                        ClassName.get(List.class),
                                        ClassName.get(
                                                "com.github.fge.jsonpatch.JsonPatchOperation", "JsonPatchOperation")),
                                "patchOperations")
                        .addCode(CodeBlock.builder()
                                .addStatement(
                                        "$T existingEntity = repository.findById(id).orElseThrow(() -> new $T(\"$L not found with id: \" + id))",
                                        ClassName.get("com.techconqueror.codegen.entity", entityName),
                                        ClassName.get(
                                                "com.techconqueror.codegen.exception", "ResourceNotFoundException"),
                                        entityName)
                                .addStatement(
                                        "repository.save($T.patchEntity(existingEntity, $T.class, patchOperations))",
                                        ClassName.get("com.techconqueror.codegen.util", "JsonMergePatchUtils"),
                                        ClassName.get("com.techconqueror.codegen.entity", entityName))
                                .build())
                        .build())
                // Delete
                .addMethod(MethodSpec.methodBuilder("deleteById")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class)
                        .addParameter(Long.class, "id")
                        .addStatement("repository.deleteById(id)")
                        .build())
                .build();

        // Build the Java file
        JavaFile javaFile =
                JavaFile.builder("com.techconqueror.codegen.service", service).build();

        // Write the file to the output directory
        javaFile.writeTo(Path.of(outputPath));
    }
}
