package com.techconqueror.codegen.generator.hibernate;

import static com.techconqueror.codegen.generator.common.ExceptionGenerator.generateResourceNotFoundException;
import static com.techconqueror.codegen.generator.hibernate.HibernateAnnotationGenerator.*;
import static com.techconqueror.codegen.generator.layer.RepositoryGenerator.generateRepository;
import static com.techconqueror.codegen.generator.layer.RestControllerGenerator.generateController;
import static com.techconqueror.codegen.generator.layer.ServiceGenerator.generateService;
import static org.springframework.util.StringUtils.capitalize;

import com.google.common.base.CaseFormat;
import com.squareup.javapoet.*;
import com.techconqueror.codegen.generator.java.ClassSource;
import jakarta.persistence.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * The {@code HibernateEntityGenerator} class provides functionality to generate Java entity classes
 * from database table structures. It connects to a relational database, fetches metadata about the
 * tables, and generates Hibernate-compatible Java classes using the JavaPoet library.
 * <p>
 * The generated entity classes include proper annotations for Hibernate Object-Relational Mapping (ORM),
 * including {@code @Entity}, {@code @Table}, {@code @Id}, {@code @OneToOne}, {@code @ManyToOne},
 * and {@code @Column}.
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *     <li>Automatically generates entity classes for all tables in a specified database schema.</li>
 *     <li>Handles primary keys, foreign keys, and unique constraints with appropriate annotations.</li>
 *     <li>Supports various Java types mapped from database column types.</li>
 *     <li>Handles relationships between entities, such as {@code @OneToOne} and {@code @ManyToOne}.</li>
 *     <li>Uses customizable annotations and metadata through a modular architecture.</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * Connection connection = ...; // Obtain a JDBC connection
 * String outputPath = "path/to/output/directory";
 * HibernateEntityGenerator.generateEntitiesForAllTables(outputPath, connection);
 * }</pre>
 *
 * @see EntityClassMetadata
 * @see EntityFieldMetadata
 * @see HibernateAnnotationGenerator
 */
public class HibernateEntityGenerator {

    /**
     * Generates entity classes for all tables in the given database connection and writes them to the specified output path.
     *
     * @param outputPath the directory path where the generated entity files will be saved.
     * @param connection the {@link Connection} object used to connect to the database.
     * @throws Exception if an error occurs while generating the entities.
     */
    public static void generateEntitiesForAllTables(String outputPath, Connection connection) throws Exception {
        var tableNames = fetchAllTableNames(connection);
        for (var tableName : tableNames) {
            System.out.println("Generating entity for table: " + tableName);
            generateEntityFromTable(tableName, outputPath, connection);
        }
    }

    /**
     * Generates a Java entity class for a specific table and writes it to the specified output path.
     *
     * @param tableName  the name of the table for which the entity class is to be generated.
     * @param outputPath the directory path where the generated entity file will be saved.
     * @param connection the {@link Connection} object used to connect to the database.
     * @throws Exception if an error occurs while generating the entity.
     */
    private static void generateEntityFromTable(String tableName, String outputPath, Connection connection)
            throws Exception {
        var metadata = fetchTableMetadata(tableName, connection);
        metadata.getAnnotations().add(createEntityAnnotation());
        metadata.getAnnotations().add(createTableAnnotation(metadata.getTableName()));
        generateResourceNotFoundException(outputPath);
        generateEntityFile(metadata, outputPath);
        generateRepository(metadata, outputPath);
        generateService(metadata, outputPath);
        generateController(metadata, outputPath);
    }

    /**
     * Generates a Java entity file based on the provided {@link EntityClassMetadata}.
     * The file is written to the specified output path.
     *
     * @param metadata   the metadata containing information about the entity to be generated.
     * @param outputPath the directory path where the generated entity file will be saved.
     * @throws IOException if an error occurs while writing the entity file.
     */
    private static void generateEntityFile(EntityClassMetadata metadata, String outputPath) throws IOException {
        var entityClass = ClassSource.createClass(metadata.getName())
                .addAnnotations(metadata.getAnnotations())
                .addFields(metadata.getFields())
                .withNoArgConstructor(metadata.getIsNoArgsConstructorNeeded())
                .build();
        var javaFile = JavaFile.builder("com.techconqueror.codegen.entity", entityClass)
                .build();
        javaFile.writeTo(Path.of(outputPath));
    }

