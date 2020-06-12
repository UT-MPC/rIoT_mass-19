package IoT.SmartAgent;



import IoT.Context.AbstractContextArr;
import IoT.Context.DynamicContext;
import IoT.THING.Action;
import IoT.THING.Thing;
import IoT.THING.ThingAction;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class DeviceAgentCpy extends AbstractAgent{
    private Thing device;
    ArrayList<AdaptiveSpaceTimeState> knownStates;
    boolean lastStateNew = false;
    AdaptiveSpaceTimeState chosenState;
    ThingAction chosenAction;
    AbstractContextArr lastContext;
    private final int longRetPraise = 10;
    private final int longRetPunish = -5;
    public DeviceAgentCpy(Thing newDev) {
        super();
        device = newDev;
        knownStates = new ArrayList<AdaptiveSpaceTimeState>();
    }

    public Thing getDevice() {
        return device;
    }

    public ThingAction getConfiguration(AbstractContextArr currContext, Class thingType){
        return getConfiguration(currContext, thingType, null);
    }

    public ThingAction getConfiguration(AbstractContextArr currContext, Class thingType, Action actionFilter) {
        if (!thingType.isInstance(device)){
            return null;
        }
        lastContext = currContext;
        lastStateNew = false;
        AdaptiveSpaceTimeState currState = new AdaptiveSpaceTimeState((DynamicContext) currContext);

        AdaptiveSpaceTimeState helper = null;
        chosenState = null;
        for (AdaptiveSpaceTimeState state: knownStates){
            if (state.isSame(currState)){
                //chosenState = state.updateState(currState);
                chosenState = state;
                break;
            }
            if (state.betterHelpingThan(currState, helper)){
                helper = state;
            }
        }
//        if ((device.getScope().getX() ==680) && ((device.getScope().getY() == 330))){
//            System.out.println("!!!!!!!!!!!!!!!!!! L001 " + chosenState);
//        }
        if (chosenState == null){
            chosenState = currState;
            if (chosenState.getHelped(helper)){

            } else {
                ArrayList<ThingAction> allAction = device.getAction();
                for (ThingAction action: allAction){chosenState.addAction(action);}
//                chosenAction = chosenState.actions.get(0);
            }
            //knownStates.add(chosenState);
            lastStateNew = true;
        }
        chosenAction = chosenState.getAction(device.getDeviceState(), actionFilter);
//        System.out.println(chosenAction + "  "+ chosenAction.longRet);
        return chosenAction;
    }
    @Override
    public void getPunished() {
        if (chosenState != null){
            chosenAction.undoAction();
            if (lastStateNew){
                knownStates.add(chosenState);
                chosenAction.longRet += longRetPunish;
            }else{
                if (chosenAction.longRet < 4){
                    chosenAction.longRet += longRetPunish;
                    chosenState.updateState(new AdaptiveSpaceTimeState((DynamicContext) lastContext));
                }else{
                    AdaptiveSpaceTimeState newState = chosenState.splitState(lastContext, chosenAction, longRetPunish);
                    if (newState != null){
                        knownStates.add(newState);
                    }
                }
            }
        }

    }
    @Override
    public void getPraised() {

        if (chosenState != null){
            if (lastStateNew){
                knownStates.add(chosenState);
                chosenAction.longRet += longRetPraise;

            }else{
                if (chosenAction.longRet > -4){
                    chosenAction.longRet += longRetPraise;
                    chosenState.updateState(new AdaptiveSpaceTimeState((DynamicContext) lastContext));
                }else{
                    AdaptiveSpaceTimeState newState = chosenState.splitState(lastContext, chosenAction, longRetPraise);
                    if (newState != null){
                        knownStates.add(newState);
                    }
                }
            }
        }
        chosenAction = null;
        chosenState = null;
    }

    @Override
    public void interaction(AbstractContextArr currContext, KeyEvent e) {}

    @Override
    public void mouseCallback(MouseEvent e){}

    @Override
    public void paintNeed(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        for (AdaptiveSpaceTimeState knownState : knownStates){
            knownState.drawStateInfo(g2d);
        }
    }
    @Override
    public void getSmallPunished(){}

}
