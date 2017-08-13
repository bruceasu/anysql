package com.asql.core.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public final class DateOperator {
    private static final int[] Month_Days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private static final int getMonthDays(int paramInt1, int paramInt2) {
        if (((paramInt1 % 400 == 0) || ((paramInt1 % 4 == 0) && (paramInt1 % 100 != 0))) && (paramInt2 == 2))
            return Month_Days[(paramInt2 - 1)] + 1;
        return Month_Days[(paramInt2 - 1)];
    }

    public static final String getDay() {
        String str = "";
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat();
        localSimpleDateFormat.applyPattern("yyyyMMdd");
        str = localSimpleDateFormat.format(new Date());
        return str;
    }

    public static final String getDay(String paramString) {
        String str = "";
        SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(paramString);
        str = localSimpleDateFormat.format(new Date());
        localSimpleDateFormat = null;
        return str;
    }

    public static final String firstDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        str = str + "01";
        return str;
    }

    public static final String firstQuaterDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1)) {
            return paramString;
        }
        if ((d > getMonthDays(y, m)) || (d < 1)) {
            return paramString;
        }
        str = String.valueOf(y);
        m = (m - 1) / 3 * 3 + 1;
        if (m < 10) {
            str = str + "0" + m;
        } else {
            str = str + m;
        }
        str = str + "01";
        return str;
    }

    public static final String lastQuaterDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);

        m = ((m - 1) / 3 + 1) * 3;
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        str = str + getMonthDays(y, m);
        return str;
    }

    public static final String firstYearDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);
        str = str + "0101";
        return str;
    }

    public static final String lastYearDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);
        str = str + "12" + getMonthDays(y, 12);
        return str;
    }

    public static final String lastDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        str = str + getMonthDays(y, m);
        return str;
    }

    public static final String nextDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        if (d == getMonthDays(y, m)) {
            d = 1;
            m += 1;
        } else {
            d += 1;
        }
        if (m > 12) {
            m = 1;
            y += 1;
        }
        str = String.valueOf(y);
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        if (d < 10)
            str = str + "0" + d;
        else
            str = str + d;
        return str;
    }

    public static final String prevDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        if (d == 1) {
            m -= 1;
            if (m == 0) {
                m = 12;
                y -= 1;
            }
            d = getMonthDays(y, m);
        } else {
            d -= 1;
        }
        str = String.valueOf(y);
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        if (d < 10)
            str = str + "0" + d;
        else
            str = str + d;
        return str;
    }

    public static final String addDays(String paramString, int paramInt) {
        int j = Math.abs(paramInt);
        String str = paramString;
        int k = paramInt > 0 ? 1 : 0;
        for (int i = 0; i < j; i++)
            if (k != 0)
                str = nextDay(str);
            else
                str = prevDay(str);
        return str;
    }

    public static final String nextMonth(String paramString) {
        int i = 0;
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((m > getMonthDays(y, m)) || (m < 1))
            return paramString;
        if (m == getMonthDays(y, m))
            i = 1;
        else
            i = 0;
        m += 1;
        if (m > 12) {
            m = 1;
            y += 1;
        }
        if ((i != 0) || (m > getMonthDays(y, m)))
            m = getMonthDays(y, m);
        str = String.valueOf(y);
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        return str;
    }

    public static final String prevMonth(String paramString) {
        int i = 0;
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        if (d == getMonthDays(y, m))
            i = 1;
        else
            i = 0;
        m -= 1;
        if (m == 0) {
            m = 12;
            y -= 1;
        }
        if ((i != 0) || (d > getMonthDays(y, m)))
            d = getMonthDays(y, m);
        str = String.valueOf(y);
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        if (d < 10)
            str = str + "0" + d;
        else
            str = str + d;
        return str;
    }

    public static final String addMonths(String paramString, int paramInt) {
        int j = Math.abs(paramInt);
        String str = paramString;
        int k = paramInt > 0 ? 1 : 0;
        for (int i = 0; i < j; i++)
            if (k != 0)
                str = nextMonth(str);
            else
                str = prevMonth(str);
        return str;
    }

    public static final String firstHarfYearDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();

        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);
        if (m > 6)
            m = 7;
        else
            m = 1;
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        str = str + "01";
        return str;
    }

    public static final String lastHarfYearDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString).longValue();
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();

        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);
        if (m > 6)
            m = 12;
        else
            m = 6;
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        str = str + getMonthDays(y, m);
        return str;
    }

    public static final String firstTenDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString);
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        if (d < 11)
            d = 1;
        else if (d < 21)
            d = 11;
        else
            d = 21;
        if (d < 10)
            str = str + "0" + d;
        else
            str = str + d;
        return str;
    }

    public static final String lastTenDay(String paramString) {
        String str = "";
        if (paramString == null)
            return paramString;
        if (paramString.length() != 8)
            return paramString;
        if ((paramString.startsWith("-")) || (paramString.startsWith("+")))
            return paramString;
        try {
            long l = Long.valueOf(paramString);
        } catch (NumberFormatException localNumberFormatException1) {
            return paramString;
        }
        GetYMD getYMD = new GetYMD(paramString).invoke();
        if (!getYMD.success()) {
            return paramString;
        }
        int y = getYMD.getY();
        int m = getYMD.getM();
        int d = getYMD.getD();
        if ((m > 12) || (m < 1))
            return paramString;
        if ((d > getMonthDays(y, m)) || (d < 1))
            return paramString;
        str = String.valueOf(y);
        if (m < 10)
            str = str + "0" + m;
        else
            str = str + m;
        if (d < 11)
            d = 10;
        else if (d < 21)
            d = 20;
        else
            d = getMonthDays(y, m);
        if (d < 10)
            str = str + "0" + d;
        else
            str = str + d;
        return str;
    }

    public static final String[] getDays(String paramString1, String paramString2) {
        String[] arrayOfString = new String[0];
        Vector localVector = new Vector();
        if (nextDay(paramString1).equals(paramString1))
            return arrayOfString;
        if (nextDay(paramString2).equals(paramString2))
            return arrayOfString;
        String str = paramString1;
        int i = str.compareTo(paramString2);
        if (i > 0)
            return arrayOfString;
        while (i <= 0) {
            localVector.addElement(str);
            str = nextDay(str);
            i = str.compareTo(paramString2);
        }
        arrayOfString = new String[localVector.size()];
        for (i = 0; i < localVector.size(); i++)
            arrayOfString[i] = localVector.elementAt(i).toString();
        return arrayOfString;
    }

    public static final String[] getTenDays(String paramString1, String paramString2) {
        String[] arrayOfString = getDays(paramString1, paramString2);
        Vector localVector = new Vector();
        String str = "xxxxxxxx";
        for (int i = 0; i < arrayOfString.length; i++) {
            if (firstTenDay(arrayOfString[i]).equals(str))
                continue;
            str = firstTenDay(arrayOfString[i]);
            localVector.addElement(str);
        }
        arrayOfString = new String[localVector.size()];
        for (int i = 0; i < localVector.size(); i++)
            arrayOfString[i] = localVector.elementAt(i).toString();
        return arrayOfString;
    }

    public static final String[] getMonthDays(String paramString1, String paramString2) {
        String[] arrayOfString = getTenDays(paramString1, paramString2);
        Vector localVector = new Vector();
        String str = "xxxxxxxx";
        for (int i = 0; i < arrayOfString.length; i++) {
            if (firstDay(arrayOfString[i]).equals(str))
                continue;
            str = firstDay(arrayOfString[i]);
            localVector.addElement(str);
        }
        arrayOfString = new String[localVector.size()];
        for (int i = 0; i < localVector.size(); i++)
            arrayOfString[i] = localVector.elementAt(i).toString();
        return arrayOfString;
    }

    public static final String[] getQuaterDays(String paramString1, String paramString2) {
        String[] arrayOfString = getMonthDays(paramString1, paramString2);
        Vector localVector = new Vector();
        String str = "xxxxxxxx";
        for (int i = 0; i < arrayOfString.length; i++) {
            if (firstQuaterDay(arrayOfString[i]).equals(str))
                continue;
            str = firstQuaterDay(arrayOfString[i]);
            localVector.addElement(str);
        }
        arrayOfString = new String[localVector.size()];
        for (int i = 0; i < localVector.size(); i++)
            arrayOfString[i] = localVector.elementAt(i).toString();
        return arrayOfString;
    }

    public static final String[] getHarfYearDays(String paramString1, String paramString2) {
        String[] arrayOfString = getQuaterDays(paramString1, paramString2);
        Vector localVector = new Vector();
        String str = "xxxxxxxx";
        for (int i = 0; i < arrayOfString.length; i++) {
            if (firstHarfYearDay(arrayOfString[i]).equals(str))
                continue;
            str = firstHarfYearDay(arrayOfString[i]);
            localVector.addElement(str);
        }
        arrayOfString = new String[localVector.size()];
        for (int i = 0; i < localVector.size(); i++)
            arrayOfString[i] = localVector.elementAt(i).toString();
        return arrayOfString;
    }

    public static final String[] getYearDays(String paramString1, String paramString2) {
        String[] arrayOfString = getHarfYearDays(paramString1, paramString2);
        Vector localVector = new Vector();
        String str = "xxxxxxxx";
        for (int i = 0; i < arrayOfString.length; i++) {
            if (firstYearDay(arrayOfString[i]).equals(str))
                continue;
            str = firstYearDay(arrayOfString[i]);
            localVector.addElement(str);
        }
        arrayOfString = new String[localVector.size()];
        for (int i = 0; i < localVector.size(); i++)
            arrayOfString[i] = localVector.elementAt(i).toString();
        return arrayOfString;
    }

    private static class GetYMD {
        private boolean myResult;
        private String paramString;
        private int y;
        private int m;
        private int d;

        public GetYMD(String paramString) {
            this.paramString = paramString;
        }

        boolean success() {
            return myResult;
        }

        public int getY() {
            return y;
        }

        public int getM() {
            return m;
        }

        public int getD() {
            return d;
        }

        public GetYMD invoke() {
            try {
                y = Integer.valueOf(paramString.substring(0, 4));
                m = Integer.valueOf(paramString.substring(4, 6));
                d = Integer.valueOf(paramString.substring(6, 8));
                myResult = true;
            } catch (NumberFormatException localNumberFormatException2) {
                myResult = false;
            }
            return this;
        }
    }
}
