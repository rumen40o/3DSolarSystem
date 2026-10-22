package org.example;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class CelestialBody {

    private final String name;
    private final float radius;
    private final float distance;
    private final float rotationSpeed;
    private float orbitalSpeed = 0.0f;

    private final Texture texture;
    private final SphereMesh mesh;

    public CelestialBody(
            String name,
            float radius,
            float distance,
            float rotationSpeed,
            String texturePath
    ) {
        this.name = name;
        this.radius = radius;
        this.distance = distance;
        this.rotationSpeed = rotationSpeed;

        this.texture = new Texture(texturePath);
        this.mesh = new SphereMesh(1.0f, 64, 32);
    }





    public Matrix4f getModelMatrix(float time) {

        Vector3f position = getPosition(time);

        float rotationAngle = time * rotationSpeed;

        return new Matrix4f()
                .translate(position)
                .rotateY(rotationAngle)
                .scale(radius);
    }



    public void render() {
        texture.bind();
        mesh.render();
    }

    public void cleanup() {
        mesh.cleanup();
        texture.cleanup();
    }

    public String getName() {
        return name;
    }

    public float getDistance() {
        return distance;
    }

    public float getRadius() {
        return radius;
    }

    public void setOrbitalSpeed(float orbitalSpeed) {
        this.orbitalSpeed = orbitalSpeed;
    }


    public Vector3f getPosition(float time) {

        float orbitalAngle = time * orbitalSpeed;

        float x = (float) (
                distance * Math.cos(orbitalAngle)
        );

        float z = (float) (
                distance * Math.sin(orbitalAngle)
        );

        return new Vector3f(x, 0.0f, z);
    }

}

