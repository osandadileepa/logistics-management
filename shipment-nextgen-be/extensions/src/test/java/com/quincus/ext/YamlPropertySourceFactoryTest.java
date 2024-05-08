package com.quincus.ext;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class YamlPropertySourceFactoryTest {

    @Test
    void should_read_properties() throws IOException {
        final YamlPropertySourceFactory factory = new YamlPropertySourceFactory();

        final PropertySource source = factory.createPropertySource("test", new EncodedResource(new ClassPathResource("test.yaml")));
        assertThat(source.getProperty("test.config.message")).withFailMessage("String input was not parsed correctly.").isEqualTo("chuck norris");
    }

    @Test
    void should_return_empty() throws IOException {
        final YamlPropertySourceFactory factory = new YamlPropertySourceFactory();

        final PropertySource source = factory.createPropertySource("test", new EncodedResource(new ClassPathResource("non-existing.yaml")));
        assertThat(source.containsProperty("test.config.message")).isFalse();

    }

}
