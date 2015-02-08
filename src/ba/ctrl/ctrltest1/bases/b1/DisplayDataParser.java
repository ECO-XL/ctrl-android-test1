package ba.ctrl.ctrltest1.bases.b1;

import android.content.Context;
import ba.ctrl.ctrltest1.bases.Base;
import ba.ctrl.ctrltest1.bases.DisplayDataParserInterface;
import ba.ctrl.ctrltest1.database.DataSource;

public class DisplayDataParser implements DisplayDataParserInterface {
    private DataSource dataSource = null;

    @Override
    public String doParse(Context context, Base base) {
        String displayData;

        if (dataSource == null) {
            dataSource = DataSource.getInstance(context);
        }

        String data = dataSource.getLatestBaseData(base.getBaseid());

        if (data.equals("")) {
            displayData = "No data received yet.";
            return displayData;
        }

        displayData = "Position: " + Misc.dataToStringDegrees(data) + " degrees.";
        return displayData;
    }

}
