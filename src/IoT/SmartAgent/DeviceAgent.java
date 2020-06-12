package IoT.SmartAgent;



import IoT.Context.AbstractContextArr;
import IoT.Context.DynamicContext;
import IoT.Context.LocationContext;
import IoT.THING.Action;
import IoT.THING.Speaker;
import IoT.THING.Thing;
import IoT.THING.ThingAction;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;

public class DeviceAgent extends AbstractAgent{
    private Thing device;
    ArrayList<StateWithEntropy> knownStates;
    boolean lastStateNew = false;
    StateWithEntropy chosenState;
    ThingAction chosenAction;
    AbstractContextArr lastContext;
    private final int longRetPraise = 10;
    private final int longRetPunish = -9;
    private final int longRetSmallPunish = -1;
    private boolean lastStateHelp = false;
    public void setDeviceUserAlpha(DynamicContext.Alpha deviceUserAlpha) {
        this.deviceUserAlpha = deviceUserAlpha;
    }

    public DynamicContext.Alpha getDeviceUserAlpha() {
        return deviceUserAlpha;
    }

    private DynamicContext.Alpha deviceUserAlpha;


    public DeviceAgent(Thing newDev) {
        device = newDev;
        knownStates = new ArrayList<StateWithEntropy>();
        deviceUserAlpha = DynamicContext.Alpha.defaultAlpha();
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
        ((DynamicContext)currContext).setAlpha(deviceUserAlpha);
        lastContext = currContext;
        lastStateNew = false;
        lastStateHelp = false;
        ArrayList<StateWithEntropy> helper = new ArrayList<>(3);
        chosenState = null;
        for (StateWithEntropy state: knownStates){
            if (state.isWithin((DynamicContext) currContext)){
                //chosenState = state.updateState(currState);
                chosenState = state;
                break;
            }
            if (helper.size() == 0) {
                helper.add(state);
            }else{
                int i = 0;
                for (i = 0; i < helper.size(); i++){
                    if (!state.betterHelpingThan((DynamicContext) currContext, helper.get(i))){
                        break;
                    }
                }
                if (i > 0){
                    helper.add(i, state);
                    if (helper.size() > 3){
                        helper.remove(0);
                    }
                }
            }
        }
        if (chosenState == null){
            chosenState = new StateWithEntropy((DynamicContext) currContext);
            chosenState.setMyAlpha(deviceUserAlpha);
            if (chosenState.getHelped(helper)){
                lastStateHelp = true;
            } else {
                ArrayList<ThingAction> allAction = device.getAction();
                for (ThingAction action: allAction){chosenState.addAction(action);}
//                chosenAction = chosenState.actions.get(0);
            }
            //knownStates.add(chosenState);
            lastStateNew = true;
        }
//        if ((device.getScope().getX() ==1140) && ((device.getScope().getY() == 200))){
//            System.out.println(helper);
//            System.out.println(chosenState);
//        }
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
            }
            chosenState.interactState((DynamicContext)lastContext, chosenAction, longRetPunish, knownStates);
//                if ((device.getScope().getX() == 1886) && (chosenAction.getActions().get(0).checkAction() == 0)) {
//                    int ctX = chosenState.getCentralContext().getX();
//                    int ctY = chosenState.getCentralContext().getY();
//                    if ((ctX < 1789) || (ctX > 1960) || (ctY < 91) || (ctY > 317)){
//                        System.out.println("In punish!!" + chosenAction + "  " + chosenAction.longRet + "   " + ctX + ", "+ ctY);
//                    }
//                }
        }
    }
    @Override
    public void getPraised() {
        if (chosenState != null){
            if (lastStateNew){
                knownStates.add(chosenState);
            }

            chosenState.interactState((DynamicContext)lastContext, chosenAction, longRetPraise, knownStates);
        }
    }

    @Override
    public void getSmallPunished(){
        if (chosenState != null){
            if (lastStateNew){
                knownStates.add(chosenState);
            }
            chosenState.interactState((DynamicContext)lastContext, chosenAction, longRetSmallPunish, knownStates);

        }
    }

    @Override
    public void interaction(AbstractContextArr currContext, KeyEvent e) {}

    @Override
    public void mouseCallback(MouseEvent e){}

    @Override
    public void paintNeed(Graphics g){
        Graphics2D g2d = (Graphics2D) g;
        for (StateWithEntropy knownState : knownStates){
            knownState.drawStateInfo(g2d);
        }

    }
}
