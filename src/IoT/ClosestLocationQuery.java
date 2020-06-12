package IoT;

/**
 * Created by christinejulien on 8/29/17.
 */

import BuildingSimulator.Building;
import BuildingSimulator.Cell;
import IoT.THING.Thing;

import java.util.ArrayList;
import java.util.Iterator;
import java.awt.geom.Point2D;

public class ClosestLocationQuery extends Query{

    public ClosestLocationQuery(Building building){
        super(building);
    }

    @Override
    public Thing makeQuery(String type, int x, int y) {
        Thing closestSoFar = null;
        ArrayList<Cell> thingCells = building.getCellsByType(type);
        if(!thingCells.isEmpty()){
            Iterator<Cell> it = thingCells.iterator();
            closestSoFar = (Thing)it.next();
            double closestDistance = Point2D.distance(x, y, closestSoFar.getScope().getX(), closestSoFar.getScope().getY());
            while(it.hasNext()) {
                Thing next = (Thing) it.next();
                double d = Point2D.distance(x, y, next.getScope().getX(), next.getScope().getY());
                if (d < closestDistance) {
                    closestSoFar = next;
                    closestDistance = d;
                }
            }
        }
        return closestSoFar; // should be the closest thing of the requested type
    }
}
