package IoT.Context;

import java.util.ArrayList;

/*
    Base single context type
 */
public abstract class BaseContext {
    public abstract BaseContext makeClone();
    public abstract double distanceTo (BaseContext ctx);
    public abstract boolean isBetween (BaseContext ctxA, BaseContext ctxB);
    public abstract BaseContext getOffset(double offset);
    public abstract BaseContext getMidCtx(BaseContext ctxB, double ratio);
//    public abstract double longAxis(BaseContext ctxB);
//    public abstract void splitLongAxis(BaseContext massCtx, BaseContext oldMin, BaseContext oldMax, BaseContext newMin, BaseContext newMax, double ratio);

    /*
        This method is used to find the splitters to branch
     */
    public abstract ArrayList<BaseContext[]> splitPoints(BaseContext bContext, double ratio);
}
