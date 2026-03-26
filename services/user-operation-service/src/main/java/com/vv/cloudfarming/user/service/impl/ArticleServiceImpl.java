package com.vv.cloudfarming.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.user.dao.entity.ArticleDO;
import com.vv.cloudfarming.user.dao.mapper.ArticleMapper;
import com.vv.cloudfarming.user.dto.req.ArticlePageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.ArticlePublishReqDTO;
import com.vv.cloudfarming.user.dto.req.ArticleUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.ArticleRespDTO;
import com.vv.cloudfarming.user.enums.ArticleStatusEnum;
import com.vv.cloudfarming.user.enums.ArticleTypeEnum;
import com.vv.cloudfarming.user.service.ArticleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, ArticleDO> implements ArticleService {

    @Override
    public Long publishArticle(Long operatorId, ArticlePublishReqDTO requestParam) {
        validateArticleType(requestParam.getArticleType());

        ArticleDO article = new ArticleDO();
        article.setTitle(StrUtil.trim(requestParam.getTitle()));
        article.setSummary(buildSummary(requestParam.getSummary(), requestParam.getContent()));
        article.setCoverImage(StrUtil.trim(requestParam.getCoverImage()));
        article.setContent(requestParam.getContent());
        article.setArticleType(requestParam.getArticleType());
        article.setStatus(ArticleStatusEnum.PUBLISHED.getCode());
        article.setIsTop(Boolean.TRUE.equals(requestParam.getIsTop()));
        article.setAuthorId(operatorId);
        article.setPublishTime(LocalDateTime.now());

        boolean saved = this.save(article);
        if (!saved) {
            throw new ServiceException("文章发布失败");
        }
        return article.getId();
    }

    @Override
    public boolean updateArticle(ArticleUpdateReqDTO requestParam) {
        validateArticleType(requestParam.getArticleType());
        validateArticleStatus(requestParam.getStatus());

        ArticleDO article = getRequiredArticle(requestParam.getId());
        article.setTitle(StrUtil.trim(requestParam.getTitle()));
        article.setSummary(buildSummary(requestParam.getSummary(), requestParam.getContent()));
        article.setCoverImage(StrUtil.trim(requestParam.getCoverImage()));
        article.setContent(requestParam.getContent());
        article.setArticleType(requestParam.getArticleType());
        article.setStatus(requestParam.getStatus());
        article.setIsTop(Boolean.TRUE.equals(requestParam.getIsTop()));
        if (ObjectUtil.equals(requestParam.getStatus(), ArticleStatusEnum.PUBLISHED.getCode())
                && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }

        boolean updated = this.updateById(article);
        if (!updated) {
            throw new ServiceException("文章更新失败");
        }
        return true;
    }

    @Override
    public boolean deleteArticle(Long id) {
        getRequiredArticle(id);
        boolean removed = this.removeById(id);
        if (!removed) {
            throw new ServiceException("文章删除失败");
        }
        return true;
    }

    @Override
    public void updateArticleStatus(Long id, Integer status) {
        validateArticleStatus(status);

        ArticleDO article = getRequiredArticle(id);
        article.setStatus(status);
        if (ObjectUtil.equals(status, ArticleStatusEnum.PUBLISHED.getCode()) && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }

        boolean updated = this.updateById(article);
        if (!updated) {
            throw new ServiceException("文章状态更新失败");
        }
    }

    @Override
    public ArticleRespDTO getPublishedArticleById(Long id) {
        LambdaQueryWrapper<ArticleDO> wrapper = Wrappers.lambdaQuery(ArticleDO.class)
                .eq(ArticleDO::getId, id)
                .eq(ArticleDO::getStatus, ArticleStatusEnum.PUBLISHED.getCode());
        ArticleDO article = this.getOne(wrapper);
        if (article == null) {
            throw new ClientException("文章不存在或已下线");
        }
        return convert(article);
    }

    @Override
    public ArticleRespDTO getArticleById(Long id) {
        return convert(getRequiredArticle(id));
    }

    @Override
    public IPage<ArticleRespDTO> pagePublishedArticles(ArticlePageQueryReqDTO requestParam) {
        LambdaQueryWrapper<ArticleDO> wrapper = buildPageWrapper(requestParam)
                .eq(ArticleDO::getStatus, ArticleStatusEnum.PUBLISHED.getCode());
        IPage<ArticleDO> page = this.page(requestParam, wrapper);
        return page.convert(this::convert);
    }

    @Override
    public IPage<ArticleRespDTO> pageManageArticles(ArticlePageQueryReqDTO requestParam) {
        LambdaQueryWrapper<ArticleDO> wrapper = buildPageWrapper(requestParam)
                .eq(ObjectUtil.isNotNull(requestParam.getStatus()), ArticleDO::getStatus, requestParam.getStatus());
        IPage<ArticleDO> page = this.page(requestParam, wrapper);
        return page.convert(this::convert);
    }

    private LambdaQueryWrapper<ArticleDO> buildPageWrapper(ArticlePageQueryReqDTO requestParam) {
        return Wrappers.lambdaQuery(ArticleDO.class)
                .eq(ObjectUtil.isNotNull(requestParam.getId()), ArticleDO::getId, requestParam.getId())
                .like(StrUtil.isNotBlank(requestParam.getTitle()), ArticleDO::getTitle, StrUtil.trim(requestParam.getTitle()))
                .eq(ObjectUtil.isNotNull(requestParam.getArticleType()), ArticleDO::getArticleType, requestParam.getArticleType())
                .eq(ObjectUtil.isNotNull(requestParam.getIsTop()), ArticleDO::getIsTop, requestParam.getIsTop())
                .orderByDesc(ArticleDO::getIsTop)
                .orderByDesc(ArticleDO::getPublishTime)
                .orderByDesc(ArticleDO::getCreateTime);
    }

    private ArticleDO getRequiredArticle(Long id) {
        if (id == null || id <= 0) {
            throw new ClientException("文章ID不合法");
        }
        ArticleDO article = this.getById(id);
        if (article == null) {
            throw new ClientException("文章不存在");
        }
        return article;
    }

    private void validateArticleType(Integer articleType) {
        if (ArticleTypeEnum.of(articleType) == null) {
            throw new ClientException("文章类型错误");
        }
    }

    private void validateArticleStatus(Integer status) {
        if (ArticleStatusEnum.of(status) == null) {
            throw new ClientException("文章状态错误");
        }
    }

    private String buildSummary(String summary, String content) {
        if (StrUtil.isNotBlank(summary)) {
            return truncate(StrUtil.trim(summary), 300);
        }
        String cleanedContent = StrUtil.trim(content)
                .replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ");
        if (StrUtil.isBlank(cleanedContent)) {
            return null;
        }
        return truncate(cleanedContent, 300);
    }

    private String truncate(String text, int maxLength) {
        if (StrUtil.isBlank(text)) {
            return null;
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength);
    }

    private ArticleRespDTO convert(ArticleDO article) {
        ArticleRespDTO respDTO = BeanUtil.toBean(article, ArticleRespDTO.class);
        ArticleTypeEnum articleTypeEnum = ArticleTypeEnum.of(article.getArticleType());
        respDTO.setArticleTypeDesc(articleTypeEnum == null ? null : articleTypeEnum.getDesc());
        ArticleStatusEnum articleStatusEnum = ArticleStatusEnum.of(article.getStatus());
        respDTO.setStatusDesc(articleStatusEnum == null ? null : articleStatusEnum.getDesc());
        return respDTO;
    }
}
