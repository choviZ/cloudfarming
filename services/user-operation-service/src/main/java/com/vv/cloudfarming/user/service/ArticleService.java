package com.vv.cloudfarming.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.user.dao.entity.ArticleDO;
import com.vv.cloudfarming.user.dto.req.ArticlePageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.ArticlePublishReqDTO;
import com.vv.cloudfarming.user.dto.req.ArticleUpdateReqDTO;
import com.vv.cloudfarming.user.dto.resp.ArticleRespDTO;

public interface ArticleService extends IService<ArticleDO> {

    Long publishArticle(Long operatorId, ArticlePublishReqDTO requestParam);

    boolean updateArticle(ArticleUpdateReqDTO requestParam);

    boolean deleteArticle(Long id);

    void updateArticleStatus(Long id, Integer status);

    ArticleRespDTO getPublishedArticleById(Long id);

    ArticleRespDTO getArticleById(Long id);

    IPage<ArticleRespDTO> pagePublishedArticles(ArticlePageQueryReqDTO requestParam);

    IPage<ArticleRespDTO> pageManageArticles(ArticlePageQueryReqDTO requestParam);
}
