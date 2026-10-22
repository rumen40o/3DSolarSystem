package org.example;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SaturnRings {

    private final int vaoId;
    private final int vboId;
    private final int eboId;
    private final int indexCount;

    public SaturnRings(
            float innerRadius,
            float outerRadius,
            int segments
    ) {

        if (innerRadius <= 0 ||
                outerRadius <= innerRadius ||
                segments < 3) {

            throw new IllegalArgumentException(
                    "Invalid ring parameters"
            );
        }

        // Each vertex: position (3), UV (2), normal (3)
        float[] vertices = new float[(segments + 1) * 2 * 8];
        int[] indices = new int[segments * 6];

        for (int i = 0; i <= segments; i++) {

            double angle = 2.0 * Math.PI * i / segments;

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            for (int j = 0; j < 2; j++) {

                float radius =
                        j == 0 ? innerRadius : outerRadius;

                int index = (i * 2 + j) * 8;

                // Position
                vertices[index] = radius * cos;
                vertices[index + 1] = 0.0f;
                vertices[index + 2] = radius * sin;

                // UV coordinates
                vertices[index + 3] = (float) j;
                vertices[index + 4] = (float) i / segments;

                // Normal
                vertices[index + 5] = 0.0f;
                vertices[index + 6] = 1.0f;
                vertices[index + 7] = 0.0f;
            }
        }

        for (int i = 0; i < segments; i++) {

            int first = i * 2;
            int next = first + 2;

            int index = i * 6;

            indices[index] = first;
            indices[index + 1] = next;
            indices[index + 2] = first + 1;

            indices[index + 3] = first + 1;
            indices[index + 4] = next;
            indices[index + 5] = next + 1;
        }

        indexCount = indices.length;

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        eboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(
                GL_ARRAY_BUFFER,
                vertices,
                GL_STATIC_DRAW
        );

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(
                GL_ELEMENT_ARRAY_BUFFER,
                indices,
                GL_STATIC_DRAW
        );

        int stride = 8 * Float.BYTES;

        // Position
        glVertexAttribPointer(
                0, 3, GL_FLOAT, false,
                stride, 0
        );
        glEnableVertexAttribArray(0);

        // UV
        glVertexAttribPointer(
                1, 2, GL_FLOAT, false,
                stride, 3 * Float.BYTES
        );
        glEnableVertexAttribArray(1);

        // Normal
        glVertexAttribPointer(
                2, 3, GL_FLOAT, false,
                stride, 5 * Float.BYTES
        );
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);

        glDrawElements(
                GL_TRIANGLES,
                indexCount,
                GL_UNSIGNED_INT,
                0
        );

        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
    }
}
