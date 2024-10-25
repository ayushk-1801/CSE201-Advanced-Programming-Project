package io.github.angrybirds;

public class Material {
    private int weight;
    private int height;
    private int width;
    private int resistance;

    public int getWeight() {
        return weight;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getResistance() {
        return resistance;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setResistance(int resistance) {
        this.resistance = resistance;
    }

    public void breakMaterial() {
        // Break the material
    }
}
