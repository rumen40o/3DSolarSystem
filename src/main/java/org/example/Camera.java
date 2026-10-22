
package org.example;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private final Vector3f position = new Vector3f();
    private final Vector3f target = new Vector3f(0, 0, 0);
    private final Vector3f up = new Vector3f(0, 1, 0);

    private float distance = 30.0f;

    private float yaw = 0.0f;
    private float pitch = 35.0f;

    private float fov = 60.0f;

    private final float nearPlane = 0.1f;
    private final float farPlane = 1000.0f;

    public Camera() {
        updatePosition();
    }

    private void updatePosition() {

        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float x = (float) (
                distance *
                        Math.cos(pitchRad) *
                        Math.sin(yawRad)
        );

        float y = (float) (
                distance *
                        Math.sin(pitchRad)
        );

        float z = (float) (
                distance *
                        Math.cos(pitchRad) *
                        Math.cos(yawRad)
        );

        position.set(
                target.x + x,
                target.y + y,
                target.z + z
        );
    }

    public void rotate(float deltaYaw, float deltaPitch) {

        yaw += deltaYaw;
        pitch += deltaPitch;

        pitch = Math.max(-85.0f, Math.min(85.0f, pitch));

        updatePosition();
    }


    public void zoom(float amount) {

        // Proportional zoom for smooth close-up navigation
        distance *= (float) Math.exp(-amount * 0.1f);

        distance = Math.max(
                0.3f,
                Math.min(100.0f, distance)
        );

        updatePosition();
    }


    public void reset() {

        distance = 30.0f;
        yaw = 0.0f;
        pitch = 35.0f;

        target.set(0, 0, 0);

        updatePosition();
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

    public Vector3f getPosition() {
        return new Vector3f(position);
    }

    public void setTarget(float x, float y, float z) {
        target.set(x, y, z);
        updatePosition();
    }

    public void setFov(float fov) {
        this.fov = Math.max(20.0f, Math.min(100.0f, fov));
    }


    public void setDistance(float newDistance) {

        distance = Math.max(
                0.3f,
                Math.min(100.0f, newDistance)
        );

        updatePosition();
    }

}
