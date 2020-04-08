package com.miotech.mdp.common.model.bo;

import lombok.Data;

@Data
public class PageInfo {

    private Integer pageNum = 1;

    private Integer pageSize = 25;

    public Integer getPageNum() {
        return this.pageNum - 1;
    }
}
