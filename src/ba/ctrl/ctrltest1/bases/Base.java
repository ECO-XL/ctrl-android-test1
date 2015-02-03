package ba.ctrl.ctrltest1.bases;

import ba.ctrl.ctrltest1.R;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

public class Base {
    private final static String TAG = "Base";

    private DisplayDataParserInterface displayDataParserClass = null;

    private String baseid;
    private String title;
    private boolean connected;
    private long stamp;
    private String displayData;
    private int baseType = 0;

    public Base() {
    }

    public Base(String baseid, int base_type, String title, boolean connected, long stamp) {
        this.setBaseid(baseid);
        this.setTitle(title);
        this.setConnected(connected);
        this.setStamp(stamp);
        this.setBaseType(base_type);
    }

    // Resolves icon image resource ID for this Base Type and sets to "icon_res"
    // variable the resource value from R.java
    public int getBaseIconRID(Context context) {
        int iconRes = context.getResources().getIdentifier("ic_base_" + this.baseType, "drawable", context.getPackageName());
        // icon not found?
        if (iconRes == 0)
            iconRes = context.getResources().getIdentifier("ic_base_unknown", "drawable", context.getPackageName());
        return iconRes;
    }

    // Resolves Base Type from array resources
    public String getBaseTypeTitle(Context context) {
        String[] baseTitles = context.getResources().getStringArray(R.array.pref_base_type_titles);
        if (baseTitles.length <= this.baseType)
            return "Resources error";
        return baseTitles[this.baseType];
    }

    public String getBaseid() {
        return baseid;
    }

    public void setBaseid(String baseid) {
        this.baseid = baseid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public int getStatusColor(Context context) {
        if (connected)
            return context.getResources().getColor(R.color.base_connected_color);
        else
            return context.getResources().getColor(R.color.base_disconnected_color);
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    /*
     * Parses data that was received by this Base by calling the Display Data Parser class for this Base.
     * If "cache" is not null it will cache the class for later use once it finds it.
     * */
    @SuppressWarnings("unchecked")
    public String getDisplayData(Context context, SparseArray<Class<? extends DisplayDataParserInterface>> cache) {
        displayData = "";

        // If class is already found in this Base class instance, simply call
        // parser and return the value
        if (displayDataParserClass != null) {
            displayData = displayDataParserClass.doParse(context, this);
            Log.i(TAG, "using local cached instance for parser");
            return displayData;
        }

        // If cache storage is provided and class is already cached and found in
        // there call the parser and return value
        if (cache != null && cache.get(baseType) != null) {
            Class<? extends DisplayDataParserInterface> ddpi = cache.get(baseType);
            try {
                displayDataParserClass = ddpi.newInstance();
                displayData = displayDataParserClass.doParse(context, this);
                Log.i(TAG, "using provided-cache cached class for parser");
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return displayData;
        }

        // Since we got to here, it means we don't have class already found and
        // we don't have it in cache. Time to find it and cache if caching is
        // used.
        String cname = context.getPackageName() + ".bases.b" + this.baseType + ".DisplayDataParser";
        Class<? extends DisplayDataParserInterface> c = null;
        if (cname != null) {
            try {
                c = (Class<? extends DisplayDataParserInterface>) Class.forName(cname);
                Log.i(TAG, "found and cached parser");
            }
            catch (ClassNotFoundException e) {
                Log.e(TAG, "Looking for DisplayDataParser Class (" + cname + "), not found: " + e.getMessage());
            }
            catch (Exception e) {
                Log.e(TAG, "Looking for DisplayDataParser Class Error: " + e.getMessage());
            }
        }
        if (c != null) {
            try {
                // If caching is used, cache it!
                if (cache != null) {
                    cache.put(baseType, c);
                }
                displayDataParserClass = c.newInstance();
                displayData = displayDataParserClass.doParse(context, this);
            }
            catch (InstantiationException e) {
                e.printStackTrace();
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return displayData;
    }

    public void setDisplayData(String displayData) {
        this.displayData = displayData;
    }

    public int getBaseType() {
        return baseType;
    }

    public void setBaseType(int baseType) {
        this.baseType = baseType;
    }

}
