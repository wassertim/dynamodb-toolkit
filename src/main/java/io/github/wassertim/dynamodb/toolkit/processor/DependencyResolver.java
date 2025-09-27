package io.github.wassertim.dynamodb.toolkit.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.*;

import io.github.wassertim.dynamodb.toolkit.analysis.TypeAnalyzer;
import io.github.wassertim.dynamodb.toolkit.analysis.TypeInfo;

/**
 * Resolves dependencies between @DynamoMappable types to determine
 * the correct order for mapper generation.
 */
public class DependencyResolver {

    private final TypeAnalyzer typeAnalyzer;
    private final Map<String, TypeInfo> typeInfoMap = new HashMap<>();
    private final Map<String, Set<String>> dependencyGraph = new HashMap<>();

    public DependencyResolver(TypeAnalyzer typeAnalyzer) {
        this.typeAnalyzer = typeAnalyzer;
    }

    /**
     * Analyzes all @DynamoMappable types and builds a dependency graph.
     */
    public void analyzeDependencies(Set<? extends Element> annotatedElements) {
        // First pass: analyze all types and collect type info
        for (Element element : annotatedElements) {
            if (element instanceof TypeElement typeElement) {
                TypeInfo typeInfo = typeAnalyzer.analyzeType(typeElement);
                typeInfoMap.put(typeInfo.getFullyQualifiedClassName(), typeInfo);
                dependencyGraph.put(typeInfo.getFullyQualifiedClassName(), typeInfo.getDependencies());
            }
        }
    }

    /**
     * Returns the processing order with dependencies processed before dependents.
     * Uses topological sorting to ensure correct order.
     */
    public List<Element> getProcessingOrder() {
        List<String> sortedClassNames = topologicalSort();
        List<Element> result = new ArrayList<>();

        for (String className : sortedClassNames) {
            TypeInfo typeInfo = typeInfoMap.get(className);
            if (typeInfo != null) {
                result.add(typeInfo.getTypeElement());
            }
        }

        return result;
    }

    private List<String> topologicalSort() {
        // Kahn's algorithm for topological sorting
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, Set<String>> reverseGraph = new HashMap<>();

        // Initialize in-degree count and reverse graph
        for (String node : dependencyGraph.keySet()) {
            inDegree.put(node, 0);
            reverseGraph.put(node, new HashSet<>());
        }

        // Build reverse graph and calculate in-degrees
        for (Map.Entry<String, Set<String>> entry : dependencyGraph.entrySet()) {
            String node = entry.getKey();
            Set<String> dependencies = entry.getValue();

            for (String dependency : dependencies) {
                String dependencyClass = resolveDependencyToClassName(dependency);
                if (dependencyClass != null && dependencyGraph.containsKey(dependencyClass)) {
                    reverseGraph.get(dependencyClass).add(node);
                    inDegree.put(node, inDegree.get(node) + 1);
                }
            }
        }

        // Find nodes with no incoming edges
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.offer(entry.getKey());
            }
        }

        List<String> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            String current = queue.poll();
            result.add(current);

            // Process all dependents
            for (String dependent : reverseGraph.get(current)) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) {
                    queue.offer(dependent);
                }
            }
        }

        // Check for circular dependencies
        if (result.size() != dependencyGraph.size()) {
            throw new IllegalStateException("Circular dependency detected in @DynamoMappable types");
        }

        return result;
    }

    /**
     * Resolves a mapper dependency name (e.g., "RouteMapper") to the corresponding
     * class name (e.g., "io.github.wassertim.domain.Route").
     */
    private String resolveDependencyToClassName(String mapperName) {
        if (!mapperName.endsWith("Mapper")) {
            return null;
        }

        String baseClassName = mapperName.substring(0, mapperName.length() - "Mapper".length());

        // Look for a matching class name in our type info map
        for (String className : typeInfoMap.keySet()) {
            if (className.endsWith("." + baseClassName)) {
                return className;
            }
        }

        return null;
    }

    public Map<String, TypeInfo> getTypeInfoMap() {
        return Collections.unmodifiableMap(typeInfoMap);
    }
}