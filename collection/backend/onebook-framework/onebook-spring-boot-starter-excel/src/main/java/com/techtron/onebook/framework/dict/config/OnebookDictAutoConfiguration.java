package com.techtron.onebook.framework.dict.config;

import com.techtron.onebook.framework.common.biz.system.dict.DictDataCommonApi;
import com.techtron.onebook.framework.dict.core.DictFrameworkUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class OnebookDictAutoConfiguration {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictUtils(DictDataCommonApi dictDataApi) {
        DictFrameworkUtils.init(dictDataApi);
        return new DictFrameworkUtils();
    }

}
