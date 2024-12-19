package com.primefactorsolutions.service.compiler;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

import static java.util.Objects.requireNonNull;

public class JavaSourceFromString extends SimpleJavaFileObject {

    private String sourceCode;

    public JavaSourceFromString(final String name, final String sourceCode) {
        super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
                Kind.SOURCE);
        this.sourceCode = requireNonNull(sourceCode, "sourceCode must not be null");
    }

    @Override
    public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
        return sourceCode;
    }
}
