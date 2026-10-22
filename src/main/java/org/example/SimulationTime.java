
package org.example;

public class SimulationTime {

    private float simulationTime = 0.0f;
    private float timeScale = 1.0f;
    private boolean paused = false;

    public void update(float deltaTime) {
        if (!paused) {
            simulationTime += deltaTime * timeScale;
        }
    }

    public void togglePause() {
        paused = !paused;
    }

    public void increaseSpeed() {
        timeScale = Math.min(100.0f, timeScale * 2.0f);
    }

    public void decreaseSpeed() {
        timeScale = Math.max(0.125f, timeScale / 2.0f);
    }

    public void reset() {
        simulationTime = 0.0f;
        timeScale = 1.0f;
        paused = false;
    }

    public float getTime() {
        return simulationTime;
    }

    public float getTimeScale() {
        return timeScale;
    }

    public boolean isPaused() {
        return paused;
    }
}

