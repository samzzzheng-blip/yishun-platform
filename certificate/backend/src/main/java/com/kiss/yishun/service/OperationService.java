package com.kiss.yishun.service;

import com.kiss.yishun.entity.Operation;

import java.util.List;

public interface OperationService {

    Operation findOperationByCode(String code);
    void updateOperation(Operation Operation);
    Operation findOperationById(long id);
    void deleteOperation(long id);
    void addOperation(Operation role);
    List<Operation> findOperationList();

}
