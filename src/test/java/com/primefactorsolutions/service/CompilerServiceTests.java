package com.primefactorsolutions.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CompilerServiceTests {
    @InjectMocks
    private CompilerService compilerService;

    @Test
    public void testDoCompile() {
        final var result = compilerService.doCompile("package com.primefactorsolutions; import java.util.*; public class TestClass { public static Map<String, Boolean> run() { return Map.of(\"test\", true); } }");

        Assertions.assertThat(result).isEqualTo(Optional.of(String.format("%-50s ... %4s", "test", "OK")));
    }
}
