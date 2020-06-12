package IoT.THING;

import BuildingSimulator.Cell;

import java.awt.*;

public interface drawableThing {
    void drawSelf(Graphics2D g2d, Cell[][] buildingCells, int buildingWidth, int buildingHeight);
}
