package com.kiss.yishun.dao;

import com.kiss.yishun.entity.OperLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperLogDao extends JpaRepository<OperLog, Long> {

}
