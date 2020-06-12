package IoT.THING.Scope;


import java.awt.*;

public class ConicalScope extends PhysicalScope {
    private int x;
    private int y;
    private int radius;
    private int startAngle;
    private int endAngle;
    public ConicalScope(int x, int y, int radius, int startAngle, int endAngle){
        super();
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.startAngle = startAngle;
        this.endAngle= endAngle;
    }

    public boolean contains(int px, int py){

        double distance = new Point(x, y).distance(px, py);
        double crossP1 = Math.cos(Math.toRadians(startAngle)) * (double) (py-y) - Math.sin(Math.toRadians(startAngle)) * (double) (px-x);
        double crossP2 = Math.sin(Math.toRadians(endAngle)) * (double) (px-x) - Math.cos(Math.toRadians(endAngle)) * (double) (py-y);
        if (crossP1 >=0 && crossP2>=0 && distance<radius)
            return true;
        else
            return false;
    }

    public int getX(){
        return x;
    }

    public int getY() {
        return y;
    }

    public int getR() {
        return radius;
    }

}