    /**
     * Fetches the names of all tables in the database.
     *
     * @param connection the {@link Connection} object used to connect to the database.
     * @return a list of table names in the database.
     * @throws SQLException if an error occurs while fetching the table names.
     */
    private static List<String> fetchAllTableNames(Connection connection) throws SQLException {
        var tableNames = new ArrayList<String>();
        var query =
                """
                    SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema = 'public'
                      AND table_type = 'BASE TABLE';
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            var resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tableNames.add(resultSet.getString("table_name"));
            }
        }

        return tableNames;
    }

    /**
     * Fetches the metadata of a specific table, including column details, primary keys, foreign keys, and constraints.
     *
     * @param tableName  the name of the table for which the metadata is to be fetched.
     * @param connection the {@link Connection} object used to connect to the database.
     * @return an {@link EntityClassMetadata} object containing the metadata of the table.
     * @throws SQLException           if an error occurs while fetching the table metadata.
     * @throws ClassNotFoundException if a referenced entity class cannot be resolved during metadata processing.
     */
    private static EntityClassMetadata fetchTableMetadata(String tableName, Connection connection)
            throws SQLException, ClassNotFoundException {
        var fields = new ArrayList<EntityFieldMetadata>();
        var childEntities = findChildEntities(tableName, connection);
        var columnQuery =
                """
                    SELECT
                        c.column_name,
                        c.data_type,
                        c.is_nullable,
                        c.character_maximum_length,
                        tc.constraint_type,
                        ccu.table_name AS referenced_table,
                        ccu.column_name AS referenced_column,
                        EXISTS (
                            SELECT 1
                            FROM pg_catalog.pg_index i
                            JOIN pg_catalog.pg_attribute a
                                ON a.attnum = ANY(i.indkey)
                                AND a.attrelid = i.indrelid
                            WHERE i.indrelid = (quote_ident(c.table_schema) || '.' || quote_ident(c.table_name))::regclass
                            AND i.indisunique
                            AND a.attname = c.column_name
                        ) AS is_unique
                    FROM information_schema.columns c
                    LEFT JOIN information_schema.key_column_usage kcu
                        ON c.table_name = kcu.table_name AND c.column_name = kcu.column_name
                    LEFT JOIN information_schema.table_constraints tc
                        ON kcu.table_name = tc.table_name AND kcu.constraint_name = tc.constraint_name
                    LEFT JOIN information_schema.constraint_column_usage ccu
                        ON tc.constraint_name = ccu.constraint_name
                    WHERE c.table_name = ?
                    ORDER BY c.ordinal_position;
                """;

        // Fetch columns and metadata
        try (var statement = connection.prepareStatement(columnQuery)) {
            statement.setString(1, tableName);
            var resultSet = statement.executeQuery();

            while (resultSet.next()) {
                var columnName = resultSet.getString("column_name");
                var dataType = resultSet.getString("data_type");
                var isNullable = resultSet.getString("is_nullable").equalsIgnoreCase("YES");
                var constraintType = resultSet.getString("constraint_type");
                var referencedTable = resultSet.getString("referenced_table");
                var referencedColumn = resultSet.getString("referenced_column");
                var columnLength = resultSet.getObject("character_maximum_length", Integer.class);
                var isUnique = resultSet.getBoolean("is_unique");

                var isPrimaryKey = "PRIMARY KEY".equals(constraintType);
                var isForeignKey = "FOREIGN KEY".equals(constraintType);

                var entityFieldMetadata = new EntityFieldMetadata(
                        isPrimaryKey
                                ? "id"
                                : CaseFormat.LOWER_UNDERSCORE.to(
                                        CaseFormat.LOWER_CAMEL, isForeignKey ? referencedTable : columnName),
                        isForeignKey ? mapToEntityClass(referencedTable) : TypeName.get(mapToJavaType(dataType)),
                        createFieldAnnotations(
                                columnName,
                                isNullable,
                                isPrimaryKey,
                                isForeignKey,
                                columnLength,
                                referencedColumn,
                                isUnique));

                fields.add(entityFieldMetadata);
            }
        }

        // Add fields for child entities
        for (var child : childEntities.entrySet()) {
            fields.add(createRelationshipField(tableName, child.getKey(), child.getValue()));
        }

        return new EntityClassMetadata(capitalize(tableName), fields, tableName);
    }

    /**
     * Retrieves child entities (tables that reference the given table via a foreign key)
     * and determines the uniqueness of each relationship.
     *
     * <p>
     * This method identifies child tables in a database schema that reference the specified table
     * through foreign key constraints. It also checks whether the relationship is unique,
     * indicating a one-to-one relationship instead of a one-to-many relationship.
     * </p>
     *
     * @param parentTable the name of the parent table to find child entities for.
     * @param connection  the {@link Connection} object used to execute the query.
     * @return a map where the key is the child table name and the value is a {@code Boolean}
     * indicating whether the relationship is unique ({@code true} for one-to-one, {@code false} for one-to-many).
     * @throws SQLException if an error occurs while querying the database.
     */
    private static Map<String, Boolean> findChildEntities(String parentTable, Connection connection)
            throws SQLException {
        var childEntities = new HashMap<String, Boolean>();
        var query =
                """
                    SELECT
                        tc.table_name AS child_table,
                        EXISTS (
                            SELECT 1
                            FROM pg_catalog.pg_index i
                            JOIN pg_catalog.pg_attribute a
                                ON a.attnum = ANY(i.indkey)
                                AND a.attrelid = i.indrelid
                            WHERE i.indrelid = (quote_ident(tc.table_schema) || '.' || quote_ident(tc.table_name))::regclass
                            AND i.indisunique
                            AND a.attname = kcu.column_name
                        ) AS is_unique
                    FROM information_schema.table_constraints AS tc
                    LEFT JOIN information_schema.constraint_column_usage AS ccu
                        ON tc.constraint_name = ccu.constraint_name
                    LEFT JOIN information_schema.key_column_usage AS kcu
                        ON tc.constraint_name = kcu.constraint_name
                        AND tc.table_name = kcu.table_name
                    WHERE tc.constraint_type = 'FOREIGN KEY' AND ccu.table_name = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, parentTable);
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    var childTable = resultSet.getString("child_table");
                    var isUnique = resultSet.getBoolean("is_unique");
                    childEntities.put(childTable, isUnique);
                }
            }
        }

        return childEntities;
    }

    /**
     * Creates a list of annotations for the specified column based on its metadata.
     *
     * @param columnName       the name of the column.
     * @param isNullable       whether the column is nullable.
     * @param isPrimaryKey     whether the column is a primary key.
     * @param isForeignKey     whether the column is a foreign key.
     * @param columnLength     the maximum length of the column, if applicable.
     * @param referencedColumn the column in the referenced table, if it is a foreign key.
     * @param isUnique         whether the column has a unique constraint.
     * @return a list of {@link AnnotationSpec} objects for the column.
     */
    private static List<AnnotationSpec> createFieldAnnotations(
            String columnName,
            boolean isNullable,
            boolean isPrimaryKey,
            boolean isForeignKey,
            Integer columnLength,
            String referencedColumn,
            boolean isUnique) {
        var annotations = new ArrayList<AnnotationSpec>();

        // Add @Id annotation if the field is a primary key
        if (isPrimaryKey) {
            annotations.add(createIdAnnotation());
            annotations.add(createGeneratedValueAnnotation());
        }

        if (isForeignKey) {
            if (isUnique) {
                // Add @OneToOne annotation if the foreign key is unique
                annotations.add(AnnotationSpec.builder(OneToOne.class).build());
            } else {
                // Add @ManyToOne annotation if the foreign key is not unique
                annotations.add(AnnotationSpec.builder(ManyToOne.class).build());
            }

            annotations.add(AnnotationSpec.builder(JoinColumn.class)
                    .addMember("name", "$S", columnName)
                    .addMember("referencedColumnName", "$S", referencedColumn)
                    .build());
        } else {
            // Add @Column annotation
            annotations.add(createColumnAnnotation(columnName, isNullable, columnLength));
        }

        return annotations;
    }

    /**
     * Creates a metadata field for a bidirectional relationship between two entities.
     * <p>
     * This method generates the field in the parent entity representing the child entity/entities.
     * Depending on whether the relationship is unique, it configures either a {@code @OneToOne} or
     * a {@code @OneToMany} annotation. The {@code mappedBy} attribute specifies the field in the child entity
     * that defines the inverse relationship to the parent entity.
     * </p>
     *
     * @param parentTable the name of the parent table in the database, in snake_case.
     * @param childTable  the name of the child table in the database, in snake_case.
     * @param isUnique    {@code true} if the relationship is unique (e.g., one-to-one);
     *                    {@code false} if the relationship involves multiple child entities (one-to-many).
     * @return an {@link EntityFieldMetadata} object representing the relationship,
     * including the field name, its type, and annotations.
     */
    private static EntityFieldMetadata createRelationshipField(
            String parentTable, String childTable, boolean isUnique) {
        // Convert table names from snake_case to camelCase
        var formattedParentTable = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, parentTable);
        var formattedChildTable = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, childTable);

        // Define the field name based on relationship type
        var fieldName = formattedChildTable + (isUnique ? "" : "Set");

        // Determine the field type: single entity for @OneToOne or Set for @OneToMany
        var fieldType = isUnique
                ? mapToEntityClass(childTable)
                : ParameterizedTypeName.get(ClassName.get(Set.class), mapToEntityClass(childTable));

        // Select the appropriate annotation
        var annotation = isUnique
                ? AnnotationSpec.builder(OneToOne.class)
                : AnnotationSpec.builder(OneToMany.class).addMember("fetch", "$T.LAZY", FetchType.class);

        annotation.addMember("mappedBy", "$S", formattedParentTable);

        return new EntityFieldMetadata(fieldName, fieldType, List.of(annotation.build()));
    }

    /**
     * Maps a database type to its corresponding Java type.
     *
     * @param dataType the database column type.
     * @return the corresponding Java type.
     */
    private static Class<?> mapToJavaType(String dataType) {
        return switch (dataType) {
            case "boolean" -> Boolean.class;
            case "smallint", "smallserial" -> Short.class;
            case "integer", "serial" -> Integer.class;
            case "bigint", "bigserial" -> Long.class;
            case "text", "character varying" -> String.class;
            case "numeric", "money" -> BigDecimal.class;
            case "real" -> Float.class;
            case "double precision" -> Double.class;
            case "date" -> LocalDate.class;
            case "time" -> LocalTime.class;
            case "timestamp", "time without time zone", "timestamp without time zone" -> Instant.class;
            default -> Object.class;
        };
    }

    /**
     * Maps a referenced table name to the corresponding JavaPoet {@link ClassName} for the entity class.
     * <p>
     * This method assumes that the entity classes follow a naming convention where the table name is converted
     * to PascalCase (e.g., "user_account" becomes "UserAccount") and reside in the package
     * {@code com.techconqueror.codegen.entity}.
     * </p>
     *
     * @param referencedTable the name of the referenced table, in snake_case.
     * @return a {@link ClassName} object representing the fully qualified name of the entity class.
     * @throws IllegalArgumentException if the referenced table name is {@code null} or blank.
     */
    private static ClassName mapToEntityClass(String referencedTable) {
        if (referencedTable == null || referencedTable.isBlank()) {
            throw new IllegalArgumentException("Referenced table name cannot be null or blank");
        }

        var className = capitalize(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, referencedTable));
        return ClassName.get("com.techconqueror.codegen.entity", className);
    }
}
