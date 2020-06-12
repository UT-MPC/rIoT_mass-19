package IoT.THING.Scope;

import java.awt.Point;

public class CircularScope extends PhysicalScope {
    private int centerX;
    private int centerY;
    private int radius;

    public CircularScope(int x, int y, int r){
        centerX = x;
        centerY = y;
        radius = r;
    }

    public boolean contains(int x, int y){
        double distance = new Point(x, y).distance(centerX, centerY);
        return (distance <= radius);
    }

    public int getX(){ return centerX; }

    public int getY(){ return centerY; }

    public int getR(){ return radius; }
}
