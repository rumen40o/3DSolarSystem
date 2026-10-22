
package org.example;

import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class SkySphere {

    private final SphereMesh sphere;
    private final Texture texture;
    private final ShaderProgram shader;

    public SkySphere() {

        sphere = new SphereMesh(500.0f, 128, 64);

        texture = new Texture(
                "/textures/milkyway.jpg"
        );

        String vertexShader = """
                #version 330 core

                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec2 aTexCoord;

                out vec2 TexCoord;

                uniform mat4 view;
                uniform mat4 projection;

                void main() {
                    TexCoord = aTexCoord;

                    gl_Position =
                        projection *
                        view *
                        vec4(aPos, 1.0);
                }
                """;

        String fragmentShader = """
                #version 330 core

                in vec2 TexCoord;

                out vec4 FragColor;

                uniform sampler2D textureSampler;

                void main() {
                    FragColor = texture(
                        textureSampler,
                        TexCoord
                    );
                }
                """;

        shader = new ShaderProgram(
                vertexShader,
                fragmentShader
        );
    }

    public void render(
            Matrix4f view,
            Matrix4f projection
    ) {

        // Remove camera translation:
        // sky follows the camera position
        Matrix4f skyView = new Matrix4f(view);

        skyView.m30(0.0f);
        skyView.m31(0.0f);
        skyView.m32(0.0f);

        glDisable(GL_DEPTH_TEST);
        // Draw sky behind scene objects
        glDepthMask(false);

        // Render inside of sphere
        glDisable(GL_CULL_FACE);

        shader.use();

        shader.setUniform("view", skyView);
        shader.setUniform(
                "projection",
                projection
        );

        glActiveTexture(GL_TEXTURE0);
        texture.bind();

        sphere.render();
        glDepthMask(true);
        glEnable(GL_DEPTH_TEST);

        glDepthMask(true);
    }

    public void cleanup() {
        sphere.cleanup();
        texture.cleanup();
        shader.cleanup();
    }
}
