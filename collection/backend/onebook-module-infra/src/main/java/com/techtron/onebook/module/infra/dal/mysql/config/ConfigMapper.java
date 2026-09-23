package com.techtron.onebook.module.infra.dal.mysql.config;

import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.mybatis.core.mapper.BaseMapperX;
import com.techtron.onebook.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.techtron.onebook.module.infra.controller.admin.config.vo.ConfigPageReqVO;
import com.techtron.onebook.module.infra.dal.dataobject.config.ConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConfigMapper extends BaseMapperX<ConfigDO> {

    default ConfigDO selectByKey(String key) {
        return selectOne(ConfigDO::getConfigKey, key);
    }

    default PageResult<ConfigDO> selectPage(ConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ConfigDO>()
                .likeIfPresent(ConfigDO::getName, reqVO.getName())
                .likeIfPresent(ConfigDO::getConfigKey, reqVO.getKey())
                .gtIfPresent(ConfigDO::getType, 1)
                .betweenIfPresent(ConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(ConfigDO::getId));
    }

}
