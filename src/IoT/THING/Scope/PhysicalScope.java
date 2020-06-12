package IoT.THING.Scope;

public abstract class PhysicalScope {

    public PhysicalScope(){

    }

    public abstract boolean contains(int x, int y);

    public int getX(){ return 0;}
    public int getY(){ return 0;}
    public int getR(){ return 0;}
    public int getMinX(){ return 0;}
    public int getMinY(){ return 0;}
    public int getMaxX(){ return 0;}
    public int getMaxY(){ return 0;}
}
