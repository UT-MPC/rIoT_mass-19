package IoT;

import BuildingSimulator.Building;
import BuildingSimulator.Cell;
import IoT.THING.Thing;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by christinejulien on 8/29/17.
 */
public class PhysicalScopeQuery extends Query{

    public PhysicalScopeQuery(Building building){
        super(building);
    }

    @Override
    public Thing makeQuery(String type, int x, int y) {
        //TODO: Make more sophisticated to account for walls!

        Thing toReturn = null;
        double toReturnDistance = Double.MAX_VALUE;
        ArrayList<Cell> thingCells = building.getCellsByType(type);
        if(!thingCells.isEmpty()){
            Iterator<Cell> it = thingCells.iterator();
            while(it.hasNext()) {
                Thing next = (Thing) it.next();
                if (next.getScope().contains(x, y)) {
                    double d = Point2D.distance(x, y, next.getScope().getX(), next.getScope().getY());
                    if (d < toReturnDistance) {
                        toReturnDistance = d;
                        toReturn = next;
                    }
                }
            }
        }
        return toReturn; // should be the the closest thing of the requested type whose physical scope covers the actor
    }
}

