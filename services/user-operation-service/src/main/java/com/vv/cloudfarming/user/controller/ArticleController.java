package com.vv.cloudfarming.user.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vv.cloudfarming.common.cosntant.UserRoleConstant;
import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.user.dto.req.ArticlePageQueryReqDTO;
import com.vv.cloudfarming.user.dto.req.ArticlePublishReqDTO;
import com.vv.cloudfarming.user.dto.req.ArticleUpdateReqDTO;
import com.vv.cloudfarming.user.dto.req.ArticleUpdateStatusReqDTO;
import com.vv.cloudfarming.user.dto.resp.ArticleRespDTO;
import com.vv.cloudfarming.user.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文章资讯管理")
@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "发布文章")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/article/publish")
    public Result<Long> publishArticle(@Valid @RequestBody ArticlePublishReqDTO requestParam) {
        Long operatorId = StpUtil.getLoginIdAsLong();
        return Results.success(articleService.publishArticle(operatorId, requestParam));
    }

    @Operation(summary = "更新文章")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/article/update")
    public Result<Boolean> updateArticle(@Valid @RequestBody ArticleUpdateReqDTO requestParam) {
        return Results.success(articleService.updateArticle(requestParam));
    }

    @Operation(summary = "删除文章")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/article/delete")
    public Result<Boolean> deleteArticle(@RequestParam("id") @NotNull Long id) {
        return Results.success(articleService.deleteArticle(id));
    }

    @Operation(summary = "更新文章状态")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/article/status")
    public Result<Void> updateArticleStatus(@Valid @RequestBody ArticleUpdateStatusReqDTO requestParam) {
        articleService.updateArticleStatus(requestParam.getId(), requestParam.getStatus());
        return Results.success();
    }

    @Operation(summary = "管理端查询文章详情")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @GetMapping("/api/article/manage/get")
    public Result<ArticleRespDTO> getArticleById(@RequestParam("id") @NotNull Long id) {
        return Results.success(articleService.getArticleById(id));
    }

    @Operation(summary = "用户端查询已发布文章详情")
    @GetMapping("/api/article/get")
    public Result<ArticleRespDTO> getPublishedArticleById(@RequestParam("id") @NotNull Long id) {
        return Results.success(articleService.getPublishedArticleById(id));
    }

    @Operation(summary = "用户端分页查询已发布文章")
    @PostMapping("/api/article/page")
    public Result<IPage<ArticleRespDTO>> pagePublishedArticles(@RequestBody ArticlePageQueryReqDTO requestParam) {
        return Results.success(articleService.pagePublishedArticles(requestParam));
    }

    @Operation(summary = "管理端分页查询文章")
    @SaCheckRole(UserRoleConstant.ADMIN_DESC)
    @PostMapping("/api/article/manage/page")
    public Result<IPage<ArticleRespDTO>> pageManageArticles(@RequestBody ArticlePageQueryReqDTO requestParam) {
        return Results.success(articleService.pageManageArticles(requestParam));
    }
}
