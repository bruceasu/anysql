package com.asql.core;

public final class SQLQuery {
    private String sourceSQL = null;
    private String destSQL = null;
    private String[] paramNames = null;
    private String[] paramTypes = null;

    public SQLQuery(String sourceSQL,
                    String destSQL,
                    String[] paramNames,
                    String[] paramTypes) {
        this.sourceSQL = sourceSQL;
        this.destSQL = destSQL;
        this.paramNames = paramNames;
        this.paramTypes = paramTypes;
    }

    public final String getSourceSQL() {
        return this.sourceSQL;
    }

    public final String getDestSQL() {
        return this.destSQL;
    }

    public final String[] getParamNames() {
        return this.paramNames;
    }

    public final String[] getParamTypes() {
        return this.paramTypes;
    }
}

