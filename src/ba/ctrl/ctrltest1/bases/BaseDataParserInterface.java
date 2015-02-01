package ba.ctrl.ctrltest1.bases;

import android.content.Context;

public interface BaseDataParserInterface {
    public void doParse(Context context, String baseId, String data, boolean connectedStateChanged, boolean connected);
}
