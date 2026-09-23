package com.kiss.yishun.dao;

import com.kiss.yishun.entity.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface OperationDao extends JpaRepository<Operation, Long> {

    Operation findOperationByCode(String code);

    Page<Operation> findAllByCode(String keywords, Pageable pageable);

    Operation findOperationById(long id);

    @Transactional
    void deleteOperationById(long id);

}
