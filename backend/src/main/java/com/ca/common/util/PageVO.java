package com.ca.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PageVO<T> {

    private long total;
    private long page;
    private long size;
    private List<T> items;

    public static <T> PageVO<T> of(IPage<T> page) {
        return new PageVO<>(page.getTotal(), page.getCurrent(), page.getSize(), page.getRecords());
    }

    public static <T> PageVO<T> of(long total, long page, long size, List<T> items) {
        return new PageVO<>(total, page, size, items);
    }
}
