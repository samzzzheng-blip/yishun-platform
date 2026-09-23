package com.techtron.onebook.module.app.service.exchange;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.framework.common.util.object.BeanUtils;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangePageReqVO;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangeRespVO;
import com.techtron.onebook.module.app.controller.admin.exchange.vo.ExchangeSaveReqVO;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeDO;
import com.techtron.onebook.module.app.dal.dataobject.exchange.ExchangeQuestionDO;
import com.techtron.onebook.module.app.dal.mysql.exchange.ExchangeMapper;
import com.techtron.onebook.module.app.dal.mysql.exchange.ExchangeQuestionMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.EXCHANGE_NOT_EXISTS;

/**
 * 兑换品 Service 实现类
 *
 * @author 超级管理员
 */
@Service
@Validated
public class ExchangeServiceImpl implements ExchangeService {

    @Resource
    private ExchangeMapper exchangeMapper;

    @Resource
    private ExchangeQuestionMapper exchangeQuestionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createExchange(ExchangeSaveReqVO createReqVO) {
        // 插入
        ExchangeDO exchange = BeanUtils.toBean(createReqVO, ExchangeDO.class);
        exchangeMapper.insert(exchange);
        List<ExchangeQuestionDO> questions = BeanUtils.toBean(createReqVO.getQuestions(), ExchangeQuestionDO.class);
        questions.forEach(question -> question.setExchangeId(exchange.getId()));
        exchangeQuestionMapper.insertBatch(questions);

        // 返回
        return exchange.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExchange(ExchangeSaveReqVO updateReqVO) {
        // 校验存在
        validateExchangeExists(updateReqVO.getId());
        // 更新
        ExchangeDO updateObj = BeanUtils.toBean(updateReqVO, ExchangeDO.class);
        exchangeMapper.updateById(updateObj);
        exchangeQuestionMapper.delete(new LambdaQueryWrapper<ExchangeQuestionDO>()
                .eq(ExchangeQuestionDO::getExchangeId, updateReqVO.getId()));
        List<ExchangeQuestionDO> questions = BeanUtils.toBean(updateReqVO.getQuestions(), ExchangeQuestionDO.class);
        questions.forEach(question -> question.setExchangeId(updateReqVO.getId()));
        exchangeQuestionMapper.insertBatch(questions);
    }

    @Override
    public void deleteExchange(Long id) {
        // 校验存在
        validateExchangeExists(id);
        // 删除
        exchangeMapper.deleteById(id);
    }

    @Override
        public void deleteExchangeListByIds(List<Long> ids) {
        // 删除
        exchangeMapper.deleteByIds(ids);
        }


    private void validateExchangeExists(Long id) {
        if (exchangeMapper.selectById(id) == null) {
            throw exception(EXCHANGE_NOT_EXISTS);
        }
    }

    @Override
    public ExchangeRespVO getExchange(Long id) {
        ExchangeDO exchange = exchangeMapper.selectById(id);
        ExchangeRespVO exchangeRespVO = BeanUtils.toBean(exchange, ExchangeRespVO.class);
        List<ExchangeQuestionDO> exchangeQuestionDOS = exchangeQuestionMapper.selectList(new LambdaQueryWrapper<ExchangeQuestionDO>().eq(ExchangeQuestionDO::getExchangeId, id));
        exchangeRespVO.setQuestions(exchangeQuestionDOS);
        return exchangeRespVO;
    }

    @Override
    public PageResult<ExchangeDO> getExchangePage(ExchangePageReqVO pageReqVO) {
        return exchangeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ExchangeDO> getExchangeList() {
        return exchangeMapper.selectList(new LambdaQueryWrapper<ExchangeDO>()
                .eq(ExchangeDO::getStatus, 0)
                .orderByAsc(ExchangeDO::getOrdinalPosition)
        );
    }

}