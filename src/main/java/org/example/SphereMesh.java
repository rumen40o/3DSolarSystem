package org.example;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SphereMesh {

    private final int vaoId;
    private final int vboId;
    private final int eboId;
    private final int indexCount;

    public SphereMesh(float radius, int sectors, int stacks) {

        if (radius <= 0 || sectors < 3 || stacks < 2) {
            throw new IllegalArgumentException(
                    "Invalid sphere parameters"
            );
        }

        List<Float> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        // Генериране на върховете на сферата
        for (int i = 0; i <= stacks; i++) {

            double stackAngle =
                    Math.PI / 2.0 - i * Math.PI / stacks;

            float xy = (float) (radius * Math.cos(stackAngle));
            float y = (float) (radius * Math.sin(stackAngle));

            for (int j = 0; j <= sectors; j++) {

                double sectorAngle =
                        j * 2.0 * Math.PI / sectors;

                float x = (float) (xy * Math.cos(sectorAngle));
                float z = (float) (xy * Math.sin(sectorAngle));

                // Position
                vertices.add(x);
                vertices.add(y);
                vertices.add(z);

                // Texture coordinates
                vertices.add((float) j / sectors);
                vertices.add(1.0f - (float) i / stacks);
            }
        }

        // Генериране на триъгълниците
        for (int i = 0; i < stacks; i++) {

            int k1 = i * (sectors + 1);
            int k2 = k1 + sectors + 1;

            for (int j = 0; j < sectors; j++, k1++, k2++) {

                if (i != 0) {
                    indices.add(k1);
                    indices.add(k2);
                    indices.add(k1 + 1);
                }

                if (i != stacks - 1) {
                    indices.add(k1 + 1);
                    indices.add(k2);
                    indices.add(k2 + 1);
                }
            }
        }

        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }

        int[] indexArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            indexArray[i] = indices.get(i);
        }

        indexCount = indexArray.length;

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        eboId = glGenBuffers();

        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexArray, GL_STATIC_DRAW);

        // Position attribute
        glVertexAttribPointer(
                0, 3, GL_FLOAT, false,
                5 * Float.BYTES, 0
        );
        glEnableVertexAttribArray(0);

        // Texture coordinate attribute
        glVertexAttribPointer(
                1, 2, GL_FLOAT, false,
                5 * Float.BYTES, 3 * Float.BYTES
        );
        glEnableVertexAttribArray(1);

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

