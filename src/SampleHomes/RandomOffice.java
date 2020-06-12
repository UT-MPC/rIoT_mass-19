package SampleHomes;

import BuildingSimulator.Office;
import BuildingSimulator.OfficeGenerator;

public class RandomOffice extends Office {
    int minLights;
    int maxLights;

    public RandomOffice(int width, int height, int minLights, int maxLights){
        super(width, height);
        this.minLights = minLights;
        this.maxLights = maxLights;
        generate();
    }

    @Override
    public void generate() {
        OfficeGenerator g = new OfficeGenerator(getWidth(), getHeight(), minLights, maxLights);
        setCells(g.getCells());
    }
}
