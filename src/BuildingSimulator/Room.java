package BuildingSimulator;

public class Room implements Comparable {
    private int width;
    private int height;
    private int leftCornerX;
    private int leftCornerY;
    private int area;

    public Room(){
        width = 0;
        height = 0;
        leftCornerX = 0;
        leftCornerY = 0;
    }

    public int getArea(){
        return width * height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLeftCornerX() {
        return leftCornerX;
    }

    public int getLeftCornerY() {
        return leftCornerY;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setLeftCornerX(int leftCornerX) {
        this.leftCornerX = leftCornerX;
    }

    public void setLeftCornerY(int leftCornerY) {
        this.leftCornerY = leftCornerY;
    }

    @Override
    public int compareTo(Object o) {
        return 0;
    }
}
