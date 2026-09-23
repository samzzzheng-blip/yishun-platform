package com.kiss.yishun.service.impl;

import com.kiss.yishun.dao.SmartImageRecordDao;
import com.kiss.yishun.entity.SmartImageRecord;
import com.kiss.yishun.service.SmartImageRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmartImageRecordServiceImpl implements SmartImageRecordService {

    @Autowired
    private SmartImageRecordDao imageRecordDao;

    @Override
    public List<SmartImageRecord> findByFileName(int type, String fileName, Long userId, PageRequest pageRequest) {
        return imageRecordDao.findByUserIdAndTypeAndNameLikeOrderByUpdatedateDesc(userId, type, "%"+fileName+"%", pageRequest);
    }

    @Override
    public List<SmartImageRecord> findLastList(int type, Long userId, PageRequest pageRequest) {
        return imageRecordDao.findAllByUserIdEqualsAndTypeEqualsOrderByUpdatedateDesc(userId, type, pageRequest);
    }

    @Override
    public void addRecord(SmartImageRecord record) {
        imageRecordDao.save(record);
    }

    @Override
    public boolean deleteImageRecord(Long userId, Long id) {
        SmartImageRecord record = imageRecordDao.findById(id).get();
        if (record != null && record.getUserId() == userId) {
            imageRecordDao.deleteById(id);
            return true;
        }
        return false;
    }
}
