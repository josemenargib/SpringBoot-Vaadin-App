package com.primefactorsolutions.service.compiler;

import static java.util.Objects.requireNonNull;

import java.util.Map;

public class InMemoryClassLoader extends ClassLoader {

    private final InMemoryFileManager manager;

    public InMemoryClassLoader(final ClassLoader parent, final InMemoryFileManager manager) {
        super(parent);
        this.manager = requireNonNull(manager, "manager must not be null");
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {

        Map<String, JavaClassAsBytes> compiledClasses = manager
                .getBytesMap();

        if (compiledClasses.containsKey(name)) {
            byte[] bytes = compiledClasses.get(name)
                    .getBytes();
            return defineClass(name, bytes, 0, bytes.length);
        } else {
            throw new ClassNotFoundException();
        }
    }
}