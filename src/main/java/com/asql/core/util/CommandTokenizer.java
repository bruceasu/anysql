package com.asql.core.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class CommandTokenizer
        implements Enumeration {
    private int currentPosition = 0;
    private int newPosition = -1;
    private int maxPosition;
    private String str;
    private String[] delimiters = {" ", "\r", "\n", "\t"};
    private boolean retDelims;
    private boolean delimsChanged = false;
    private int currentDelims = -1;
    private char maxDelimChar;

    public CommandTokenizer(String paramString, String[] paramArrayOfString, boolean paramBoolean) {
        this.str = paramString;
        this.maxPosition = paramString.length();
        this.delimiters = paramArrayOfString;
        this.retDelims = paramBoolean;
    }

    public CommandTokenizer(String paramString, String[] paramArrayOfString) {
        this(paramString, paramArrayOfString, false);
    }

    public CommandTokenizer(String paramString) {
        this.str = paramString;
        this.maxPosition = paramString.length();
        this.retDelims = false;
    }

    private boolean matchDelims(int paramInt, String paramString) {
        if (paramString == null)
            throw new NullPointerException();

        if (paramInt + paramString.length() >= this.maxPosition)
            return false;
        if (paramInt + paramString.length() > this.maxPosition)
            return false;
        boolean flag = true;
        for (int j = 0; j < paramString.length(); j++) {
            if (this.str.charAt(paramInt + j) == paramString.charAt(j))
                continue;
            flag = false;
            break;
        }
        return flag;
    }

    private boolean matchDelims(int paramInt) {
        if ((this.delimiters == null) || (this.delimiters.length == 0))
            throw new NullPointerException();
        this.currentDelims = -1;
        boolean bool = false;
        for (int i = 0; i < this.delimiters.length; i++) {
            bool = matchDelims(paramInt, this.delimiters[i]);
            if (!bool)
                continue;
            this.currentDelims = i;
            break;
        }
        return bool;
    }

    private int skipDelimiters(int paramInt) {
        int i = paramInt;
        for (; (!this.retDelims) && (i < this.maxPosition); i++) {
            if (!matchDelims(i))
                continue;
            i += this.delimiters[this.currentDelims].length();
            break;
        }
        return i;
    }

    private int scanToken(int paramInt) {
        int i = paramInt;
        for (; (i < this.maxPosition) && (!matchDelims(i)); i++) ;
        if ((this.retDelims) && (paramInt == i) && (matchDelims(i))) {
            i += this.delimiters[this.currentDelims].length();
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

    public String nextToken(String[] paramArrayOfString) {
        this.delimiters = paramArrayOfString;
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

