package com.primefactorsolutions.service.compiler;

import javax.tools.SimpleJavaFileObject;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URI;

public class JavaClassAsBytes extends SimpleJavaFileObject {

    private final ByteArrayOutputStream bos =
            new ByteArrayOutputStream();

    public JavaClassAsBytes(final String name, final Kind kind) {
        super(URI.create("string:///" + name.replace('.', '/')
                + kind.extension), kind);
    }

    public byte[] getBytes() {
        return bos.toByteArray();
    }

    @Override
    public OutputStream openOutputStream() {
        return bos;
    }
}
