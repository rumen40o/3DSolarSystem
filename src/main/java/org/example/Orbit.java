package org.example;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Orbit {

    private final int vaoId;
    private final int vboId;
    private final int vertexCount;

    public Orbit(float radius, int segments) {

        if (radius <= 0 || segments < 3) {
            throw new IllegalArgumentException(
                    "Invalid orbit parameters"
            );
        }

        float[] vertices = new float[segments * 5];

        for (int i = 0; i < segments; i++) {

            double angle =
                    2.0 * Math.PI * i / segments;

            float x = (float) (
                    radius * Math.cos(angle)
            );

            float z = (float) (
                    radius * Math.sin(angle)
            );

            int index = i * 5;

            // Position
            vertices[index] = x;
            vertices[index + 1] = 0.0f;
            vertices[index + 2] = z;

            // Texture coordinates
            vertices[index + 3] = 0.0f;
            vertices[index + 4] = 0.0f;
        }

        vertexCount = segments;

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glBufferData(
                GL_ARRAY_BUFFER,
                vertices,
                GL_STATIC_DRAW
        );

        glVertexAttribPointer(
                0,
                3,
                GL_FLOAT,
                false,
                5 * Float.BYTES,
                0
        );

        glEnableVertexAttribArray(0);

        glVertexAttribPointer(
                1,
                2,
                GL_FLOAT,
                false,
                5 * Float.BYTES,
                3 * Float.BYTES
        );

        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);

        glDrawArrays(
                GL_LINE_LOOP,
                0,
                vertexCount
        );

        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
    }
}

