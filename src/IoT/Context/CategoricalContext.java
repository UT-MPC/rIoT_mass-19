package IoT.Context;

import IoT.THING.Light;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CategoricalContext extends BaseContext{
    private List<Integer> cat;
    public static double range = 1.0;

    public CategoricalContext(List<Integer> category){
        this.cat = new ArrayList<Integer>(category);
    }

    public List<Integer> getCat() {
        return cat;
    }

    public void setCat(List<Integer> cat){
        this.cat = cat;
    }

    @Override
    public CategoricalContext makeClone(){
        return new CategoricalContext(cat);
    }

    @Override
    public double distanceTo (BaseContext ctx){
        if (CategoricalContext.class.isInstance(ctx)){
            for (int c: ((CategoricalContext)ctx).cat){
                if (cat.contains(c)){
                    return 0;
                }
            }
            return (1.0 / range);
        } else {
            return 0;
        }
    }

    @Override
    public boolean isBetween(BaseContext A, BaseContext B){
        if (!CategoricalContext.class.isInstance(A) || !CategoricalContext.class.isInstance(B)){
            return false;
        }
        for (int c: cat){
            if (((CategoricalContext)A).cat.contains(c) || ((CategoricalContext)B).cat.contains(c)){
                return true;
            }
        }
        return false;
    }

    @Override
    public BaseContext getOffset(double offset){
        return new CategoricalContext(cat);
    }

    @Override
    public BaseContext getMidCtx(BaseContext ctxB, double ratio){
        List<Integer> newCat = Stream.of(cat, ((CategoricalContext)ctxB).cat)
                                        .flatMap(Collection::stream)
                                        .distinct()
                                        .collect(Collectors.toList());

        return new CategoricalContext(newCat);
    }
    private void dfs(int n, int k, int start, ArrayList<Integer> item,
                     ArrayList<ArrayList<Integer>> res) {
        if (item.size() == k) {
            res.add(new ArrayList<Integer>(item));
            return;
        }

        for (int i = start; i <= n; i++) {
            item.add(i);
            dfs(n, k, i + 1, item, res);
            item.remove(item.size() - 1);
        }
    }

    private ArrayList<ArrayList<Integer>> combine(int n, int k) {
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

        if (n <= 0 || n < k)
            return result;

        ArrayList<Integer> item = new ArrayList<Integer>();
        dfs(n, k, 1, item, result); // because it need to begin from 1

        return result;
    }

    @Override
    public ArrayList<BaseContext[]> splitPoints(BaseContext bContext, double ratio){
        if (bContext instanceof CategoricalContext){
            List<Integer> newCat = Stream.of(cat, ((CategoricalContext)bContext).cat)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
            int size = newCat.size();
            int leftSize = (int)((double)size * ratio);
            int righSize = size - leftSize;
            ArrayList<BaseContext[]> output = new ArrayList<>();

            ArrayList<ArrayList<Integer>> combination = combine(size, leftSize);
            for (ArrayList<Integer> combo: combination){
                ArrayList<Integer> newMax = new ArrayList<>();
                ArrayList<Integer> newMin = new ArrayList<>(newCat);
                for (int pick: combo){
                    newMax.add(newCat.get(pick));
                    newMin.remove(newCat.get(pick));
                }
                output.add(new BaseContext[]{new CategoricalContext(newMax), new CategoricalContext(newMax), new CategoricalContext(newMin), new CategoricalContext(newMin)});
            }
            return output;
        }
        return null;
    }
}