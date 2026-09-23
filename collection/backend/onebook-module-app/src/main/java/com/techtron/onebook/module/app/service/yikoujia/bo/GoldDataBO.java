package com.techtron.onebook.module.app.service.yikoujia.bo;

import lombok.Data;

import java.util.List;

@Data
public class GoldDataBO {
    private List<GoldfishBO> list;

    private Integer count;

    private Integer page_size;
    private Integer page_no;
}
