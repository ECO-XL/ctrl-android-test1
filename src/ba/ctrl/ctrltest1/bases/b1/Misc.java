package ba.ctrl.ctrltest1.bases.b1;

import java.util.Locale;

import ba.ctrl.ctrltest1.CommonStuff;

public class Misc {
    public static String dataToStringDegrees(String data) {
        Double dAngle = dataToDoubleDegrees(data);
        String sAngle = String.format(Locale.ENGLISH, "%3.1f", dAngle);
        return sAngle;
    }

    public static Integer angleToProgressValue(Double angle) {
        Integer iMs = 0;

        Double x = (double) ((double) 1000 / (double) 90);
        iMs = (int) (x * angle);

        return iMs;
    }

    public static Double dataToDoubleDegrees(String data) {
        // Lets just take the last data we received and parse it here
        String sMs = CommonStuff.hexStringToString(data).toString();
        // Convert millisecond value we receive 1000...2000 into degrees
        // 0...90
        Integer iMs = 1000;
        try {
            iMs = Integer.parseInt(sMs);
        }
        catch (NumberFormatException nfe) {
            iMs = 1500;
        }
        iMs -= 1000; // 0...1000 where 0 is 0 degrees and 1000 is 90 degrees

        Double dAngle = (double) (((double) 90 / (double) 1000) * (double) iMs);
        return dAngle;
    }
}
