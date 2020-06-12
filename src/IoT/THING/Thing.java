package IoT.THING;


import BuildingSimulator.Cell;
import IoT.THING.Scope.PhysicalScope;

import java.util.ArrayList;

public abstract class Thing extends Cell {
    protected PhysicalScope scope;
    protected boolean penetratesWalls;

    public Thing(PhysicalScope scope){
        this.scope = scope;
        penetratesWalls = false;
        setType(Cell.THING);
    }

    public Thing(PhysicalScope scope, boolean penetratesWalls){
        this.scope = scope;
        this.penetratesWalls = penetratesWalls;
    }

    public PhysicalScope getScope(){ return scope;}
    public boolean getPenetration(){ return penetratesWalls; }
    public void reset(){

    }
    public abstract Integer getDeviceState();
    public abstract ArrayList<ThingAction> getAction(Integer state);
    public abstract ArrayList<ThingAction> getAction();
}
