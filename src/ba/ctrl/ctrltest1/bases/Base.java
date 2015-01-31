package ba.ctrl.ctrltest1.bases;

import ba.ctrl.ctrltest1.R;
import android.content.Context;
import android.util.Log;

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

    @SuppressWarnings("unchecked")
    public String getDisplayData(Context context) {
        if (displayDataParserClass == null) {
            String cname = context.getPackageName() + ".bases.b" + this.baseType + ".DisplayDataParser";
            Class<? extends DisplayDataParserInterface> c = null;
            if (cname != null) {
                try {
                    c = (Class<? extends DisplayDataParserInterface>) Class.forName(cname);
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
                    this.displayDataParserClass = c.newInstance();
                }
                catch (InstantiationException e) {
                    e.printStackTrace();
                }
                catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if( displayDataParserClass != null )
        {
            displayData = displayDataParserClass.doParse(context, this);
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
