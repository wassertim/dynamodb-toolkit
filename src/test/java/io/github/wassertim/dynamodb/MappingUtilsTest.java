package io.github.wassertim.dynamodb;

import io.github.wassertim.dynamodb.runtime.MappingUtils;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for MappingUtils runtime utilities.
 */
class MappingUtilsTest {

    @Test
    void parseInstantSafely_validInput_returnsInstant() {
        String instantString = "2023-01-01T10:00:00Z";
        Instant result = MappingUtils.parseInstantSafely(instantString);

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo(instantString);
    }

    @Test
    void parseInstantSafely_invalidInput_returnsNull() {
        assertThat(MappingUtils.parseInstantSafely("invalid")).isNull();
        assertThat(MappingUtils.parseInstantSafely(null)).isNull();
        assertThat(MappingUtils.parseInstantSafely("")).isNull();
    }

    @Test
    void parseEnumSafely_validInput_returnsEnum() {
        TestEnum result = MappingUtils.parseEnumSafely("VALUE1", TestEnum.class);

        assertThat(result).isEqualTo(TestEnum.VALUE1);
    }

    @Test
    void parseEnumSafely_invalidInput_returnsNull() {
        assertThat(MappingUtils.parseEnumSafely("INVALID", TestEnum.class)).isNull();
        assertThat(MappingUtils.parseEnumSafely(null, TestEnum.class)).isNull();
        assertThat(MappingUtils.parseEnumSafely("", TestEnum.class)).isNull();
    }

    @Test
    void createStringAttribute_validInput_returnsAttributeValue() {
        AttributeValue result = MappingUtils.createStringAttribute("test");

        assertThat(result).isNotNull();
        assertThat(result.s()).isEqualTo("test");
    }

    @Test
    void createStringAttribute_nullInput_returnsNull() {
        assertThat(MappingUtils.createStringAttribute(null)).isNull();
    }

    @Test
    void createNumberAttribute_validInput_returnsAttributeValue() {
        AttributeValue result = MappingUtils.createNumberAttribute(42.5);

        assertThat(result).isNotNull();
        assertThat(result.n()).isEqualTo("42.5");
    }

    @Test
    void createNumberAttribute_nullInput_returnsNull() {
        assertThat(MappingUtils.createNumberAttribute((Double) null)).isNull();
    }

    @Test
    void createListAttribute_validInput_returnsAttributeValue() {
        List<AttributeValue> values = List.of(
            AttributeValue.builder().s("item1").build(),
            AttributeValue.builder().s("item2").build()
        );

        AttributeValue result = MappingUtils.createListAttribute(values);

        assertThat(result).isNotNull();
        assertThat(result.l()).hasSize(2);
        assertThat(result.l().get(0).s()).isEqualTo("item1");
        assertThat(result.l().get(1).s()).isEqualTo("item2");
    }

    @Test
    void createListAttribute_emptyInput_returnsNull() {
        assertThat(MappingUtils.createListAttribute(null)).isNull();
        assertThat(MappingUtils.createListAttribute(List.of())).isNull();
    }

    private enum TestEnum {
        VALUE1, VALUE2
    }
}