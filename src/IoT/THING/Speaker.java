package IoT.THING;

import BuildingSimulator.Cell;
import IoT.THING.Scope.CircularScope;
import IoT.THING.Scope.ConicalScope;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class Speaker extends Thing implements drawableThing {

    private boolean isOn;
    //    private static final Color offColor = new Color(0.5f,0.5f,0.2f, 0.1f);
//    private static final Color onColor = new Color(1f,1f,0.7f, 0.7f);
    private static final Color offColor = new Color(0.5f,0.3f,0.3f);
    private static final Color onColor = new Color(0.7f,0.5f,0.5f);
    private static final Color centerColor = Color.red;
    public static final int onState = 1;
    public static final int offState = 0;
    private boolean isDrawChecked;
    private ArrayList<Integer> drawPixel;
    private boolean isChanged = true;
    private BufferedImage buffer;

    public Speaker(int x, int y, int r){

        super(new CircularScope(x, y, r));
        setType(Cell.Speaker);
        isOn = false;
        isDrawChecked = false;
    }

    public Speaker(int x, int y, int r, double angle){

        super(new ConicalScope(x, y, r, 0,360));
        setType(Cell.Speaker);
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
        actions.addAction(new SpeakerAction(this,false));
        ret.add(actions);

        actions = new ThingAction();
        actions.addAction(new SpeakerAction(this,true));
        ret.add(actions);
        return ret;
    }


    @Override
    public ArrayList<ThingAction> getAction(Integer state){
        ThingAction actions = new ThingAction();
        ArrayList<ThingAction> ret = new ArrayList<>();
        if (state == 1){
            actions.addAction(new SpeakerAction(this,false));
        }else{
            actions.addAction(new SpeakerAction(this,true));
        }
        ret.add(actions);
        return ret;
    }

    public boolean isOn(){
        return isOn;
    }

    private void countPixel(Cell[][] buildingCells, int buildingWidth, int buildingHeight){
        drawPixel = new ArrayList<>();
        int minX = scope.getX() - scope.getR();
        int minY = scope.getY() - scope.getR();
        int maxX = scope.getX() + scope.getR();
        int maxY = scope.getY() + scope.getR();
        // color cells in this box IF: (1) they are within R of (x,y) AND they are within "line of sight" of (x,y)
        for(int x = minX; x<=maxX; x++){
            if (x >= buildingWidth) break;
            for(int y = minY; y<maxY; y++) {
                if (y >= buildingHeight) break;
                if (isWithinR(x, y)) {
                    drawPixel.add(x);
                    drawPixel.add(y);
                }
            }
        }
    }

    @Override
    public void drawSelf(Graphics2D g2d, Cell[][] buildingCells, int buildingWidth, int buildingHeight){
        if (isChanged){
            if (!isDrawChecked){
                countPixel(buildingCells, buildingWidth, buildingHeight);
                isDrawChecked = true;
            }
            buffer = new BufferedImage(buildingWidth, buildingHeight, TYPE_INT_ARGB);
            Graphics2D buffer2d = buffer.createGraphics();
            // first draw the scope
            if (isOn == false){
                buffer2d.setColor(offColor);
            } else{
                buffer2d.setColor(onColor);
            }
            for (int i = 0; i < drawPixel.size(); i += 2){
                buffer2d.fillRect(drawPixel.get(i), drawPixel.get(i + 1), 1, 1);
            }
            buffer2d.setColor(centerColor);
            buffer2d.fill(new Ellipse2D.Double(scope.getX()-5, scope.getY()-5, 10, 10));
            buffer2d.dispose();
            isChanged = false;
        }
        if (isOn){
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.7f));
        }else{
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.2f));
        }
        g2d.drawImage(buffer, 0,0,null);
    }

    private boolean isWithinR(int x, int y){
        return (Point2D.distance(x,y, scope.getX(), scope.getY()) <= scope.getR());
    }


    public String toString(){
        String toReturn = "";
        toReturn += "Speaker: (";
        toReturn += scope.getX();
        toReturn += ", ";
        toReturn += scope.getY();
        toReturn += ")";
        toReturn += " is " + (isOn?"On":"Off");
        return toReturn;
    }
    public Action getFilter(boolean isTurnOn){
        return new SpeakerAction(null, isTurnOn);
    }
}
