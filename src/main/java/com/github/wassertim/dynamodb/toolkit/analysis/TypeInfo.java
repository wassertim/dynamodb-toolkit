package com.github.wassertim.dynamodb.toolkit.analysis;

import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;

/**
 * Information about a type that needs a DynamoDB mapper generated.
 * Contains analyzed field information and dependencies.
 */
public class TypeInfo {
    public static final String MAPPER_PACKAGE = "com.github.wassertim.dynamodb.toolkit.mappers";

    private final TypeElement typeElement;
    private final String originalPackageName;
    private final String className;
    private final String tableName;
    private final String mapperClassName;
    private final List<FieldInfo> fields;
    private final Set<String> dependencies;

    public TypeInfo(TypeElement typeElement, String originalPackageName,
                    String className, String tableName, List<FieldInfo> fields, Set<String> dependencies) {
        this.typeElement = typeElement;
        this.originalPackageName = originalPackageName;
        this.className = className;
        this.tableName = tableName;
        this.mapperClassName = className + "Mapper";
        this.fields = fields;
        this.dependencies = dependencies;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    public String getPackageName() {
        return MAPPER_PACKAGE;
    }

    public String getOriginalPackageName() {
        return originalPackageName;
    }

    public String getClassName() {
        return className;
    }

    public String getTableName() {
        return tableName;
    }

    public String getMapperClassName() {
        return mapperClassName;
    }

    public String getFullyQualifiedClassName() {
        return originalPackageName + "." + className;
    }

    public String getFullyQualifiedMapperClassName() {
        return MAPPER_PACKAGE + "." + mapperClassName;
    }

    public List<FieldInfo> getFields() {
        return fields;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return "TypeInfo{" +
                "className='" + className + '\'' +
                ", dependencies=" + dependencies +
                ", fieldCount=" + fields.size() +
                '}';
    }
}