package org.example;

import static org.lwjgl.opengl.GL20.*;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class ShaderProgram {

    private final int programId;

    public ShaderProgram(String vertexSource,
                         String fragmentSource) {

        int vertexShader = compileShader(
                vertexSource, GL_VERTEX_SHADER
        );

        int fragmentShader = compileShader(
                fragmentSource, GL_FRAGMENT_SHADER
        );

        programId = glCreateProgram();

        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);

        if (glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException(
                    "Shader linking failed: " +
                            glGetProgramInfoLog(programId)
            );
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private int compileShader(String source, int type) {
        int shader = glCreateShader(type);

        glShaderSource(shader, source);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException(
                    "Shader compilation failed: " +
                            glGetShaderInfoLog(shader)
            );
        }

        return shader;
    }

    public void use() {
        glUseProgram(programId);
    }


    public void setUniform(String name, Matrix4f matrix) {

        int location = glGetUniformLocation(programId, name);

        if (location == -1) {
            throw new IllegalArgumentException(
                    "Uniform not found: " + name
            );
        }


        try (MemoryStack stack = MemoryStack.stackPush()) {

            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);

            glUniformMatrix4fv(location, false, buffer);
        }
    }

    public int getProgramId() {
        return programId;
    }

    public void cleanup() {
        glDeleteProgram(programId);
    }
}

