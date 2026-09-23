package com.kiss.yishun.controller.admin;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.kiss.yishun.entity.Precious;
import com.kiss.yishun.entity.vo.ExcelPreciousVo;
import com.kiss.yishun.service.PreciousService;
import io.swagger.annotations.Scope;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Scope(name = "prototype", description = "")
public class ExcelPreciousListener extends AnalysisEventListener<ExcelPreciousVo> {
    
    @Autowired
    private PreciousService preciousService;
    
    @Override
    public void invoke(ExcelPreciousVo o, AnalysisContext analysisContext) {
        ExcelPreciousVo preciousVo = new ExcelPreciousVo();
        BeanUtils.copyProperties(o, preciousVo);
        if (StringUtils.isEmpty(preciousVo.getCertNumber()) || StringUtils.isEmpty(preciousVo.getItemType())
            || StringUtils.isEmpty(preciousVo.getSigner()) || StringUtils.isEmpty(preciousVo.getPublishTime())
            || StringUtils.isEmpty(preciousVo.getPublishActivity())
            || StringUtils.isEmpty(preciousVo.getPublishCity())) {
            return;
        }
        Precious precious = new Precious();
        precious.setCertNumber(preciousVo.getCertNumber());
        precious.setItemType(preciousVo.getItemType());
        precious.setSigner(preciousVo.getSigner());
        precious.setPublishTime(preciousVo.getPublishTime());
        precious.setPublishActivity(preciousVo.getPublishActivity());
        precious.setPublishCity(preciousVo.getPublishCity());
        precious.setImgUrl("");
        precious.setCreatedate(System.currentTimeMillis());
        precious.setUpdatedate(System.currentTimeMillis());
        precious.setStatus(0);
        preciousService.addPrecious(precious);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
