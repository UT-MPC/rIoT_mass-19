package IoT.THING.Scope;

import java.awt.geom.Ellipse2D;

public class EllipseScope extends PhysicalScope {
    private Ellipse2D circle;
    public EllipseScope(int x, int y, int rx, int ry){
        circle = new Ellipse2D.Double(x-rx,y-ry,rx * 2,ry * 2);
    }

    public boolean contains(int x, int y){
        return circle.contains(x,y);
    }

    @Override
    public int getX(){ return (int)circle.getCenterX(); }

    @Override
    public int getY(){ return (int)circle.getCenterY(); }

    @Override
    public int getMinX() { return (int)circle.getMinX(); }

    @Override
    public int getMinY() { return (int)circle.getMinY(); }

    @Override
    public int getMaxX() { return (int)circle.getMaxX(); }

    @Override
    public int getMaxY() { return (int)circle.getMaxY(); }
}
