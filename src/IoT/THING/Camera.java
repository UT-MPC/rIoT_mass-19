package IoT.THING;

import BuildingSimulator.Cell;
import IoT.THING.Scope.ConicalScope;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class Camera extends Thing implements drawableThing {
    private boolean isOn;
    private static final Color offColor = new Color(0.2f,0.5f,0.5f);
    private static final Color onColor = new Color(0.7f,1f,0.7f);
    private static final Color centerColor = Color.green;
    public static final int onState = 1;
    public static final int offState = 0;

    private boolean isDrawChecked;
    private ArrayList<Integer> drawPixel;
    private boolean isChanged = true;
    private BufferedImage buffer;
    public Camera(int x, int y, int r, int startAngle, int endAngle){

        super(new ConicalScope(x, y, r, startAngle, endAngle));
        setType(Cell.Camera);
        isOn = false;
    }

    public void turnOff(){
        if (isOn) isChanged = true;
        isOn = false;
    }

    public void turnOn(){
        if (!isOn) isChanged = true;
        isOn = true;
    }

    @Override
    public void reset(){
        if (isOn) isChanged = true;
        turnOff();
    }

    @Override
    public Integer getDeviceState(){
        return isOn?onState:offState;
    }

    @Override
    public ArrayList<ThingAction> getAction(){
        ThingAction actions = new ThingAction();
        ArrayList<ThingAction> ret = new ArrayList<>();
        actions.addAction(new CameraAction(this,false));
        ret.add(actions);

        actions = new ThingAction();
        actions.addAction(new CameraAction(this,true));
        ret.add(actions);
        return ret;
    }

    @Override
    public ArrayList<ThingAction> getAction(Integer state){
        ThingAction actions = new ThingAction();
        ArrayList<ThingAction> ret = new ArrayList<>();

        if (state == 1){
            actions.addAction(new CameraAction(this,false));
        }else{
            actions.addAction(new CameraAction(this,true));
        }
        ret.add(actions);
        return ret;
    }

    public boolean isOn(){
        return isOn;
    }

    @Override
    public void drawSelf(Graphics2D g2d, Cell[][] buildingCells, int buildingWidth, int buildingHeight){
        if (isChanged){
            buffer = new BufferedImage(buildingWidth, buildingHeight, TYPE_INT_ARGB);
            Graphics2D buffer2d = buffer.createGraphics();
            // first draw the scope
            if (isOn == false){
                buffer2d.setColor(offColor);
            } else{
                buffer2d.setColor(onColor);
            }
            int minX = scope.getX() - scope.getR();
            int minY = scope.getY() - scope.getR();
            int maxX = scope.getX() + scope.getR();
            int maxY = scope.getY() + scope.getR();
            // color cells in this box IF: (1) they are within R of (x,y) AND they are within "line of sight" of (x,y)
            for(int x = minX; x<=maxX; x++){
                for(int y = minY; y<maxY; y++) {
                    if (scope.contains(x, y)  && isLineOfSight(x, y, buildingCells, buildingWidth, buildingHeight) && x < buildingWidth && y < buildingHeight) {
                        buffer2d.fillRect(x, y, 1, 1);
                    }
                }
            }
            buffer2d.setColor(centerColor);
            buffer2d.fill(new Ellipse2D.Double(scope.getX()-5, scope.getY()-5, 10, 10));
            buffer2d.dispose();
            isChanged = false;
        }
        if (isOn){
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.7f));
        }else{
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.1f));
        }
        g2d.drawImage(buffer, 0,0,null);
    }

    private boolean isLineOfSight(int x, int y, Cell[][] cells, int buildingWidth, int buildingHeight){
        // this is basically an implementation of Bresenham's algorithm; I'm basing this on source code from here:
        // http://www.sanfoundry.com/java-program-bresenham-line-algorithm/

        int dx = Math.abs(scope.getX() - x);
        int dy = Math.abs(scope.getY() - y);

        int sx = x < scope.getX() ? 1 : -1;
        int sy = y < scope.getY() ? 1 : -1;

        int err = dx-dy;
        int e2;

        while (true) {

            // x,y is one point on the line
            // we now just need to check to see if any of this location in cells is a wall
            if(x < 0 || y < 0 || x >= buildingWidth || y >= buildingHeight){
                break;
            }

            if (x == scope.getX() && y == scope.getY()) {
                break;
            }
            if(cells[x][y].getType() == Cell.WALL){
                return false;
            }

            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x = x + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y = y + sy;
            }
        }

        // at this point, we went through all of the points connecting the light to our location x,y; if none were walls, return true
        return true;
    }
    public String toString(){
        String toReturn = "";
        toReturn += "Camera: (";
        toReturn += scope.getX();
        toReturn += ", ";
        toReturn += scope.getY();
        toReturn += ")";
        return toReturn;
    }
    public Action getFilter(boolean isTurnOn){
        return new CameraAction(null, isTurnOn);
    }
}
