package com.kiss.yishun.dao;

import com.kiss.yishun.entity.SmartConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartConfigDao extends JpaRepository<SmartConfig, Long> {

    SmartConfig findValueByNameEquals(String key);
}
