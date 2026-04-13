package org.kiteseven.kiteuniverse.pojo.dto;

import org.kiteseven.kiteuniverse.common.util.PageUtils;

public class BasePageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public Integer getPageNum() {
        return PageUtils.normalizePageNum(pageNum);
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return PageUtils.normalizePageSize(pageSize);
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
