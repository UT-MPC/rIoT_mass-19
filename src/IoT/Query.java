package IoT;

import BuildingSimulator.Building;
import IoT.THING.Thing;

public abstract class Query {
    protected Building building;

    public Query(Building building){
        this.building = building;
    }

    public abstract Thing makeQuery(String type, int x, int y); // TODO refactor to not require location for every query!

}
