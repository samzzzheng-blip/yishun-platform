package com.techtron.onebook.module.app.controller.admin.ads;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.CommonResult;
import com.techtron.onebook.framework.common.pojo.PageParam;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.dal.dataobject.yikoujia.YikoujiaDO;
import com.techtron.onebook.module.app.dal.mysql.yikoujia.YikoujiaMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/ads")
public class AdsProductController {
    @Resource
    private YikoujiaMapper yikoujiaMapper;

    public record ProductOption(Long id, String name, Integer price) {}

    @GetMapping("/product-options")
    @PreAuthorize("@ss.hasAnyPermissions('app:ads:create', 'app:ads:update')")
    public CommonResult<PageResult<ProductOption>> options(@Valid PageParam page,
            @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        var query = new LambdaQueryWrapper<YikoujiaDO>()
                .select(YikoujiaDO::getId, YikoujiaDO::getName, YikoujiaDO::getPrice)
                .eq(YikoujiaDO::getStatus, 3).orderByDesc(YikoujiaDO::getId);
        String term = keyword.trim();
        if (!term.isEmpty()) {
            query.and(q -> {
                q.like(YikoujiaDO::getName, term);
                if (term.matches("[0-9]{1,18}")) q.or().eq(YikoujiaDO::getId, Long.valueOf(term));
            });
        }
        var result = yikoujiaMapper.selectPage(page, query);
        return CommonResult.success(new PageResult<>(result.getList().stream()
                .map(p -> new ProductOption(p.getId(), p.getName(), p.getPrice())).toList(), result.getTotal()));
    }
}
