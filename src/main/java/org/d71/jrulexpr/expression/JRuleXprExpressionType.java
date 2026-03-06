package org.d71.jrulexpr.expression;

public enum JRuleXprExpressionType {
    JRXC("jrxc"),
    JRXP("jrxp"),
    JRX("jrx"),
    JRXT("jrxt"),
    JRXF("jrxf"),
    JRXV("jrxv");

    private String token;

    JRuleXprExpressionType(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
