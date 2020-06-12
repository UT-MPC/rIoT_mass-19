package IoT;

import BuildingSimulator.Building;
import IoT.THING.Thing;

public class SimpleQuery extends Query{

    public SimpleQuery(Building building){
        super(building);
    }

    @Override
    public Thing makeQuery(String type, int x, int y) {
        return null; // TODO: BAD!
    }
}
