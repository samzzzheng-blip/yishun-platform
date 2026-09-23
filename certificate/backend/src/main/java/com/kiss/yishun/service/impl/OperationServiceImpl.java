package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.OperationDao;
import com.kiss.yishun.entity.Operation;
import com.kiss.yishun.service.OperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    private OperationDao operationDao;

    @Override
    public Operation findOperationByCode(String code) {
        return operationDao.findOperationByCode(code);
    }

    @Override
    public void updateOperation(Operation Operation) {
        operationDao.saveAndFlush(Operation);
    }

    @Override
    public List<Operation> findOperationList() {
        return operationDao.findAll();
    }

    @Override
    public Operation findOperationById(long id) {
        return operationDao.findOperationById(id);
    }

    @Override
    public void deleteOperation(long id) {
        operationDao.deleteOperationById(id);
    }


    @Override
    public void addOperation(Operation Operation) {
        operationDao.save(Operation);
    }
}
