package com.techtron.onebook.module.app.service.article;

import cn.hutool.core.collection.CollUtil;
import com.techtron.onebook.framework.common.pojo.PageResult;
import com.techtron.onebook.module.app.controller.admin.article.vo.article.ArticleCreateReqVO;
import com.techtron.onebook.module.app.controller.admin.article.vo.article.ArticlePageReqVO;
import com.techtron.onebook.module.app.controller.admin.article.vo.article.ArticleUpdateReqVO;
import com.techtron.onebook.module.app.controller.app.article.vo.article.AppArticlePageReqVO;
import com.techtron.onebook.module.app.convert.article.ArticleConvert;
import com.techtron.onebook.module.app.dal.dataobject.article.ArticleDO;
import com.techtron.onebook.module.app.dal.mysql.article.ArticleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.techtron.onebook.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.techtron.onebook.module.app.enums.ErrorCodeConstants.ARTICLE_NOT_EXISTS;

/**
 * 文章管理 Service 实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Override
    public Long createArticle(ArticleCreateReqVO createReqVO) {

        // 插入
        ArticleDO article = ArticleConvert.INSTANCE.convert(createReqVO);
        article.setBrowseCount(0); // 初始浏览量
        articleMapper.insert(article);
        // 返回
        return article.getId();
    }

    @Override
    public void updateArticle(ArticleUpdateReqVO updateReqVO) {
        // 校验存在
        validateArticleExists(updateReqVO.getId());

        // 更新
        ArticleDO updateObj = ArticleConvert.INSTANCE.convert(updateReqVO);
        articleMapper.updateById(updateObj);
    }

    @Override
    public void deleteArticle(Long id) {
        // 校验存在
        validateArticleExists(id);
        // 删除
        articleMapper.deleteById(id);
    }

    private void validateArticleExists(Long id) {
        if (articleMapper.selectById(id) == null) {
            throw exception(ARTICLE_NOT_EXISTS);
        }
    }

    @Override
    public ArticleDO getArticle(Long id) {
        return articleMapper.selectById(id);
    }

    @Override
    public ArticleDO getLastArticleByTitle(String title) {
        List<ArticleDO> articles = articleMapper.selectListByTitle(title);
        return CollUtil.getLast(articles);
    }

    @Override
    public PageResult<ArticleDO> getArticlePage(ArticlePageReqVO pageReqVO) {
        return articleMapper.selectPage(pageReqVO);
    }

    @Override
    public List<ArticleDO> getArticleCategoryListByRecommend(Boolean recommendHot, Boolean recommendBanner) {
        return articleMapper.selectList(recommendHot, recommendBanner);
    }

    @Override
    public PageResult<ArticleDO> getArticlePage(AppArticlePageReqVO pageReqVO) {
        return articleMapper.selectPage(pageReqVO);
    }

    @Override
    public Long getArticleCountByCategoryId(Long categoryId) {
        return articleMapper.selectCount(ArticleDO::getCategoryId, categoryId);
    }

    @Override
    public void addArticleBrowseCount(Long id) {
        // 校验文章是否存在
        validateArticleExists(id);
        // 增加浏览次数
        articleMapper.updateBrowseCount(id);
    }

}
