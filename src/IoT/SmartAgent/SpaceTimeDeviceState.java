package IoT.SmartAgent;

import IoT.Context.SimpleSpaceTimeContext;
import IoT.THING.ThingAction;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;
@Deprecated
public class SpaceTimeDeviceState extends State {
    public ArrayList<ThingAction> actions;
    public int x, y, time;
    public int deviceState;
    private int threshold = 1000;
    private int stateDensity = 0;
    public SpaceTimeDeviceState(SimpleSpaceTimeContext curr, int deviceState) {
        actions = new ArrayList<ThingAction>();
        x = curr.getX();
        y = curr.getY();
        time = curr.timeOfDay;
        this.deviceState = deviceState;
        stateDensity = 1;
    }
    public boolean getHelped(SpaceTimeDeviceState helper) {
        if (helper == null) return false;
        if (this.deviceState != helper.deviceState) return false;
        Collections.sort(helper.actions, Collections.reverseOrder());
        actions = new ArrayList<ThingAction>(helper.actions.size());
        for (ThingAction action : helper.actions){
            ThingAction newAction =new ThingAction(action.getActions());
            int discount = this.distanceToState(helper) / threshold + 1;
            newAction.longRet = action.longRet / discount;
            newAction.shortRet = action.shortRet / discount;
            actions.add(newAction);
        }
        return true;
    }
    /*
        The distance is in square order
     */
    public int distanceToState(SpaceTimeDeviceState bState) {
        int timeDifference = (Math.abs(time - bState.time)) / 360;
        int spaceDifference = (x - bState.x) * (x - bState.x) + (y - bState.y) * (y - bState.y);
        return spaceDifference + timeDifference;
    }
    public void addAction(ThingAction newAction){
        actions.add(newAction);
    }

    public boolean isSame(SpaceTimeDeviceState bState) {
        if (bState.deviceState != deviceState){
            return false;
        }
        //System.out.println(distanceToState(bState));
        if (distanceToState(bState) < threshold) {
            return true;
        }
        return false;
    }
    public SpaceTimeDeviceState updateState(SpaceTimeDeviceState bState){
        this.x = (this.x * stateDensity + bState.x) / (stateDensity + 1);
        this.y = (this.y * stateDensity + bState.y) / (stateDensity + 1);
        this.time = (this.time * stateDensity + bState.time) / (stateDensity + 1);
        stateDensity += 1;
        return this;
    }

    public int getRadius() {
        return (int) Math.sqrt((double) threshold);
    }

    public boolean betterHelpingThan(SpaceTimeDeviceState toState, SpaceTimeDeviceState thanState){
        Collections.sort(this.actions, Collections.reverseOrder());
        double myRet = this.actions.get(0).longRet;
        if (myRet <= 0){
            return false;
        }
        if (thanState == null){
            return true;
        }
        Collections.sort(thanState.actions, Collections.reverseOrder());
        double hisRet = thanState.actions.get(0).longRet;
        if (thanState.distanceToState(toState) * myRet > this.distanceToState(toState) * hisRet){
            return true;
        }else{
            return false;
        }
    }

    public void drawStateInfo(Graphics2D g2d){
        Color onColor = new Color(0f, 1f,0f, 0.3f);
        Color offColor = new Color(0.1f, 0f,0.7f, 0.1f);
        Color unknownColor = new Color(0f, 0.5f,0f, 0.05f);
        int r = this.getRadius();
        double confidence = actions.get(0).longRet;
        if (confidence == 0){
            g2d.setColor(unknownColor);
        }
        if (confidence > 0){
            if (deviceState == 0){
                g2d.setColor(onColor);
            }else{
                g2d.setColor(offColor);
            }
        }
        if (confidence < 0){
            if (deviceState == 0){
                g2d.setColor(offColor);
            }else{
                g2d.setColor(onColor);
            }
        }
        g2d.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }
}