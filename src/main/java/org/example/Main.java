
package org.example;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;

    private int width = 1280;
    private int height = 720;

    private ShaderProgram shaderProgram;
    private CelestialBody sun;
    private CelestialBody earth;
    private Orbit earthOrbit;
    private ShaderProgram orbitShader;
    private Camera camera;

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        try {
            init();
            loop();
        } finally {
            cleanup();
        }
    }

    private void init() {

        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException(
                    "Unable to initialize GLFW"
            );
        }

        glfwDefaultWindowHints();

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);

        glfwWindowHint(
                GLFW_OPENGL_PROFILE,
                GLFW_OPENGL_CORE_PROFILE
        );

        window = glfwCreateWindow(
                width,
                height,
                "Solar System 3D",
                NULL,
                NULL
        );

        if (window == NULL) {
            throw new RuntimeException(
                    "Failed to create GLFW window"
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);

        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glViewport(0, 0, width, height);

        // Window resize
        glfwSetFramebufferSizeCallback(
                window,
                (win, w, h) -> {
                    width = w;
                    height = h;
                    glViewport(0, 0, w, h);
                }
        );

        // Keyboard input
        glfwSetKeyCallback(
                window,
                (win, key, scancode, action, mods) -> {
                    if (key == GLFW_KEY_ESCAPE
                            && action == GLFW_PRESS) {

                        glfwSetWindowShouldClose(win, true);
                    }
                }
        );

        // Camera
        camera = new Camera();

        // SUN
        sun = new CelestialBody(
                "Sun",
                1.0f,
                0.0f,
                0.4f,
                "/textures/sun.jpg"
        );

        // EARTH
        earth = new CelestialBody(
                "Earth",
                0.35f,
                2.5f,
                0.8f,
                "/textures/earth.jpg"
        );

        earth.setOrbitalSpeed(0.25f);

        earthOrbit = new Orbit(2.5f, 180);


        String orbitVertexShader = """
        #version 330 core

        layout (location = 0) in vec3 aPos;

        uniform mat4 model;
        uniform mat4 view;
        uniform mat4 projection;

        void main() {
            gl_Position =
                projection * view * model *
                vec4(aPos, 1.0);
        }
        """;

        String orbitFragmentShader = """
        #version 330 core

        out vec4 FragColor;

        void main() {
            FragColor = vec4(
                0.45, 0.60, 0.85, 1.0
            );
        }
        """;

        orbitShader = new ShaderProgram(
                orbitVertexShader,
                orbitFragmentShader
        );


        // Vertex shader
        String vertexShader = """
                #version 330 core

                layout (location = 0) in vec3 aPos;
                layout (location = 1) in vec2 aTexCoord;

                out vec2 TexCoord;

                uniform mat4 model;
                uniform mat4 view;
                uniform mat4 projection;

                void main() {

                    TexCoord = aTexCoord;

                    gl_Position =
                        projection *
                        view *
                        model *
                        vec4(aPos, 1.0);
                }
                """;

        // Fragment shader
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

        shaderProgram = new ShaderProgram(
                vertexShader,
                fragmentShader
        );

        glfwShowWindow(window);
    }

    private void loop() {

        while (!glfwWindowShouldClose(window)) {

            glClearColor(
                    0.02f,
                    0.02f,
                    0.06f,
                    1.0f
            );

            glClear(
                    GL_COLOR_BUFFER_BIT |
                            GL_DEPTH_BUFFER_BIT
            );

            if (width > 0 && height > 0) {

                shaderProgram.use();

                // Camera matrices
                Matrix4f view =
                        camera.getViewMatrix();

                Matrix4f projection =
                        camera.getProjectionMatrix(
                                width,
                                height
                        );

                shaderProgram.setUniform(
                        "view",
                        view
                );

                shaderProgram.setUniform(
                        "projection",
                        projection
                );

                // Texture unit 0
                glActiveTexture(GL_TEXTURE0);

                glUniform1i(
                        glGetUniformLocation(
                                shaderProgram.getProgramId(),
                                "textureSampler"
                        ),
                        0
                );

                float time = (float) glfwGetTime();

                // Render Sun
                Matrix4f sunModel =
                        sun.getModelMatrix(time);

                shaderProgram.setUniform(
                        "model",
                        sunModel
                );

                sun.render();

                // Render Earth
                Matrix4f earthModel =
                        earth.getModelMatrix(time);

                shaderProgram.setUniform(
                        "model",
                        earthModel
                );

                earth.render();


// ORBIT
                orbitShader.use();

                glEnable(GL_DEPTH_TEST);

                orbitShader.setUniform(
                        "model",
                        new Matrix4f()
                );

                orbitShader.setUniform(
                        "view",
                        camera.getViewMatrix()
                );

                orbitShader.setUniform(
                        "projection",
                        camera.getProjectionMatrix(width, height)
                );

                earthOrbit.render();

            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {

        if (window != NULL) {
            glfwMakeContextCurrent(window);

            if (sun != null) {
                sun.cleanup();
            }

            if (earth != null) {
                earth.cleanup();
            }

            if (shaderProgram != null) {
                shaderProgram.cleanup();
            }

            if (earthOrbit != null) {
                earthOrbit.cleanup();
            }

            if (orbitShader != null) {
                orbitShader.cleanup();
            }

            glfwDestroyWindow(window);
        }

        glfwTerminate();

        GLFWErrorCallback callback =
                glfwSetErrorCallback(null);

        if (callback != null) {
            callback.free();
        }
    }
}
