package com.primefactorsolutions.service;

import com.primefactorsolutions.service.compiler.InMemoryFileManager;
import com.primefactorsolutions.service.compiler.JavaSourceFromString;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CompilerService {
    private static final Map<Boolean, String> RESULT_MESSAGE = Map.of(Boolean.TRUE, "OK", Boolean.FALSE, "ERROR");

    @SneakyThrows
    public Optional<String> doCompile(final String javaCode) {
        final String qualifiedClassName = "com.primefactorsolutions.TestClass";
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        final InMemoryFileManager manager = new InMemoryFileManager(
                compiler.getStandardFileManager(null, null, null));

        final List<JavaFileObject> sourceFiles = Collections.singletonList(new JavaSourceFromString(qualifiedClassName,
                javaCode));
        final JavaCompiler.CompilationTask task = compiler.getTask(null, manager, diagnostics,
                List.of("-proc:full", "-Xlint:-options"), null, sourceFiles);

        boolean result = task.call();

        if (!result) {
            final String errors = diagnostics.getDiagnostics().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("\n"));

            return Optional.of(errors);
        } else {
            final ClassLoader classLoader = manager.getClassLoader(null);
            final Class<?> clazz = classLoader.loadClass(qualifiedClassName);
            Map<String, Boolean> results = Map.of();

            try {
                Method[] methods = clazz.getMethods();
                for (Method m : methods) {
                    if ("run".equals(m.getName())) {
                        results = (Map<String, Boolean>) m.invoke(null, new Object[]{});
                        break;
                    }
                }

                if (results.isEmpty()) {
                    results = Map.of("No existe ningun resultado. Verifique el metodo 'run()'.", false);
                }
            } catch (Exception e) {
                results = Map.of("Exception: " + ExceptionUtils.getStackTrace(ExceptionUtils.getRootCause(e)), false);
            }

            return Optional.of(results.entrySet().stream()
                    .map(e -> String.format("%-50s ... %4s", e.getKey(), RESULT_MESSAGE.get(e.getValue())))
                    .collect(Collectors.joining("\n")));
        }
    }
}
