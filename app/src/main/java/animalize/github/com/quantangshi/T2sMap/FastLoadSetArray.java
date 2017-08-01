package animalize.github.com.quantangshi.T2sMap;

/**
 * Created by anima on 17-8-1.
 */

public class FastLoadSetArray {
    private int[] mSet;
    private int mSize;

    public FastLoadSetArray(int[] set) {
        mSet = set;
        mSize = set.length;
    }

    public boolean contains(int key) {
        int low = 0;
        int high = mSize - 1;

        while (high >= low) {
            int mid = (low + high) / 2;

            if (key < mSet[mid]) {
                high = mid - 1;
            } else if (key > mSet[mid]) {
                low = mid + 1;
            } else {
                return true;
            }
        }
        return false;
    }
}
