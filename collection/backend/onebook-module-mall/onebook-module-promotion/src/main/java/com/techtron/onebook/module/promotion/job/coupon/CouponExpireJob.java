package com.techtron.onebook.module.promotion.job.coupon;

import cn.hutool.core.util.StrUtil;
import com.techtron.onebook.framework.quartz.core.handler.JobHandler;
import com.techtron.onebook.module.promotion.service.coupon.CouponService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 优惠券过期 Job
 *
 * @author owen
 */
@Component
public class CouponExpireJob implements JobHandler {

    @Resource
    private CouponService couponService;

    @Override
    public String execute(String param) {
        int count = couponService.expireCoupon();
        return StrUtil.format("过期优惠券 {} 个", count);
    }

}
