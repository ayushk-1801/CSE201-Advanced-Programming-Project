package io.github.angrybirds;

public abstract class Powerup {
    private int duration;

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    abstract void activatePowerup();
}
