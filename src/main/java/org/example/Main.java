
package org.example;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Main {

    private long window;

    private int width = 1280;
    private int height = 720;

    private ShaderProgram shaderProgram;
    private ShaderProgram orbitShader;

    private Camera camera;
    private SkySphere skySphere;

    private final SimulationTime simulationTime = new SimulationTime();
    private int selectedPlanet = -1;

    private boolean mouseDragging = false;

    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;

    private static final float MOUSE_SENSITIVITY = 0.3f;
    private static final float ZOOM_SPEED = 1.5f;

    private CelestialBody sun;
    private CelestialBody moon;
    private Orbit moonOrbit;

    private SaturnRings saturnRings;
    private Texture saturnRingsTexture;
    private ShaderProgram ringsShader;

    private final List<CelestialBody> planets = new ArrayList<>();
    private final List<Orbit> orbits = new ArrayList<>();

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


    private void initCameraControls() {

        // Натискане и отпускане на левия бутон
        glfwSetMouseButtonCallback(
                window,
                (win, button, action, mods) -> {

                    if (button == GLFW_MOUSE_BUTTON_LEFT) {

                        if (action == GLFW_PRESS) {
                            mouseDragging = true;

                            double[] x = new double[1];
                            double[] y = new double[1];

                            glfwGetCursorPos(win, x, y);

                            lastMouseX = x[0];
                            lastMouseY = y[0];

                        } else if (action == GLFW_RELEASE) {
                            mouseDragging = false;
                        }
                    }
                }
        );

        // Движение на мишката
        glfwSetCursorPosCallback(
                window,
                (win, xpos, ypos) -> {

                    if (mouseDragging) {

                        double deltaX = xpos - lastMouseX;
                        double deltaY = ypos - lastMouseY;

                        camera.rotate(
                                (float) deltaX * MOUSE_SENSITIVITY,
                                (float) -deltaY * MOUSE_SENSITIVITY
                        );
                    }

                    lastMouseX = xpos;
                    lastMouseY = ypos;
                }
        );

        // Колелце на мишката
        glfwSetScrollCallback(
                window,
                (win, xoffset, yoffset) -> {
                    camera.zoom(
                            (float) yoffset * ZOOM_SPEED
                    );
                }
        );
    }


    private void processInput(float deltaTime) {

        float rotateSpeed = 60.0f * deltaTime;
        float zoomSpeed = 10.0f * deltaTime;

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
            camera.zoom(zoomSpeed);
        }

        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
            camera.zoom(-zoomSpeed);
        }

        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
            camera.rotate(-rotateSpeed, 0.0f);
        }

        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
            camera.rotate(rotateSpeed, 0.0f);
        }
    }



    private void selectPlanet(int index) {

        if (index < -1 || index >= planets.size()) {
            return;
        }

        selectedPlanet = index;

        if (selectedPlanet == -1) {

            camera.reset();

            System.out.println("Camera: Solar System");
            return;
        }

        CelestialBody planet = planets.get(selectedPlanet);

        camera.reset();

        // Distance proportional to planet radius
        float cameraDistance = planet.getRadius() * 5.0f;

        camera.setDistance(cameraDistance);

        // Immediately focus on current planet position
        float time = simulationTime.getTime();;

        org.joml.Vector3f position = planet.getPosition(time);

        camera.setTarget(
                position.x,
                position.y,
                position.z
        );

        System.out.println(
                "Selected planet: " + planet.getName()
        );
    }



    private void updateCameraTarget(float time) {

        if (selectedPlanet == -1) {
            camera.setTarget(0.0f, 0.0f, 0.0f);
            return;
        }

        CelestialBody planet = planets.get(selectedPlanet);

        org.joml.Vector3f position = planet.getPosition(time);

        camera.setTarget(
                position.x,
                position.y,
                position.z
        );
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
                width, height,
                "Solar System 3D",
                NULL, NULL
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

        glfwSetFramebufferSizeCallback(
                window,
                (win, w, h) -> {
                    width = w;
                    height = h;
                    glViewport(0, 0, w, h);
                }
        );



        glfwSetKeyCallback(
                window,
                (win, key, scancode, action, mods) -> {

                    if (action == GLFW_PRESS) {

                        // ESC - Exit
                        if (key == GLFW_KEY_ESCAPE) {
                            glfwSetWindowShouldClose(win, true);
                        }

                        // R - Reset camera
                        if (key == GLFW_KEY_R) {
                            selectPlanet(-1);
                        }

                        // 0 - Full Solar System view
                        if (key == GLFW_KEY_0) {
                            selectPlanet(-1);
                        }

                        // 1-8 - Select planet
                        if (key >= GLFW_KEY_1 &&
                                key <= GLFW_KEY_8) {

                            int index = key - GLFW_KEY_1;

                            selectPlanet(index);
                        }
                    }

                    if (key == GLFW_KEY_SPACE) {
                        simulationTime.togglePause();

                        System.out.println(
                                simulationTime.isPaused()
                                        ? "Simulation paused"
                                        : "Simulation resumed"
                        );
                    }

                    if (key == GLFW_KEY_EQUAL ||
                            key == GLFW_KEY_KP_ADD) {

                        simulationTime.increaseSpeed();

                        System.out.println(
                                "Simulation speed: " +
                                        simulationTime.getTimeScale() + "x"
                        );
                    }

                    if (key == GLFW_KEY_MINUS ||
                            key == GLFW_KEY_KP_SUBTRACT) {

                        simulationTime.decreaseSpeed();

                        System.out.println(
                                "Simulation speed: " +
                                        simulationTime.getTimeScale() + "x"
                        );
                    }

                    if (key == GLFW_KEY_T) {
                        simulationTime.reset();

                        System.out.println("Simulation time reset");
                    }


                }
        );



        camera = new Camera();
        initCameraControls();

        // Shaders
        initShaders();
        skySphere = new SkySphere();

        // Sun
        sun = new CelestialBody(
                "Sun",
                1.0f,
                0.0f,
                0.4f,
                "/textures/sun.jpg"
        );

        // Eight planets and their orbits
        initPlanets();

        saturnRings = new SaturnRings(
                0.65f,
                1.15f,
                180
        );

        saturnRingsTexture = new Texture(
                "/textures/saturn_rings.png"
        );

        glfwShowWindow(window);
    }

    private void initShaders() {


        String vertexShader = """
        #version 330 core

        layout (location = 0) in vec3 aPos;
        layout (location = 1) in vec2 aTexCoord;
        layout (location = 2) in vec3 aNormal;

        out vec2 TexCoord;
        out vec3 FragPos;
        out vec3 Normal;

        uniform mat4 model;
        uniform mat4 view;
        uniform mat4 projection;

        void main() {

            vec4 worldPosition = model * vec4(aPos, 1.0);

            FragPos = worldPosition.xyz;

            Normal = mat3(
                transpose(inverse(model))
            ) * aNormal;

            TexCoord = aTexCoord;

            gl_Position =
                projection *
                view *
                worldPosition;
        }
        """;




        String fragmentShader = """
        #version 330 core

        in vec2 TexCoord;
        in vec3 FragPos;
        in vec3 Normal;

        out vec4 FragColor;

        uniform sampler2D textureSampler;
        uniform vec3 lightPos;
        uniform vec3 viewPos;
        uniform bool isSun;

        void main() {

            vec4 texColor = texture(textureSampler, TexCoord);

            // Sun is self-illuminated
            if (isSun) {
                FragColor = texColor;
                return;
            }

            vec3 norm = normalize(Normal);
            vec3 lightDir = normalize(lightPos - FragPos);
            vec3 viewDir = normalize(viewPos - FragPos);

            // Ambient
            float ambientStrength = 0.12;
            vec3 ambient = vec3(ambientStrength);

            // Diffuse
            float diff = max(dot(norm, lightDir), 0.0);
            vec3 diffuse = diff * vec3(1.0);

            // Specular (Phong reflection)
            vec3 reflectDir = reflect(-lightDir, norm);

            float spec = pow(
                max(dot(viewDir, reflectDir), 0.0),
                32.0
            );

            float specularStrength = 0.05;
            vec3 specular = specularStrength * spec * vec3(1.0);

            // Specular only on illuminated side
            if (diff <= 0.0) {
                specular = vec3(0.0);
            }

            vec3 result =
                (ambient + diffuse) * texColor.rgb + specular;

            FragColor = vec4(result, texColor.a);
        }
        """;



        shaderProgram = new ShaderProgram(
                vertexShader,
                fragmentShader
        );

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


        String ringsVertexShader = """
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
                projection * view * model *
                vec4(aPos, 1.0);
        }
        """;

        String ringsFragmentShader = """
        #version 330 core

        in vec2 TexCoord;

        out vec4 FragColor;

        uniform sampler2D textureSampler;

        void main() {
            vec4 color = texture(
                textureSampler,
                TexCoord
            );

            if (color.a < 0.01) {
                discard;
            }

            FragColor = color;
        }
        """;

        ringsShader = new ShaderProgram(
                ringsVertexShader,
                ringsFragmentShader
        );

    }


    private void renderSaturnRings(
            Matrix4f view,
            Matrix4f projection,
            float time
    ) {

        // Saturn's orbital movement
        float orbitalAngle = time * 0.16f;

        float x = (float) (
                6.8f * Math.cos(orbitalAngle)
        );

        float z = (float) (
                6.8f * Math.sin(orbitalAngle)
        );

        Matrix4f ringsModel = new Matrix4f()
                .translate(x, 0.0f, z)
                .rotateZ((float) Math.toRadians(26.7));

        ringsShader.use();

        ringsShader.setUniform("model", ringsModel);
        ringsShader.setUniform("view", view);
        ringsShader.setUniform("projection", projection);

        glActiveTexture(GL_TEXTURE0);
        saturnRingsTexture.bind();

        glUniform1i(
                glGetUniformLocation(
                        ringsShader.getProgramId(),
                        "textureSampler"
                ),
                0
        );

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Transparent fragments should not write depth
        glDepthMask(false);

        // Render both sides of the ring
        glDisable(GL_CULL_FACE);

        saturnRings.render();

        glDepthMask(true);
        glDisable(GL_BLEND);
    }


    private void initPlanets() {

        addPlanet("Mercury", 0.12f, 1.5f,
                0.5f, 1.00f, "/textures/mercury.jpg");

        addPlanet("Venus", 0.23f, 2.0f,
                0.3f, 0.70f, "/textures/venus.jpg");

        addPlanet("Earth", 0.25f, 2.7f,
                0.8f, 0.55f, "/textures/earth.jpg");

        addPlanet("Mars", 0.18f, 3.5f,
                0.7f, 0.40f, "/textures/mars.jpg");

        addPlanet("Jupiter", 0.60f, 5.0f,
                1.2f, 0.22f, "/textures/jupiter.jpg");

        addPlanet("Saturn", 0.50f, 6.8f,
                1.0f, 0.16f, "/textures/saturn.jpg");

        addPlanet("Uranus", 0.35f, 8.5f,
                0.6f, 0.11f, "/textures/uranus.jpg");

        addPlanet("Neptune", 0.34f, 10.0f,
                0.6f, 0.08f, "/textures/neptune.jpg");

        moon = new CelestialBody(
                "Moon",
                0.07f,
                0.0f,
                0.15f,
                "/textures/moon.jpg"
        );

        moonOrbit = new Orbit(0.55f, 120);
    }



    private void renderMoon(
            Matrix4f view,
            Matrix4f projection,
            float time
    ) {
        CelestialBody earth = planets.get(2);

        org.joml.Vector3f earthPosition =
                earth.getPosition(time);

        float moonDistance = 0.55f;
        float moonOrbitalSpeed = 2.5f;

        float orbitalAngle = time * moonOrbitalSpeed;
        float inclination =
                (float) Math.toRadians(5.15);

        // Parent transformation: Earth's position
        Matrix4f earthTransform = new Matrix4f()
                .translate(earthPosition);

        // Tilted Moon orbit around Earth
        Matrix4f moonOrbitTransform =
                new Matrix4f(earthTransform)
                        .rotateZ(inclination)
                        .rotateY(-orbitalAngle)
                        .translate(moonDistance, 0.0f, 0.0f);

        // Moon orientation and size
        Matrix4f moonModel =
                new Matrix4f(moonOrbitTransform)
                        .rotateY((float) Math.toRadians(180.0))
                        .scale(0.07f);

        // Render Moon using existing Phong shader
        shaderProgram.use();
        shaderProgram.setUniform("isSun", false);
        shaderProgram.setUniform("model", moonModel);
        shaderProgram.setUniform("view", view);
        shaderProgram.setUniform("projection", projection);

        glActiveTexture(GL_TEXTURE0);
        moon.render();

        // The orbital line follows Earth's position
        // and uses the same inclination
        Matrix4f orbitModel =
                new Matrix4f(earthTransform)
                        .rotateZ(inclination);

        orbitShader.use();
        orbitShader.setUniform("model", orbitModel);
        orbitShader.setUniform("view", view);
        orbitShader.setUniform("projection", projection);

        moonOrbit.render();
    }



    private void addPlanet(
            String name,
            float radius,
            float distance,
            float rotationSpeed,
            float orbitalSpeed,
            String texturePath
    ) {

        CelestialBody planet = new CelestialBody(
                name,
                radius,
                distance,
                rotationSpeed,
                texturePath
        );

        planet.setOrbitalSpeed(orbitalSpeed);
        planets.add(planet);

        orbits.add(new Orbit(distance, 180));
    }

    private void loop() {

        float lastFrameTime = (float) glfwGetTime();

        while (!glfwWindowShouldClose(window)) {

            float currentTime = (float) glfwGetTime();

            float deltaTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;

            processInput(deltaTime);
            simulationTime.update(deltaTime);

            glClearColor(0.02f, 0.02f, 0.06f, 1.0f);

            glClear(
                    GL_COLOR_BUFFER_BIT |
                            GL_DEPTH_BUFFER_BIT
            );

            if (width > 0 && height > 0) {

                float time = simulationTime.getTime();
                updateCameraTarget(time);

                Matrix4f view = camera.getViewMatrix();

                Matrix4f projection =
                        camera.getProjectionMatrix(width, height);

                skySphere.render(view, projection);



                // Render textured celestial bodies
                shaderProgram.use();

                shaderProgram.setUniform(
                        "viewPos",
                        camera.getPosition()
                );

                shaderProgram.setUniform(
                        "lightPos",
                        new org.joml.Vector3f(0.0f, 0.0f, 0.0f)
                );

                shaderProgram.setUniform("view", view);
                shaderProgram.setUniform("projection", projection);

                glActiveTexture(GL_TEXTURE0);

                glUniform1i(
                        glGetUniformLocation(
                                shaderProgram.getProgramId(),
                                "textureSampler"
                        ),
                        0
                );

                // Sun
                shaderProgram.setUniform("isSun", true);

                shaderProgram.setUniform(
                        "model",
                        sun.getModelMatrix(time)
                );

                sun.render();

                shaderProgram.setUniform("isSun", false);
                // Planets
                for (CelestialBody planet : planets) {

                    shaderProgram.setUniform(
                            "model",
                            planet.getModelMatrix(time)
                    );

                    planet.render();
                }

                // Render orbit lines
                orbitShader.use();

                orbitShader.setUniform(
                        "model",
                        new Matrix4f()
                );

                orbitShader.setUniform("view", view);
                orbitShader.setUniform(
                        "projection", projection
                );

                for (Orbit orbit : orbits) {
                    orbit.render();
                }
                renderMoon(view, projection, time);
                renderSaturnRings(view, projection, time);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    private void cleanup() {

        if (window != NULL) {
            glfwMakeContextCurrent(window);

            if (saturnRings != null) {
                saturnRings.cleanup();
            }

            if (saturnRingsTexture != null) {
                saturnRingsTexture.cleanup();
            }

            if (ringsShader != null) {
                ringsShader.cleanup();
            }

            if (skySphere != null) {
                skySphere.cleanup();
            }

            for (Orbit orbit : orbits) {
                orbit.cleanup();
            }

            if (moon != null) {
                moon.cleanup();
            }

            if (moonOrbit != null) {
                moonOrbit.cleanup();
            }

            for (CelestialBody planet : planets) {
                planet.cleanup();
            }

            if (sun != null) {
                sun.cleanup();
            }

            if (orbitShader != null) {
                orbitShader.cleanup();
            }

            if (shaderProgram != null) {
                shaderProgram.cleanup();
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
