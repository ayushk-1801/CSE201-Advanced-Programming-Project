package io.github.angrybirds;

import java.util.List;

public class Level {
    private int number;
    private int score;
    private boolean isCompleted;

    private List<Bird> birds;
    private List<Pig> pigs;
    private List<Material> materials;
    private List<Powerup> powerups;
    private Slingshot slingshot;

    public int getNumber() {
        return number;
    }

    public int getScore() {
        return score;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public List<Bird> getBirds() {
        return birds;
    }

    public List<Pig> getPigs() {
        return pigs;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public List<Powerup> getPowerups() {
        return powerups;
    }

    public Slingshot getSlingshot() {
        return slingshot;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void setBirds(List<Bird> birds) {
        this.birds = birds;
    }

    public void setPigs(List<Pig> pigs) {
        this.pigs = pigs;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    public void setPowerups(List<Powerup> powerups) {
        this.powerups = powerups;
    }

    public void setSlingshot(Slingshot slingshot) {
        this.slingshot = slingshot;
    }

    public void start() {
        // Start the level
    }

    public void pause() {
        // Pause the level
    }

    public void resume() {
        // Resume the level
    }

    public void complete() {
        // Complete the level
    }
}
