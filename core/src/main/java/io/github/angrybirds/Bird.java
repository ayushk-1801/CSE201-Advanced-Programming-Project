package io.github.angrybirds;

public abstract class Bird {
    private int weight;
    private int velocity;
    private int impactRadius;
    private boolean isAbilityActive;

    public Bird() {
        this.weight = 0;
        this.velocity = 0;
        this.impactRadius = 0;
        this.isAbilityActive = false;
    }

    public int getWeight() {
        return weight;
    }

    public int getVelocity() {
        return velocity;
    }

    public int getImpactRadius() {
        return impactRadius;
    }

    public boolean isAbilityActive() {
        return isAbilityActive;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public void setImpactRadius(int impactRadius) {
        this.impactRadius = impactRadius;
    }

    public void setAbilityActive(boolean abilityActive) {
        isAbilityActive = abilityActive;
    }

    abstract void activateAbility();
}
