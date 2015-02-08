package ba.ctrl.ctrltest1.bases.b0;

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

        // Since this is a Generic Base, lets just display last data we
        // received. If there is no data then display BaseID
        displayData = dataSource.getLatestBaseData(base.getBaseid());
        if (displayData.equals("")) {
            displayData = base.getBaseid();
        }

        return displayData;
    }

}
