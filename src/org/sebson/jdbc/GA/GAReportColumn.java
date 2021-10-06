package org.sebson.jdbc.GA;

import com.google.api.services.analytics.model.Column;

public class GAReportColumn {

    private Column gaColumn;
    private String value;
    private int valueIndex;

    public GAReportColumn(Column gaColumn) {
        this.gaColumn = gaColumn;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getColumnId() {
        return this.gaColumn.getId();
    }

    public String getValue() {
        return this.value;
    }

    public String getType() {
        return gaColumn.getAttributes().get("type");
    }

    public String getDataType() {
        return gaColumn.getAttributes().get("dataType");
    }
}