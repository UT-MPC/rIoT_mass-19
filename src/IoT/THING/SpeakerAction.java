package IoT.THING;

public class SpeakerAction implements Action {
    private Speaker mySpeaker;
    private boolean turnOn;
    public SpeakerAction(Speaker light, boolean isTurnOn){
        mySpeaker = light;
        turnOn = isTurnOn;
    }

    @Override
    public int checkAction(){
        return turnOn?0:1;
    }

    @Override
    public void undoAction() {
        if (turnOn){
            mySpeaker.turnOff();
        }else{
            mySpeaker.turnOn();
        }
    }

    @Override
    public void doAction() {
        if (turnOn){
            mySpeaker.turnOn();
        }else{
            mySpeaker.turnOff();
        }
    }
    @Override
    public Thing getDevice(){
        return mySpeaker;
    }


    @Override
    public boolean isSame(Action bAction){
        if (!(bAction instanceof SpeakerAction)){
            return false;
        }
        if (!(mySpeaker.equals(bAction.getDevice()))) {
            return false;
        }
        if (checkAction() != bAction.checkAction()){
            return false;
        }
        return true;
    }

    @Override
    public boolean isStateCompatible(int thingState){
        if ((turnOn)&&(thingState==Speaker.onState)) {
            return false;
        }
        if ((!turnOn)&&(thingState==Speaker.offState)){
            return false;
        }
        return true;
    }

    @Override
    public boolean actionFilter(Action filter){
        if (filter == null) return true;
        if (!(filter instanceof SpeakerAction)){
            return false;
        }
        if (checkAction() != filter.checkAction()){
            return false;
        }
        return true;
    }
}
