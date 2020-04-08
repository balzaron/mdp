package com.miotech.mdp.common.model.vo;

import lombok.Data;

@Data
public class PageVO {

    private Integer pageNum;

    private Integer pageSize;

    private Long totalCount;

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum + 1;
    }
}
