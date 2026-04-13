package org.kiteseven.kiteuniverse.common.util;

import org.kiteseven.kiteuniverse.common.constant.CommonConstants;

public final class PageUtils {

    private PageUtils() {
    }

    public static int normalizePageNum(Integer pageNum) {
        if (pageNum == null || pageNum < CommonConstants.DEFAULT_PAGE_NUM) {
            return CommonConstants.DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    public static int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) {
            return CommonConstants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, CommonConstants.MAX_PAGE_SIZE);
    }
}
