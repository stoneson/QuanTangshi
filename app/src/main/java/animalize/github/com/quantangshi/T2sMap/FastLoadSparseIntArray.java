package animalize.github.com.quantangshi.T2sMap;


/**
 * Created by anima on 17-8-1.
 */

public class FastLoadSparseIntArray {
    private int[] mKeys;
    private int[] mValues;
    private int mSize;

    public FastLoadSparseIntArray(int[] keys, int[] values) {
        if (keys.length != values.length) {
            mKeys = new int[]{};
            mValues = mKeys;
            mSize = 0;
        } else {
            mKeys = keys;
            mValues = values;
            mSize = keys.length;
        }
    }

    public int get(int key, int valueIfKeyNotFound) {
        int low = 0;
        int high = mSize - 1;

        while (high >= low) {
            int mid = (low + high) / 2;

            if (key < mKeys[mid]) {
                high = mid - 1;
            } else if (key > mKeys[mid]) {
                low = mid + 1;
            } else {
                return mValues[mid];
            }
        }
        return valueIfKeyNotFound;
    }
}
