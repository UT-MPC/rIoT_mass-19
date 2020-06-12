package IoT.Context;

public class Point2D {
    public int x,y;
    public Point2D(Point2D B){
        this.x = B.x;
        this.y = B.y;
    }
    public Point2D(int x, int y){
        this.x = x;
        this.y = y;
    }
    public int distance(Point2D pointB){
        return  (int) Math.sqrt((x - pointB.x) * (x - pointB.x) + (y - pointB.y) * (y - pointB.y));
    }
    /*
        isBetween is to test if this point is in the square formed by A,B
     */
    public boolean isBetween(Point2D A, Point2D B){
        if ((x < A.x) || (x > B.x)){
            return false;
        }
        if ((y < A.y) || (y > B.y)){
            return false;
        }
        return true;
    }
}
