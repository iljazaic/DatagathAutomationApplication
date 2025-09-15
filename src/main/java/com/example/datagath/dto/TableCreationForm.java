package com.example.datagath.dto;

import java.util.List;

public class TableCreationForm {
    private String tableName;
    public Boolean isPublic;
    public List<ColumnDTO> columns;
    private Long userId;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long id) {
        this.userId = id;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setColumns(List<ColumnDTO> columns) {
        this.columns = columns;
    }

    public List<ColumnDTO> getColumns() {
        return columns;
    }
}
