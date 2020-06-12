package IoT.THING;

public class CameraAction implements Action {
    private Camera myCam;
    private boolean turnOn;
    public CameraAction(Camera cam, boolean isTurnOn){
        myCam = cam;
        turnOn = isTurnOn;
    }

    @Override
    public int checkAction(){
        return turnOn?0:1;
    }

    @Override
    public void undoAction() {
        if (turnOn){
            myCam.turnOff();
        }else{
            myCam.turnOn();
        }
    }

    @Override
    public void doAction() {
        if (turnOn){
            myCam.turnOn();
        }else{
            myCam.turnOff();
        }
    }

    @Override
    public Thing getDevice(){
        return myCam;
    }

    @Override
    public boolean isSame(Action bAction) {
        if (!(bAction instanceof CameraAction)){
            return false;
        }
        if (!(myCam.equals(bAction.getDevice()))) {
            return false;
        }
        if (checkAction() != bAction.checkAction()){
            return false;
        }
        return true;
    }

    @Override
    public boolean isStateCompatible(int thingState){
        if (((turnOn)&&(thingState==Camera.onState)) || ((!turnOn)&&(thingState==Camera.offState))){
            return false;
        }
        return true;
    }

    @Override
    public boolean actionFilter(Action filter){
        if (filter == null) return true;
        if (!(filter instanceof CameraAction)){
            return false;
        }
        if (checkAction() != filter.checkAction()){
            return false;
        }
        return true;
    }
}
