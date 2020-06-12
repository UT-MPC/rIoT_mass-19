package SampleHomes;

import BuildingSimulator.House;
import BuildingSimulator.HouseGenerator;

public class RandomHouse extends House {

    public RandomHouse(int width, int height){
        super(width, height);
    }

    @Override
    public void generate() {
        HouseGenerator g = new HouseGenerator(getWidth(), getHeight());
        setCells(g.getCells());
    }
}
