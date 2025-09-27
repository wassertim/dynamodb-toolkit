# DynamoDB Mapping Library

Annotation-driven DynamoDB mapping library with automatic dependency resolution and compile-time code generation for type-safe bidirectional conversion between domain objects and DynamoDB AttributeValue format.

## Features

- **Single annotation approach**: Only `@DynamoMappable` needed to mark types
- **Automatic dependency resolution**: Analyzes object relationships and generates proper CDI injection
- **Compile-time code generation**: No runtime reflection, full Quarkus/GraalVM compatibility
- **Type-safe mapping**: Compile-time validation and generation
- **CDI integration**: Generated mappers are `@ApplicationScoped` beans with dependency injection

## Usage

### 1. Mark your domain classes with `@DynamoMappable`

```java
@DynamoMappable
@Table(name = "routes")
public class Route {
    private String userId;
    private String routeId;
    private String name;
    private List<Waypoint> waypoints;   // Waypoint must also be @DynamoMappable
    private RouteGeometry geometry;     // RouteGeometry must also be @DynamoMappable
    private Instant createdAt;
}

@DynamoMappable
public class Waypoint {
    private double lat;
    private double lng;
    private String name;
}

@DynamoMappable
public class RouteGeometry {
    private GeometryType type;
    private List<List<Double>> coordinates;
}
```

### 2. Generated mappers are automatically injected

The annotation processor generates mapper classes with proper CDI dependency injection:

```java
@ApplicationScoped
public class RouteMapper {
    private final WaypointMapper waypointMapper;
    private final RouteGeometryMapper routeGeometryMapper;

    public RouteMapper(WaypointMapper waypointMapper, RouteGeometryMapper routeGeometryMapper) {
        this.waypointMapper = waypointMapper;
        this.routeGeometryMapper = routeGeometryMapper;
    }

    public AttributeValue toDynamoDbAttributeValue(Route route) { /* generated */ }
    public Route fromDynamoDbAttributeValue(AttributeValue attributeValue) { /* generated */ }
}
```

### 3. Use in your repository classes

```java
@ApplicationScoped
public class DynamoDbRoutesRepository implements RoutesRepository {

    private final DynamoDbClient dynamoDb;
    private final RouteMapper routeMapper; // Automatically injected

    public Route save(Route route) {
        Map<String, AttributeValue> item = routeMapper.toDynamoDbAttributeValue(route).m();
        // ... save to DynamoDB
    }

    public Route findById(String id) {
        // ... get from DynamoDB
        return routeMapper.fromDynamoDbAttributeValue(AttributeValue.builder().m(item).build());
    }
}
```

## Supported Field Types

| Type | Strategy | Description |
|------|----------|-------------|
| `String` | STRING | Direct string mapping |
| `Double`, `Integer`, etc. | NUMBER | Number mapping with type conversion |
| `Boolean` | BOOLEAN | Boolean mapping |
| `Instant` | INSTANT | ISO-8601 string representation |
| `Enum` | ENUM | Enum name mapping |
| `List<String>` | STRING_LIST | Native DynamoDB string set |
| `List<@DynamoMappable>` | COMPLEX_LIST | List of mapped complex objects |
| `@DynamoMappable` objects | COMPLEX_OBJECT | Nested object mapping via dependency injection |

## Build Integration

Add the annotation processor to your Maven build:

```xml
<dependency>
    <groupId>io.github.wassertim</groupId>
    <artifactId>dynamodb-toolkit</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

The annotation processor runs automatically during compilation and generates mapper classes in the same package as your domain classes.

## Benefits over Manual Mapping

- **Reduced Code**: 256 lines of manual mapping → ~10 lines of annotations
- **Type Safety**: Compile-time validation vs runtime parsing errors
- **Maintainability**: New entities require only `@DynamoMappable` annotation
- **Consistency**: Generated code follows same patterns
- **Performance**: No reflection at runtime, optimal native compilation
- **Dependency Management**: Automatic resolution of mapper dependencies

## Generated Code Structure

For each `@DynamoMappable` class, the processor generates:

- **Mapper class**: `{ClassName}Mapper` with CDI annotations
- **Bidirectional methods**: `toDynamoDbAttributeValue()` and `fromDynamoDbAttributeValue()`
- **Dependency injection**: Constructor injection for required mappers
- **Null safety**: Proper null handling throughout mapping logic
- **Error handling**: Safe parsing with fallback to null values

## Known Issues

### Java/Maven Compatibility Warnings

When using Java 21+ with Maven, you may see warnings like:
```
WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
WARNING: sun.misc.Unsafe::staticFieldBase has been called by com.google.inject.internal.aop.HiddenClassDefiner
```

These warnings are **harmless** and come from Google Guice (bundled with Maven), not from this library. They will be resolved when Maven updates to a newer Guice version. The warnings do not affect functionality and can be safely ignored.