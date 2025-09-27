package io.github.wassertim.dynamodb.toolkit.generation;

import java.io.PrintWriter;

import io.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;

/**
 * Generates convenience methods for mapper classes to reduce boilerplate code.
 * Provides common patterns like converting lists of DynamoDB items to domain objects.
 */
public class ConvenienceMethodGenerator {

    /**
     * Generates convenience methods for common DynamoDB operations.
     */
    public void generateConvenienceMethods(PrintWriter writer, TypeInfo typeInfo) {
        String className = typeInfo.getClassName();

        writer.println("    // Convenience methods for reducing boilerplate");
        writer.println();

        generateFromDynamoDbItemMethod(writer, className);
        generateFromDynamoDbItemsMethod(writer, className);
        generateToDynamoDbItemMethod(writer, className);
        generateToDynamoDbItemsMethod(writer, className);
    }

    private void generateFromDynamoDbItemMethod(PrintWriter writer, String className) {
        writer.println("    /**");
        writer.println("     * Convenience method to convert a single DynamoDB item to a domain object.");
        writer.println("     * Handles the common pattern of mapping GetItemResponse.item() to domain objects.");
        writer.println("     *");
        writer.println("     * @param item DynamoDB item from GetItemResponse.item()");
        writer.println("     * @return Optional of " + className + " object, empty if item is null or conversion fails");
        writer.println("     */");
        writer.println("    public java.util.Optional<" + className + "> fromDynamoDbItem(Map<String, AttributeValue> item) {");
        writer.println("        if (item == null || item.isEmpty()) {");
        writer.println("            return java.util.Optional.empty();");
        writer.println("        }");
        writer.println("        " + className + " result = fromDynamoDbAttributeValue(AttributeValue.builder().m(item).build());");
        writer.println("        return java.util.Optional.ofNullable(result);");
        writer.println("    }");
        writer.println();
    }

    private void generateFromDynamoDbItemsMethod(PrintWriter writer, String className) {
        writer.println("    /**");
        writer.println("     * Convenience method to convert a list of DynamoDB items to domain objects.");
        writer.println("     * Handles the common pattern of mapping QueryResponse.items() to domain objects.");
        writer.println("     *");
        writer.println("     * @param items List of DynamoDB items from QueryResponse.items() or ScanResponse.items()");
        writer.println("     * @return List of " + className + " objects, filtering out any null results");
        writer.println("     */");
        writer.println("    public List<" + className + "> fromDynamoDbItems(List<Map<String, AttributeValue>> items) {");
        writer.println("        if (items == null || items.isEmpty()) {");
        writer.println("            return new ArrayList<>();");
        writer.println("        }");
        writer.println("        return items.stream()");
        writer.println("            .map(item -> AttributeValue.builder().m(item).build())");
        writer.println("            .map(this::fromDynamoDbAttributeValue)");
        writer.println("            .filter(Objects::nonNull)");
        writer.println("            .collect(Collectors.toList());");
        writer.println("    }");
        writer.println();
    }

    private void generateToDynamoDbItemMethod(PrintWriter writer, String className) {
        writer.println("    /**");
        writer.println("     * Convenience method to convert a single domain object to a DynamoDB item.");
        writer.println("     * Useful for PutItem operations.");
        writer.println("     *");
        writer.println("     * @param object The " + className + " object to convert");
        writer.println("     * @return DynamoDB item (Map<String, AttributeValue>), or null if input is null or conversion fails");
        writer.println("     */");
        writer.println("    public Map<String, AttributeValue> toDynamoDbItem(" + className + " object) {");
        writer.println("        if (object == null) {");
        writer.println("            return null;");
        writer.println("        }");
        writer.println("        AttributeValue av = toDynamoDbAttributeValue(object);");
        writer.println("        return av != null ? av.m() : null;");
        writer.println("    }");
        writer.println();
    }

    private void generateToDynamoDbItemsMethod(PrintWriter writer, String className) {
        writer.println("    /**");
        writer.println("     * Convenience method to convert a list of domain objects to DynamoDB items.");
        writer.println("     * Useful for batch operations like batchWriteItem.");
        writer.println("     *");
        writer.println("     * @param objects List of " + className + " objects to convert");
        writer.println("     * @return List of DynamoDB items (Map<String, AttributeValue>), filtering out any null results");
        writer.println("     */");
        writer.println("    public List<Map<String, AttributeValue>> toDynamoDbItems(List<" + className + "> objects) {");
        writer.println("        if (objects == null || objects.isEmpty()) {");
        writer.println("            return new ArrayList<>();");
        writer.println("        }");
        writer.println("        return objects.stream()");
        writer.println("            .map(this::toDynamoDbAttributeValue)");
        writer.println("            .filter(Objects::nonNull)");
        writer.println("            .map(av -> av.m())");
        writer.println("            .filter(map -> map != null && !map.isEmpty())");
        writer.println("            .collect(Collectors.toList());");
        writer.println("    }");
        writer.println();
    }
}