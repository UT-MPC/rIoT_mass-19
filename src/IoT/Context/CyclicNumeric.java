package IoT.Context;

import java.time.LocalTime;
import java.util.ArrayList;

public class CyclicNumeric extends BaseContext{
    private int num;              // in terms of seconds of the day. scale 0-86399. Normalize to 0-720
    private static int MAX = LocalTime.MAX.toSecondOfDay() - LocalTime.MIN.toSecondOfDay();
    public double range = 1.0;

    public CyclicNumeric(int num){
//        System.out.println(range);
        this.num = num;
    }
    public CyclicNumeric(int num, double range){
        this.num = num;
        this.range = range;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Override
    public CyclicNumeric makeClone(){
        return new CyclicNumeric(num);
    }

    @Override
    public double distanceTo (BaseContext ctx){
        if (CyclicNumeric.class.isInstance(ctx)){
            double pureSub = Math.abs(num - ((CyclicNumeric)ctx).getNum());
            if (pureSub > range){
                pureSub = pureSub - range;
            }
            return (pureSub / range);
        } else {
            return 0;
        }
    }

    @Override
    public boolean isBetween(BaseContext A, BaseContext B){
        if (!CyclicNumeric.class.isInstance(A) || !CyclicNumeric.class.isInstance(B)){
            return false;
        }
        if ((num < ((CyclicNumeric)A).getNum()) || (num > ((CyclicNumeric)B).getNum())){
            return false;
        }
        return true;
    }

    @Override
    public CyclicNumeric getOffset(double offset){
        int newT = (int)(num + (offset * range));
//        if (newT < 0) newT = 0;
//        if (newT > LocalTime.MAX.toSecondOfDay()) newT = LocalTime.MAX.toSecondOfDay();
        return new CyclicNumeric(newT);
    }

    @Override
    public CyclicNumeric getMidCtx(BaseContext ctxB, double ratio){
        CyclicNumeric ctx = (CyclicNumeric) ctxB;
        double newT = num * ratio + ctx.getNum() * (1 - ratio);
        return new CyclicNumeric((int)newT);
    }

    @Deprecated
    public double longAxis(BaseContext ctxB){
        return distanceTo(ctxB);
    }

    @Deprecated
    public void splitLongAxis(BaseContext massCtx, BaseContext oldMin, BaseContext oldMax, BaseContext newMin, BaseContext newMax, double ratio){
        CyclicNumeric ctx = (CyclicNumeric) massCtx;
        CyclicNumeric midCtx = getMidCtx(massCtx, ratio / (ratio + 1));
        if (num < ctx.getNum()){
            ((CyclicNumeric) newMax).setNum(midCtx.getNum());
            ((CyclicNumeric) oldMin).setNum(midCtx.getNum());
        }else{
            ((CyclicNumeric) newMin).setNum(midCtx.getNum());
            ((CyclicNumeric) oldMax).setNum(midCtx.getNum());
        }
    }

    @Override
    public ArrayList<BaseContext[]> splitPoints(BaseContext bContext, double ratio){
        if (bContext instanceof CyclicNumeric){
            CyclicNumeric bCtx = (CyclicNumeric) bContext;
            int t1 = num;
            int t2 = bCtx.num;
            int tPoint = (int)(t1 + (t2 - t1) * ratio);
            CyclicNumeric newMax1 = new CyclicNumeric(tPoint);
            CyclicNumeric newMin1 = new CyclicNumeric(tPoint);
            ArrayList<BaseContext[]> output = new ArrayList<>();
            output.add(new BaseContext[]{this, newMax1, newMin1, bContext});
            return output;
        }
        return null;
    }

}