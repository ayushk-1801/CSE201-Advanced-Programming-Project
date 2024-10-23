package io.github.angrybirds;

public class Pig {
    private int health;
    private int weight;

    public Pig(){};
    public Pig(int health, int weight){
        this.health = health;
        this.weight = weight;
    }

    public int getHealth() {
        return health;
    }

    public int getWeight() {
        return weight;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
