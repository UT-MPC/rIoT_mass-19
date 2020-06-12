package IoT.Context;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;

public class DynamicContext extends AbstractContextArr {
    /*  Alpha has two meaning.
        One is to normalize different context type.
        The second is to personalize different users and devices.
     */
    public static class Alpha{
        public ArrayList<Double>   alphas;
        public Alpha(Alpha input){
            alphas = new ArrayList<>(input.alphas);
        }

        public Alpha(ArrayList<Double> input){
            alphas = new ArrayList<>(input);
        }

        // location, time, direction
        public static Alpha defaultAlpha(){
            return new Alpha(new ArrayList<Double>(Arrays.asList(1d, 1.0d, 1d)));
        }
    }
    protected ArrayList<BaseContext> contexts;

    public ArrayList<BaseContext> getContexts() {return contexts; }
    public int getX(){return ((LocationContext)contexts.get(0)).getLocation().x;}
    public int getY(){return ((LocationContext)contexts.get(0)).getLocation().y;}


    private Alpha myAlpha;
    public Alpha getAlpha() {return  myAlpha;}
    public void setAlpha(Alpha input) {myAlpha = input;}

    public DynamicContext(DynamicContext clone){
        super();
        this.contexts = new ArrayList<>();
        for (BaseContext ctx: clone.getContexts()){
            this.contexts.add(ctx.makeClone());
        }
        this.myAlpha = new Alpha(clone.getAlpha());
    }
    public DynamicContext(ArrayList<BaseContext> ctxs, Alpha alpha){
        super();
        this.contexts = ctxs;
        this.myAlpha = alpha;
    }
    public DynamicContext(int x, int y, int time, Alpha alpha){
        super();
        this.contexts = new ArrayList<>();
        contexts.add(new LocationContext(new Point2D(x,y)));
        contexts.add(new TimeContext(time));
        this.myAlpha = alpha;
    }
    public DynamicContext(int x, int y, int time){
        super();
        this.contexts = new ArrayList<>();
        contexts.add(new LocationContext(new Point2D(x,y)));
        contexts.add(new TimeContext(time));
        this.myAlpha = Alpha.defaultAlpha();
    }
    public DynamicContext(int x, int y, double dirX, double dirY, int time){
        super();
        this.contexts = new ArrayList<>();
        contexts.add(new LocationContext(new Point2D(x,y)));
        contexts.add(new TimeContext(time));
        contexts.add(new SimpleDirContext(dirX,dirY));
        this.myAlpha = Alpha.defaultAlpha();
    }

    public void setMyAlpha(Alpha newAlpha){
        myAlpha = newAlpha;
    }

    public DynamicContext(){super();this.contexts = new ArrayList<>(); this.myAlpha = Alpha.defaultAlpha();}
    public void addContext(BaseContext newCtx){
        contexts.add(newCtx);
    }

//    public static DynamicContext generateContext(Actor user, LocalTime time){
////        return new DynamicContext(user.getTopLeftX(), user.getTopLeftY(), user.getDirX(), user.getDirY(), time.toSecondOfDay());
//        return new DynamicContext(user.getTopLeftX(), user.getTopLeftY(), time.toSecondOfDay());
//    }
    public double distanceTo(AbstractContextArr toContext){

        double sum = 0;
        for (int i = 0; i < contexts.size(); i++){
            sum += contexts.get(i).distanceTo(toContext.getContexts().get(i)) * myAlpha.alphas.get(i);
        }
        return sum;
    }
    public DynamicContext getOffsetContext(double offset){
        double of = offset;
        ArrayList<BaseContext> newContexts = new ArrayList<>();
        for (int i = 0; i < contexts.size(); i++){
            newContexts.add(contexts.get(i).getOffset((of / myAlpha.alphas.get(i))));
        }
        return new DynamicContext(newContexts, myAlpha);
    }
    /*
        Bias should be between 0-1
     */
    public DynamicContext getMidContext(DynamicContext bContext, double bias){
        ArrayList<BaseContext> newContexts = new ArrayList<>();
        for (int i = 0; i < contexts.size(); i++){
            newContexts.add(contexts.get(i).getMidCtx(bContext.getContexts().get(i), bias));
        }
        return new DynamicContext(newContexts, myAlpha);
    }

    public boolean isBetween(DynamicContext aContext, DynamicContext bContext){
        for (int i = 0; i < contexts.size(); i++){
            if (!contexts.get(i).isBetween(aContext.getContexts().get(i), bContext.getContexts().get(i))){
                return false;
            }
        }
        return true;
    }

    /*
        This function will split the context along the long axis where the long axis is the context type that has the
        biggest difference.

     */
    static public DynamicContext[] stateSpliter(DynamicContext oldMin, DynamicContext oldMax,
                                                DynamicContext newCtx
            , DynamicContext massCtx, double ratio)
    {
//        double maxAxis = 0;
//        int maxIdx = -1;
//        for (int i = 0; i < newCtx.getContexts().size(); i++){
//            double delta = newCtx.getContexts().get(i).longAxis(massCtx.getContexts().get(i));
//            if (delta > maxAxis){
//                maxAxis = delta;
//                maxIdx = i;
//            }
//        }
//        DynamicContext newMin = new DynamicContext(oldMin);
//        DynamicContext newMax = new DynamicContext(oldMax);
//
//        newCtx.getContexts().get(maxIdx).splitLongAxis(massCtx.getContexts().get(maxIdx), oldMin.getContexts().get(maxIdx),
//                                                      oldMax.getContexts().get(maxIdx), newMin.getContexts().get(maxIdx),
//                                                      newMax.getContexts().get(maxIdx), ratio);
//
//        return new DynamicContext[]{newMin, newMax};
        return null;
    }

    //split the state points for branch.
    // newMax, newMin;
    // ratio = (newMax - oldMin) / (oldMax - oldMin)
    static public ArrayList<DynamicContext[]> splitPoints(DynamicContext oldMin, DynamicContext oldMax, double ratio){
        ArrayList<DynamicContext[]> output = new ArrayList<>();
        for (int i = 0; i < oldMin.getContexts().size(); i++){
            ArrayList<BaseContext[]> splitters = oldMin.getContexts().get(i).splitPoints(oldMax.getContexts().get(i), ratio);
            for (BaseContext[] split: splitters){
                DynamicContext newMax = new DynamicContext(oldMax);
                DynamicContext newMin = new DynamicContext(oldMin);
                newMax.getContexts().set(i, split[1]);
                newMin.getContexts().set(i, split[2]);
                output.add(new DynamicContext[]{oldMin, newMax, newMin, oldMax});
            }
        }
        return output;
    }
}
