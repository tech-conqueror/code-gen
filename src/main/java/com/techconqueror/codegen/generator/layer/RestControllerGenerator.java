package com.techconqueror.codegen.generator.layer;

import com.squareup.javapoet.*;
import com.techconqueror.codegen.generator.hibernate.EntityClassMetadata;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.lang.model.element.Modifier;

import org.springframework.web.bind.annotation.*;

/**
 * A utility class for generating REST controller classes for specified entities.
 * <p>
 * This class uses the {@code JavaPoet} library to programmatically create controller classes
 * adhering to standard REST API conventions. The generated controllers are annotated with
 * Spring's {@code RestController} and contain endpoints for CRUD operations, delegating
 * business logic to a service layer.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * EntityClassMetadata metadata = new EntityClassMetadata("User", ...);
 * String outputPath = "/path/to/output";
 * ControllerGenerator.generateController(metadata, outputPath);
 * </pre>
 * </p>
 */
public class RestControllerGenerator {

    /**
     * Generates a REST controller for the given entity and writes it to the specified output path.
     * <p>
     * The generated controller class includes:
     * <ul>
     *   <li>Endpoints for CRUD operations (GET, POST, PUT, PATCH, DELETE).</li>
     *   <li>Annotations for Spring's {@code RestController}, {@code RequestMapping}, and related mapping annotations.</li>
     *   <li>A dependency on the entity's corresponding service layer class for handling business logic.</li>
     * </ul>
     * <p>
     * Key Features:
     * <ul>
     *   <li><b>GET</b>: Endpoints for retrieving all entities or a single entity by ID.</li>
     *   <li><b>POST</b>: Endpoint for creating a new entity. Returns a {@code 201 Created} status code upon success.</li>
     *   <li><b>PUT</b>: Endpoint for fully updating an entity. Returns a {@code 204 No Content} status code upon success.</li>
     *   <li><b>PATCH</b>: Endpoint for partially updating an entity. Returns a {@code 204 No Content} status code upon success.</li>
     *   <li><b>DELETE</b>: Endpoint for deleting an entity by ID. Returns a {@code 204 No Content} status code upon success.</li>
     * </ul>
     * <p>
     * Example of the generated controller class:
     * <pre>
     * package com.techconqueror.codegen.controller;
     *
     * &#064;RestController
     * &#064;RequestMapping("/users")
     * public class UserRestController {
     *
     *     private final UserService service;
     *
     *     public UserRestController(UserService service) {
     *         this.service = service;
     *     }
     *
     *     &#064;GetMapping
     *     public List&lt;User&gt; getAll() {
     *         return service.findAll();
     *     }
     *
     *     &#064;GetMapping("/{id}")
     *     public User getById(@PathVariable Long id) {
     *         return service.findById(id);
     *     }
     *
     *     &#064;PostMapping
     *     &#064;ResponseStatus(HttpStatus.CREATED)
     *     public User create(@RequestBody User entity) {
     *         return service.save(entity);
     *     }
     *
     *     &#064;PutMapping("/{id}")
     *     &#064;ResponseStatus(HttpStatus.NO_CONTENT)
     *     public void replace(@PathVariable Long id, @RequestBody User entity) {
     *         service.update(id, entity);
     *     }
     *
     *     &#064;PatchMapping("/{id}")
     *     &#064;ResponseStatus(HttpStatus.NO_CONTENT)
     *     public void patch(@PathVariable Long id, @RequestBody User partialEntity) {
     *         service.partialUpdate(id, partialEntity);
     *     }
     *
     *     &#064;DeleteMapping("/{id}")
     *     &#064;ResponseStatus(HttpStatus.NO_CONTENT)
     *     public void delete(@PathVariable Long id) {
     *         service.deleteById(id);
     *     }
     * }
     * </pre>
     *
     * @param metadata   the metadata of the entity for which the controller is to be generated.
     *                   This includes the entity name and package structure.
     * @param outputPath the directory path where the generated controller file will be saved.
     * @throws IOException if an error occurs while writing the controller file.
     */
    public static void generateController(EntityClassMetadata metadata, String outputPath) throws IOException {
        String entityName = metadata.getName();
        String serviceName = entityName + "Service";
        String controllerName = entityName + "RestController";

        // Create a JavaPoet TypeSpec for the controller class
        TypeSpec controller = TypeSpec.classBuilder(controllerName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(RestController.class)
                .addAnnotation(AnnotationSpec.builder(RequestMapping.class)
                        .addMember("value", "$S", "/" + entityName.toLowerCase() + "s")
                        .build())
                .addField(
                        ClassName.get("com.techconqueror.codegen.service", serviceName),
                        "service",
                        Modifier.PRIVATE,
                        Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addParameter(ClassName.get("com.techconqueror.codegen.service", serviceName), "service")
                        .addStatement("this.service = service")
                        .build())
                // GET all
                .addMethod(MethodSpec.methodBuilder("getAll")
                        .addAnnotation(GetMapping.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ParameterizedTypeName.get(
                                ClassName.get(List.class),
                                ClassName.get("com.techconqueror.codegen.entity", entityName)))
                        .addStatement("return service.findAll()")
                        .build())
                // GET by ID
                .addMethod(MethodSpec.methodBuilder("getById")
                        .addAnnotation(AnnotationSpec.builder(GetMapping.class)
                                .addMember("value", "$S", "/{id}")
                                .build())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Long.class, "id")
                                .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                                        .build())
                                .build())
                        .returns(ClassName.get("com.techconqueror.codegen.entity", entityName))
                        .addStatement("return service.findById(id)")
                        .build())
                // POST (Create)
                .addMethod(MethodSpec.methodBuilder("create")
                        .addAnnotation(PostMapping.class)
                        .addAnnotation(AnnotationSpec.builder(ResponseStatus.class)
                                .addMember(
                                        "value", "$T.CREATED", ClassName.get("org.springframework.http", "HttpStatus"))
                                .build())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(
                                        ClassName.get("com.techconqueror.codegen.entity", entityName), "entity")
                                .addAnnotation(RequestBody.class)
                                .build())
                        .returns(ClassName.get("com.techconqueror.codegen.entity", entityName))
                        .addStatement("return service.save(entity)")
                        .build())
                // PUT (Update with service logic)
                .addMethod(MethodSpec.methodBuilder("replace")
                        .addAnnotation(AnnotationSpec.builder(PutMapping.class)
                                .addMember("value", "$S", "/{id}")
                                .build())
                        .addAnnotation(AnnotationSpec.builder(ResponseStatus.class)
                                .addMember(
                                        "value",
                                        "$T.NO_CONTENT",
                                        ClassName.get("org.springframework.http", "HttpStatus"))
                                .build())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Long.class, "id")
                                .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                                        .build())
                                .build())
                        .addParameter(ParameterSpec.builder(
                                        ClassName.get("com.techconqueror.codegen.entity", entityName), "entity")
                                .addAnnotation(RequestBody.class)
                                .build())
                        .returns(void.class)
                        .addStatement("service.update(id, entity)")
                        .build())
                // PATCH (Partial Update)
                .addMethod(MethodSpec.methodBuilder("patch")
                        .addAnnotation(AnnotationSpec.builder(PatchMapping.class)
                                .addMember("value", "$S", "/{id}")
                                .build())
                        .addAnnotation(AnnotationSpec.builder(ResponseStatus.class)
                                .addMember(
                                        "value",
                                        "$T.NO_CONTENT",
                                        ClassName.get("org.springframework.http", "HttpStatus"))
                                .build())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Long.class, "id")
                                .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                                        .build())
                                .build())
                        .addParameter(ParameterSpec.builder(
                                        ParameterizedTypeName.get(
                                                ClassName.get(List.class),
                                                ClassName.get("com.github.fge.jsonpatch", "JsonPatchOperation")),
                                        "patchOperations")
                                .addAnnotation(RequestBody.class)
                                .build())
                        .returns(void.class)
                        .addStatement("service.partialUpdate(id, patchOperations)")
                        .build())
                // DELETE
                .addMethod(MethodSpec.methodBuilder("delete")
                        .addAnnotation(AnnotationSpec.builder(DeleteMapping.class)
                                .addMember("value", "$S", "/{id}")
                                .build())
                        .addAnnotation(AnnotationSpec.builder(ResponseStatus.class)
                                .addMember(
                                        "value",
                                        "$T.NO_CONTENT",
                                        ClassName.get("org.springframework.http", "HttpStatus"))
                                .build())
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(Long.class, "id")
                                .addAnnotation(AnnotationSpec.builder(PathVariable.class)
                                        .build())
                                .build())
                        .returns(void.class)
                        .addStatement("service.deleteById(id)")
                        .build())
                .build();

        // Build the Java file
        JavaFile javaFile = JavaFile.builder("com.techconqueror.codegen.controller", controller)
                .build();

        // Write the file to the output directory
        javaFile.writeTo(Path.of(outputPath));
    }
}
