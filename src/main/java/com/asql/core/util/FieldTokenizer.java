package com.asql.core.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public final class FieldTokenizer
        implements Enumeration {
    private int currentPosition = 0;
    private int newPosition = -1;
    private int maxPosition;
    private String str;
    private String delimiters;
    private boolean retDelims;
    private boolean delimsChanged = false;
    private char maxDelimChar;

    public FieldTokenizer(String paramString1, String paramString2, boolean paramBoolean) {
        this.str = paramString1;
        this.maxPosition = paramString1.length();
        this.delimiters = paramString2;
        this.retDelims = paramBoolean;
    }

    public FieldTokenizer(String paramString1, String paramString2) {
        this(paramString1, paramString2, false);
    }

    public FieldTokenizer(String paramString) {
        this(paramString, " \t\n\r\f", false);
    }

    private boolean matchDelims(int paramInt) {
        if (this.delimiters == null)
            throw new NullPointerException();

        if (paramInt >= this.maxPosition)
            return false;
        if (paramInt + this.delimiters.length() > this.maxPosition)
            return false;
        boolean flag = true;
        for (int j = 0; j < this.delimiters.length(); j++) {
            if (this.str.charAt(paramInt + j) == this.delimiters.charAt(j))
                continue;
            flag = false;
            break;
        }
        return flag;
    }

    private int skipDelimiters(int paramInt) {
        if (this.delimiters == null) {
            throw new NullPointerException();
        }
        int i = paramInt;
        for (; (!this.retDelims) && (i < this.maxPosition); i++) {
            if (!matchDelims(i))
                continue;
            i += this.delimiters.length();
            break;
        }
        return i;
    }

    private int scanToken(int paramInt) {
        int i = paramInt;
        for (; (i < this.maxPosition) && (!matchDelims(i)); i++) ;
        if ((this.retDelims) && (paramInt == i) && (matchDelims(i))) {
            i += this.delimiters.length();
        }
        return i;
    }

    public boolean hasMoreTokens() {
        this.newPosition = skipDelimiters(this.currentPosition);
        return this.newPosition < this.maxPosition;
    }

    public String nextToken() {
        this.currentPosition = ((this.newPosition >= 0) && (!this.delimsChanged)
                ? this.newPosition : skipDelimiters(this.currentPosition));
        this.delimsChanged = false;
        this.newPosition = -1;
        if (this.currentPosition >= this.maxPosition)
            throw new NoSuchElementException();
        int i = this.currentPosition;
        this.currentPosition = scanToken(this.currentPosition);
        return this.str.substring(i, this.currentPosition);
    }

    public String nextToken(String paramString) {
        this.delimiters = paramString;
        this.delimsChanged = true;
        return nextToken();
    }

    public boolean hasMoreElements() {
        return hasMoreTokens();
    }

    public Object nextElement() {
        return nextToken();
    }

    public int countTokens() {
        int i = 0;
        int j = this.currentPosition;
        while (j < this.maxPosition) {
            j = skipDelimiters(j);
            if (j >= this.maxPosition)
                break;
            j = scanToken(j);
            i++;
        }
        return i;
    }
}
