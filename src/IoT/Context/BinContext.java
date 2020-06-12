package IoT.Context;

import java.util.ArrayList;

public class BinContext extends BaseContext{
    private double bit = 0;
    public static double range = 1;

    public BinContext(boolean bit){
        this.bit = bit?1:0;
    }

    public BinContext(double bit){
        this.bit = bit;
    }

    public double getBit() {
        return bit;
    }

    public void setBit(double bit){
        this.bit = bit;
    }

    @Override
    public BinContext makeClone(){
        return new BinContext(bit);
    }

    @Override
    public double distanceTo (BaseContext ctx){
        if (BinContext.class.isInstance(ctx)){
            double pureSub = Math.abs(bit - ((BinContext)ctx).getBit());
            return (pureSub / range);
        } else {
            return 0;
        }
    }

    @Override
    public boolean isBetween(BaseContext A, BaseContext B){
        if (!BinContext.class.isInstance(A) || !BinContext.class.isInstance(B)){
            return false;
        }
        if ((bit < ((BinContext)A).getBit()) || (bit > ((BinContext)B).getBit())){
            return false;
        }
        return true;
    }

    @Override
    public BinContext getOffset(double offset){
        double newB = bit + offset * range;
        return new BinContext(newB);
    }

    @Override
    public BinContext getMidCtx(BaseContext ctxB, double ratio){
        BinContext ctx = (BinContext) ctxB;
        double newT = bit * ratio + ctx.getBit() * (1 - ratio);
        return new BinContext(newT);
    }
    @Override
    public ArrayList<BaseContext[]> splitPoints(BaseContext bContext, double ratio){
        if (bContext instanceof BinContext){
            BinContext bCtx = (BinContext) bContext;
            double t1 = bit;
            double t2 = bCtx.bit;
            double tPoint = (t1 + (t2 - t1) * ratio);
            BinContext newMax1 = new BinContext(tPoint);
            BinContext newMin1 = new BinContext(tPoint);
            ArrayList<BaseContext[]> output = new ArrayList<>();
            output.add(new BaseContext[]{this, newMax1, newMin1, bContext});
            return output;
        }
        return null;
    }
}
