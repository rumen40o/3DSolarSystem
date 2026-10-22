package org.example;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private final Vector3f position;
    private final Vector3f target;
    private final Vector3f up;

    private float fov = 60.0f;
    private float nearPlane = 0.1f;
    private float farPlane = 1000.0f;

    public Camera() {
        position = new Vector3f(0.0f, 6.0f, 10.0f);
        target = new Vector3f(0.0f, 0.0f, 0.0f);
        up = new Vector3f(0.0f, 1.0f, 0.0f);
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(
                position,
                target,
                up
        );
    }

    public Matrix4f getProjectionMatrix(
            int width,
            int height
    ) {
        float aspectRatio = (float) width / height;

        return new Matrix4f().perspective(
                (float) Math.toRadians(fov),
                aspectRatio,
                nearPlane,
                farPlane
        );
    }

    public void move(float x, float y, float z) {
        position.add(x, y, z);
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public void setTarget(float x, float y, float z) {
        target.set(x, y, z);
    }

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setFov(float fov) {
        this.fov = Math.max(20.0f, Math.min(100.0f, fov));
    }
}

