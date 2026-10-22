package org.example;

import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Texture {

    private final int textureId;

    public Texture(String resourcePath) {

        byte[] imageBytes;

        try (InputStream stream =
                     Texture.class.getResourceAsStream(resourcePath)) {

            if (stream == null) {
                throw new RuntimeException(
                        "Texture not found: " + resourcePath
                );
            }

            imageBytes = stream.readAllBytes();

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load texture: " + resourcePath, e
            );
        }

        ByteBuffer imageBuffer = memAlloc(imageBytes.length);
        ByteBuffer pixels = null;

        try {
            imageBuffer.put(imageBytes).flip();

            try (MemoryStack stack = MemoryStack.stackPush()) {

                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                // OpenGL uses bottom-left as texture origin.
                stbi_set_flip_vertically_on_load(true);

                pixels = stbi_load_from_memory(
                        imageBuffer,
                        width,
                        height,
                        channels,
                        STBI_rgb_alpha
                );

                if (pixels == null) {
                    throw new RuntimeException(
                            "STB failed: " + stbi_failure_reason()
                    );
                }

                textureId = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, textureId);

                glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_WRAP_S,
                        GL_REPEAT
                );
                glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_WRAP_T,
                        GL_CLAMP_TO_EDGE
                );

                glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MIN_FILTER,
                        GL_LINEAR_MIPMAP_LINEAR
                );
                glTexParameteri(
                        GL_TEXTURE_2D,
                        GL_TEXTURE_MAG_FILTER,
                        GL_LINEAR
                );

                glTexImage2D(
                        GL_TEXTURE_2D,
                        0,
                        GL_RGBA8,
                        width.get(0),
                        height.get(0),
                        0,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        pixels
                );

                glGenerateMipmap(GL_TEXTURE_2D);
                glBindTexture(GL_TEXTURE_2D, 0);
            }

        } finally {
            if (pixels != null) {
                stbi_image_free(pixels);
            }
            memFree(imageBuffer);
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void cleanup() {
        glDeleteTextures(textureId);
    }
}

