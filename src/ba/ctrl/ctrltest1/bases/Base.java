package ba.ctrl.ctrltest1.bases;

import android.content.Context;
import android.graphics.Color;

public class Base {
    private String baseid;
    private String title;
    private boolean connected;
    private long stamp;
    private String displayData;
    private int baseType;
    private String baseTypeTitle;

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
        if (iconRes == 0) {
            iconRes = context.getResources().getIdentifier("ic_base_unknown", "drawable", context.getPackageName());
        }
        return iconRes;
    }

    public String getBaseid() {
        return baseid;
    }

    public void setBaseid(String baseid) {
        this.baseid = baseid;
    }

    public String getTitle() {
        // TODO: move this to strings.xml

        if (title.equals("")) {
            title = "New Base";
        }

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

    public int getStatusColor() {
        // TODO: Move to strings.xml or wherever
        if (connected)
            return Color.parseColor("#7ed755");
        else
            return Color.parseColor("#d05b5b");
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long stamp) {
        this.stamp = stamp;
    }

    public String getDisplayData() {
        // TODO: figure how to make this dynamically for each Base Type
        displayData = "Make me...";

        if (baseType == 0) {
            displayData = baseid;
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

    public String getBaseTypeTitle() {
        // TODO: figure how to make this dynamically for each Base Type

        if (baseType == 0) {
            baseTypeTitle = "Generic Base";
        }

        return baseTypeTitle;
    }

    public void setBaseTypeTitle(String baseTypeTitle) {
        this.baseTypeTitle = baseTypeTitle;
    }

}
