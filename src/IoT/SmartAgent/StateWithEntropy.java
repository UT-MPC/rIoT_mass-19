package IoT.SmartAgent;

import IoT.Context.DynamicContext;
import IoT.THING.Action;
import IoT.THING.Thing;
import IoT.THING.ThingAction;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class StateWithEntropy extends State {
    private static class InteractionSnapshot{
        public ThingAction action;
        public DynamicContext context;

    }
    public ArrayList<ThingAction> actions;
    //    public int deviceState;
//    private final double defaultSize = 0.02;
    private final double defaultSize = 0.02;

    private double splitEntropyThreshold = 0.8;
    private double refreshThreshold = 0.2;
    private double minEntropyGain = 0.3;
    private int splitSizeThreshold = 15;
    private final int maxShots = 40;

    private DynamicContext minContext;
    private DynamicContext maxContext;

    protected ArrayList<InteractionSnapshot> interactionShots;
    private DynamicContext.Alpha myAlpha;


    public StateWithEntropy(DynamicContext curr) {
        interactionShots = new ArrayList<>();
        actions = new ArrayList<ThingAction>();
        minContext = curr.getOffsetContext(-defaultSize);
        maxContext = curr.getOffsetContext(defaultSize);
    }

    public StateWithEntropy(DynamicContext minContext, DynamicContext maxContext){
        this.minContext = new DynamicContext(minContext);
        this.maxContext = new DynamicContext(maxContext);
        actions = new ArrayList<>();
        interactionShots = new ArrayList<>();
    }

    public DynamicContext getCentralContext() {
        return minContext.getMidContext(maxContext, 0.5);
    }

    public double distanceToState(StateWithEntropy bState) {
        return getCentralContext().distanceTo(bState.getCentralContext());

    }

    public double distanceToContext(DynamicContext bContext) {
        return getCentralContext().distanceTo(bContext);

    }

    public boolean isWithin(DynamicContext bContext){
        return bContext.isBetween(minContext, maxContext);
    }

    public void addAction(ThingAction newAction){
        actions.add(newAction);
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

    public void setMyAlpha (DynamicContext.Alpha alphaObj){
        myAlpha = alphaObj;
    }

    public DynamicContext.Alpha getMyAlpha (){
        return myAlpha;
    }
    public boolean getHelped(ArrayList<StateWithEntropy> helper) {
        if (helper.size() == 0) return false;
        actions = new ArrayList<ThingAction>(helper.get(0).actions.size());
        ArrayList<Integer> actionCnt = new ArrayList<>();
        for (ThingAction action : helper.get(helper.size() - 1).actions){
            ThingAction newAction =new ThingAction(action.getActions());
            double discount = this.distanceToState(helper.get(helper.size() -1 )) / defaultSize + 1;
//            NumberFormat formatter = new DecimalFormat("#0.00000000");
//            System.out.println(formatter.format(this.distanceToState(helper.get(helper.size() -1 ))) + "   "  + formatter.format(defaultSize ));
            newAction.longRet = action.longRet / discount;
            newAction.shortRet = action.shortRet / discount;
            actions.add(newAction);
            actionCnt.add(1);
        }
        for (int i = helper.size() - 2; i >=0; i--){
            for (ThingAction action : helper.get(i).actions){
                ThingAction newAction =new ThingAction(action.getActions());
                double discount = this.distanceToState(helper.get(i)) / defaultSize + 1;
                newAction.longRet = action.longRet / discount;
                newAction.shortRet = action.shortRet / discount;
                for (int j = 0; j <actions.size(); j++){
                    if (actions.get(j).isSame(newAction)){
                        actionCnt.set(j, actionCnt.get(j) + 1);
                        actions.get(j).longRet += newAction.longRet;
                        actions.get(j).shortRet += newAction.shortRet;
                        break;
                    }
                }
            }
        }
        for (int j = 0; j <actions.size(); j++){
            actions.get(j).longRet = actions.get(j).longRet / actionCnt.get(j);
            if (actions.get(j).longRet > 10){
                actions.get(j).longRet = 10;
            }
            actions.get(j).shortRet = actions.get(j).shortRet / actionCnt.get(j);
        }
        return true;
    }

    public boolean betterHelpingThan(DynamicContext toContext, StateWithEntropy thanState){
//        return false;
        Collections.sort(this.actions, Collections.reverseOrder());
        if (thanState == null){
            return true;
        }
        if (thanState.distanceToContext(toContext)  > this.distanceToContext(toContext)){
            return true;
        }else{
            return false;
        }
    }

    private double calcE(double c0, double c1){
        if ((c0 < 0.0001)||(c1 < 0.0001)) return 0;
        double p0 = c0 / (c0 + c1);
        double p1 = c1 / (c0 + c1);
        return (-Math.log(p0) * p0 -Math.log(p1) * p1) / Math.log(2);
    }
    private double stateEntropy(int start, int last, ThingAction lastAction){
        double c0 = 0;
        double c1 = 0;
        for (int i = start; i <= last; i++){
            if (interactionShots.get(i).action.isSame(lastAction)){
                if (interactionShots.get(i).action.longRet > 0){
                    c0 += 1;
                }else{
                    c1 += 1;
                }
            }
        }
//        System.out.println(c0  + "  " + c1);
        if((c0 + c1) < splitSizeThreshold) return -1;
        return calcE(c0, c1);
    }
    private int findStart(int num, ThingAction lastAction){
        double c0 = 0;
        double c1 = 0;
        for (int i = interactionShots.size()-1; i >= 0; i--){
            if (interactionShots.get(i).action.isSame(lastAction)){
                if (interactionShots.get(i).action.longRet > 0){
                    c0 += 1;
                }else{
                    c1 += 1;
                }
            }
            if (c0+c1 > num) return i;
        }
        return 0;
    }

    private void splitState(ThingAction lastAction, ArrayList<StateWithEntropy> statePool){
        DynamicContext[] optimalSplit = null;
        double entropyGain = minEntropyGain;
        double parentEntropy = stateEntropy(0, interactionShots.size()-1, lastAction);
        ArrayList<DynamicContext[]> maxMinPairs = new ArrayList<>();
        for (double ratio = 0.1; ratio < 1; ratio += 0.1){
            List<DynamicContext[]> newSplits = DynamicContext.splitPoints(minContext, maxContext, ratio);
            if (newSplits != null){
                maxMinPairs.addAll(newSplits);
            }
        }
        for (DynamicContext[] pair: maxMinPairs){
            double c00 = 0, c01 = 0, c10 = 0, c11 = 0;
            for (InteractionSnapshot shot: interactionShots) {
                if (shot.action.isSame(lastAction)) {
                    if (shot.context.isBetween(pair[0], pair[1])) {
                        if (shot.action.longRet > 0) {
                            c00 += 1;
                        } else {
                            c01 += 1;
                        }
                    } else {
                        if (shot.context.isBetween(pair[2], pair[3])) {
                            if (shot.action.longRet > 0) {
                                c10 += 1;
                            } else {
                                c11 += 1;
                            }
                        }
                    }
                }
            }
            double e0 = calcE(c00, c01), e1 = calcE(c10, c11);
            if (parentEntropy - (e0 + e1) > entropyGain){
                entropyGain = parentEntropy - (e0 + e1);
                optimalSplit = pair;
            }
        }

        //Split the state based on the optimal split points.
        if (optimalSplit == null){
            return;
        }
        StateWithEntropy newState = new StateWithEntropy(optimalSplit[2], optimalSplit[3]);
        minContext = optimalSplit[0];
        maxContext = optimalSplit[1];
        double myTot = 0, hisTot = 0;
        double mycnt = 0, hiscnt = 0;
        ArrayList<InteractionSnapshot> myShots = new ArrayList<>();
        ArrayList<InteractionSnapshot> hisShots = new ArrayList<>();
        for (InteractionSnapshot shot: interactionShots) {
            if (shot.context.isBetween(minContext, maxContext)) {
                if (shot.action.isSame(lastAction)) {
                    myTot += shot.action.longRet;
                    mycnt += 1;
                }
                myShots.add(shot);
            }else {
                if (shot.action.isSame(lastAction)) {
                    hisTot += shot.action.longRet;
                    hiscnt += 1;
                }
                hisShots.add(shot);
            }
        }
        myTot = myTot / mycnt;
        hisTot = hisTot / hiscnt;
        for (ThingAction agentAction :actions){
            if (!agentAction.isSame(lastAction)) {
                newState.addAction(new ThingAction(agentAction));
            }else {
                agentAction.longRet = myTot;
                ThingAction falseAction = new ThingAction(lastAction);
                falseAction.longRet = hisTot;
                newState.addAction(falseAction);
            }
        }
        interactionShots = myShots;
        newState.interactionShots = hisShots;
        statePool.add(newState);
    }


    public void interactState(DynamicContext bContext, ThingAction lastAction, double adjustScore, ArrayList<StateWithEntropy> statePool){
        InteractionSnapshot newShot = new InteractionSnapshot();
        newShot.context = bContext;
        lastAction.longRet += adjustScore;
        newShot.action = new ThingAction(lastAction);
        newShot.action.longRet = adjustScore;
        interactionShots.add(newShot);
        if (interactionShots.size() > maxShots){
            interactionShots.remove(0);
        }
        double entropy = stateEntropy(0, interactionShots.size()-1, lastAction);
        int latestStart = findStart(splitSizeThreshold,lastAction);
        double latest = stateEntropy(latestStart, interactionShots.size()-1, lastAction);
        if (entropy > splitEntropyThreshold){
            splitState(lastAction, statePool);
        }

        if ((latest < refreshThreshold) && (latest >= -0.001)) {
            double sum = 0;
            for (int i = interactionShots.size()-1; i >= latestStart; i--){
                InteractionSnapshot snap = interactionShots.get(i);
                if (snap.action.isSame(lastAction)){
                    sum += snap.action.longRet;
                }
            }
            if ((sum * lastAction.longRet < 0)){
                lastAction.longRet = sum;
            }
        }
    }



    public void drawStateInfo(Graphics2D g2d){
        Color onColor = new Color(0f, 1f,0f, 0.3f);
        Color offColor = new Color(0.1f, 0f,0f, 0.1f);
        Color unknownColor = new Color(0f, 0.5f,0f, 0.05f);
        Color massColor = new Color(0f, 0.5f,0f, 1f);
        double score = actions.get(0).longRet;
        int action = actions.get(0).getActions().get(0).checkAction();
        if (score == 0){
            g2d.setColor(unknownColor);
        }
        if (score > 0){
            if (action == 0){
                g2d.setColor(onColor);
            }else{
                g2d.setColor(offColor);
            }
        }
        if (score < 0){
            if (action == 0){
                g2d.setColor(offColor);
            }else{
                g2d.setColor(unknownColor);
            }
        }
//        System.out.println("??????" + minContext.getX()+ " " + minContext.getY()+ " " + maxContext.getX()+ " " + maxContext.getY());
        g2d.fillRect(minContext.getX(), minContext.getY(), maxContext.getX()-minContext.getX(), maxContext.getY()-minContext.getY() );
//        g2d.setColor(massColor);
//        g2d.fill(new Ellipse2D.Double(massContext.getX()-2, massContext.getY()-2, 4, 4));
    }

}
