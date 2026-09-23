package com.kiss.yishun.dao;

import com.kiss.yishun.entity.SmartSms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartSmsDao extends JpaRepository<SmartSms, Long> {

}
