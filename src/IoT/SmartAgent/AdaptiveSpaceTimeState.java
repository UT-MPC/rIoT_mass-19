package IoT.SmartAgent;

import IoT.Context.AbstractContextArr;
import IoT.Context.DynamicContext;
import IoT.THING.Action;
import IoT.THING.ThingAction;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Collections;

/*
    Compare to SpaceTimeDeviceState. This state uses square shape to describe a state in the context space.
    Adaptive means that it can change its size and border in response to the user interaction so that the physic scope
    of the devices can captured during the usage of the devices.
    Ideally, this kind of state will be large at very sure space like far away and small at border. This method can also
    control the memory used in the OneAgent system.
 */
public class AdaptiveSpaceTimeState extends State {
    public ArrayList<ThingAction> actions;
//    public int deviceState;
    private int defaultThreshold = 20;
    private int stateDensity = 0;
    private DynamicContext massContext;
    private DynamicContext minContext;
    private DynamicContext maxContext;

    public AdaptiveSpaceTimeState(DynamicContext minContext, DynamicContext maxContext, DynamicContext massContext, int stateDensity) {
        actions = new ArrayList<ThingAction>();
        this.minContext = minContext;
        this.maxContext = maxContext;
        this.massContext = massContext;
//        this.deviceState = deviceState;
        this.stateDensity = stateDensity;
    }

    public AdaptiveSpaceTimeState(DynamicContext curr) {
        actions = new ArrayList<ThingAction>();
        minContext = curr.getOffsetContext(-defaultThreshold);
        maxContext = curr.getOffsetContext(defaultThreshold);
        massContext = curr.getOffsetContext(0);
//        this.deviceState = deviceState;
        stateDensity = 1;
    }

    public DynamicContext getCentralContext() {
        return minContext.getMidContext(maxContext, 0.5);
    }

    public double distanceToState(AdaptiveSpaceTimeState bState) {
        return getCentralContext().distanceTo(bState.getCentralContext());

    }

    public ThingAction getAction(int thingState, Action filter){
        Collections.sort(actions, Collections.reverseOrder());
        for (int i = 0; i < actions.size(); i++){
            if ((actions.get(i).isStateCompatible(thingState)) && (actions.get(i).actionFilter(filter))){
                return actions.get(i);
            }
        }
        return null;
    }
    public boolean getHelped(AdaptiveSpaceTimeState helper) {
        if (helper == null) return false;
        Collections.sort(helper.actions, Collections.reverseOrder());
        actions = new ArrayList<ThingAction>(helper.actions.size());
        for (ThingAction action : helper.actions){
            ThingAction newAction =new ThingAction(action.getActions());
            double discount = this.distanceToState(helper) / defaultThreshold + 1;
            newAction.longRet = action.longRet / discount;
            newAction.shortRet = action.shortRet / discount;
            actions.add(newAction);
        }
        return true;
    }

    public void addAction(ThingAction newAction){
        actions.add(newAction);
    }

    public boolean betterHelpingThan(AdaptiveSpaceTimeState toState, AdaptiveSpaceTimeState thanState){
//        if (deviceState != toState.deviceState){
//            return false;
//        }
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
    public AdaptiveSpaceTimeState updateState(AdaptiveSpaceTimeState bState){
        double fraction = (double) stateDensity / ((double)stateDensity + (double)bState.stateDensity);
        massContext = massContext.getMidContext(bState.massContext, fraction);
        stateDensity += bState.stateDensity;
        return this;
    }
    public boolean isSame(AdaptiveSpaceTimeState bState) {
//        if (bState.deviceState != deviceState){
//            return false;
//        }
        return bState.getCentralContext().isBetween(minContext, maxContext);
    }
    public void drawStateInfo(Graphics2D g2d){
        Color onColor = new Color(0f, 1f,0f, 0.3f);
        Color offColor = new Color(0.1f, 0f,0.7f, 0.1f);
        Color unknownColor = new Color(0f, 0.5f,0f, 0.05f);
        Color massColor = new Color(0f, 0.5f,0f, 1f);
        double score = actions.get(0).longRet;
        if (score == 0){
            g2d.setColor(unknownColor);
        }
        if (score > 0){
//            if (deviceState == 0){
                g2d.setColor(onColor);
//            }else{
//                g2d.setColor(offColor);
//            }
        }
        if (score < 0){
//            if (deviceState == 0){
                g2d.setColor(offColor);
//            }else{
//                g2d.setColor(onColor);
//            }
        }
        g2d.fillRect(minContext.getX(), minContext.getY(), maxContext.getX()-minContext.getX(), maxContext.getY()-minContext.getY() );
        g2d.setColor(massColor);
        g2d.fill(new Ellipse2D.Double(massContext.getX()-2, massContext.getY()-2, 4, 4));
    }

    public AdaptiveSpaceTimeState splitState(AbstractContextArr lastContext, ThingAction lastAction, int newActionRet){
        DynamicContext nowContext = (DynamicContext) lastContext;
        AdaptiveSpaceTimeState lastState = new AdaptiveSpaceTimeState(nowContext);
        if ( nowContext.distanceTo(massContext) < 5){
            lastAction.longRet += newActionRet;
            return null;
        }else{
            DynamicContext contextHolder[];
            double ratio = stateDensity;
            contextHolder = DynamicContext.stateSpliter(minContext, maxContext, nowContext, massContext, 1/ ratio);
            AdaptiveSpaceTimeState newState = new AdaptiveSpaceTimeState(contextHolder[0], contextHolder[1], nowContext,  1);
            for (ThingAction agentAction :actions){
                if (lastAction != agentAction) {
                    newState.addAction(new ThingAction(agentAction));
                }else {
                    ThingAction falseAction = new ThingAction(lastAction);
                    falseAction.longRet = newActionRet;
                    newState.addAction(falseAction);
                }
            }
            return newState;
        }
    }
}
