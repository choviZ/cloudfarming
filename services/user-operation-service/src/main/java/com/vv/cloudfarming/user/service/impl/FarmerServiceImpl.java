package com.vv.cloudfarming.user.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vv.cloudfarming.common.enums.ReviewStatusEnum;
import com.vv.cloudfarming.common.exception.ClientException;
import com.vv.cloudfarming.common.exception.ServiceException;
import com.vv.cloudfarming.user.dao.entity.FarmerDO;
import com.vv.cloudfarming.user.dao.entity.UserDO;
import com.vv.cloudfarming.user.dao.mapper.FarmerMapper;
import com.vv.cloudfarming.user.dto.req.FarmerAdminPageReqDTO;
import com.vv.cloudfarming.user.dto.req.FarmerApplyReqDO;
import com.vv.cloudfarming.user.dto.req.FarmerFeatureUpdateReqDTO;
import com.vv.cloudfarming.user.dto.req.FarmerShowcasePageReqDTO;
import com.vv.cloudfarming.user.dto.req.FarmerShowcaseUpdateReqDTO;
import com.vv.cloudfarming.user.dto.req.UpdateReviewStatusReqDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerAdminPageRespDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerMyShowcaseRespDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerReviewRespDTO;
import com.vv.cloudfarming.user.dto.resp.FarmerShowcaseRespDTO;
import com.vv.cloudfarming.user.service.FarmerService;
import com.vv.cloudfarming.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Farmer service implementation.
 */
@Service
@RequiredArgsConstructor
public class FarmerServiceImpl extends ServiceImpl<FarmerMapper, FarmerDO> implements FarmerService {

    private static final int FEATURED_ON = 1;
    private static final int FEATURED_OFF = 0;
    private static final int MAX_ENVIRONMENT_IMAGE_COUNT = 6;

    private final UserService userService;
    private final StpInterface stpInterface;

    @Override
    public void submitApply(FarmerApplyReqDO requestParam) {
        long loginId = StpUtil.getLoginIdAsLong();
        FarmerDO farmerDO = BeanUtil.toBean(requestParam, FarmerDO.class);
        farmerDO.setUserId(loginId);
        farmerDO.setReviewStatus(ReviewStatusEnum.PENDING.getStatus());
        farmerDO.setFeaturedFlag(FEATURED_OFF);
        int inserted = baseMapper.insert(farmerDO);
        if (inserted < 1) {
            throw new ServiceException("申请失败");
        }
    }

    @Override
    public FarmerReviewRespDTO getReviewStatus() {
        long loginId = StpUtil.getLoginIdAsLong();
        FarmerDO farmerDO = findLatestFarmerByUserId(loginId);
        if (farmerDO == null) {
            throw new ClientException("您未申请过成为入驻农户");
        }

        ReviewStatusEnum statusEnum = ReviewStatusEnum.getByStatus(farmerDO.getReviewStatus());
        return FarmerReviewRespDTO.builder()
                .status(statusEnum == null ? "未知状态" : statusEnum.getDesc())
                .reviewRemark(farmerDO.getReviewRemark())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateReviewState(UpdateReviewStatusReqDTO requestParam) {
        String remark = requestParam.getRemark();
        ReviewStatusEnum statusEnum = ReviewStatusEnum.getByStatus(requestParam.getStatus());
        if (statusEnum == null) {
            throw new ClientException("不存在的状态");
        }

        FarmerDO farmerDO = baseMapper.selectById(requestParam.getId());
        if (farmerDO == null) {
            throw new ClientException("请先申请成为农户");
        }

        long loginId = StpUtil.getLoginIdAsLong();
        LambdaUpdateWrapper<FarmerDO> wrapper = Wrappers.lambdaUpdate(FarmerDO.class)
                .eq(FarmerDO::getId, requestParam.getId())
                .set(FarmerDO::getReviewStatus, statusEnum.getStatus())
                .set(FarmerDO::getReviewUserId, loginId)
                .set(StrUtil.isNotBlank(remark), FarmerDO::getReviewRemark, remark);
        int updated = baseMapper.update(wrapper);

        boolean updatedUserType = true;
        if (ObjectUtil.equals(statusEnum.getStatus(), ReviewStatusEnum.APPROVED.getStatus())) {
            UserDO userDO = userService.getById(farmerDO.getUserId());
            if (userDO != null && !ObjectUtil.equals(userDO.getUserType(), 1)) {
                LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                        .eq(UserDO::getId, farmerDO.getUserId())
                        .set(UserDO::getUserType, 1);
                updatedUserType = userService.update(updateWrapper);
            }
        }
        if (updated < 1 || !updatedUserType) {
            throw new ServiceException("更新状态失败，请稍后重试");
        }

        stpInterface.getRoleList(loginId, null);
    }

    @Override
    public IPage<FarmerShowcaseRespDTO> pageShowcase(FarmerShowcasePageReqDTO requestParam) {
        LambdaQueryWrapper<FarmerDO> wrapper = buildShowcaseWrapper()
                .orderByDesc(FarmerDO::getUpdateTime)
                .orderByDesc(FarmerDO::getId);
        IPage<FarmerDO> pageResult = baseMapper.selectPage(requestParam, wrapper);
        return pageResult.convert(this::toShowcaseResp);
    }

    @Override
    public FarmerShowcaseRespDTO getShowcaseDetail(Long id) {
        if (id == null) {
            throw new ClientException("农户ID不能为空");
        }

        LambdaQueryWrapper<FarmerDO> wrapper = buildShowcaseWrapper()
                .eq(FarmerDO::getId, id);
        FarmerDO farmerDO = baseMapper.selectOne(wrapper);
        if (farmerDO == null) {
            throw new ClientException("农户不存在或暂未开放展示");
        }
        return toShowcaseResp(farmerDO);
    }

    @Override
    public FarmerMyShowcaseRespDTO getMyShowcase() {
        long loginId = StpUtil.getLoginIdAsLong();
        FarmerDO farmerDO = findLatestFarmerByUserId(loginId);
        if (farmerDO == null) {
            throw new ClientException("请先提交农户入驻申请");
        }
        return toMyShowcaseResp(farmerDO);
    }

    @Override
    public Boolean updateMyShowcase(FarmerShowcaseUpdateReqDTO requestParam) {
        long loginId = StpUtil.getLoginIdAsLong();
        FarmerDO farmerDO = findLatestFarmerByUserId(loginId);
        if (farmerDO == null) {
            throw new ClientException("请先提交农户入驻申请");
        }

        List<String> environmentImages = normalizeImageUrls(requestParam.getEnvironmentImages());
        if (environmentImages.size() > MAX_ENVIRONMENT_IMAGE_COUNT) {
            throw new ClientException("环境图片最多上传 6 张");
        }

        LambdaUpdateWrapper<FarmerDO> wrapper = Wrappers.lambdaUpdate(FarmerDO.class)
                .eq(FarmerDO::getId, farmerDO.getId())
                .set(FarmerDO::getEnvironmentImages, joinImages(environmentImages));
        int updated = baseMapper.update(wrapper);
        if (updated < 1) {
            throw new ServiceException("更新展示资料失败");
        }
        return true;
    }

    @Override
    public IPage<FarmerAdminPageRespDTO> pageAdminFarmers(FarmerAdminPageReqDTO requestParam) {
        LambdaQueryWrapper<FarmerDO> wrapper = Wrappers.lambdaQuery(FarmerDO.class)
                .like(StrUtil.isNotBlank(requestParam.getFarmName()), FarmerDO::getFarmName, StrUtil.trim(requestParam.getFarmName()))
                .like(StrUtil.isNotBlank(requestParam.getBreedingType()), FarmerDO::getBreedingType, StrUtil.trim(requestParam.getBreedingType()))
                .eq(ObjectUtil.isNotNull(requestParam.getReviewStatus()), FarmerDO::getReviewStatus, requestParam.getReviewStatus())
                .eq(ObjectUtil.isNotNull(requestParam.getFeaturedFlag()), FarmerDO::getFeaturedFlag, requestParam.getFeaturedFlag())
                .orderByDesc(FarmerDO::getUpdateTime)
                .orderByDesc(FarmerDO::getId);

        IPage<FarmerDO> pageResult = baseMapper.selectPage(requestParam, wrapper);
        return pageResult.convert(this::toAdminPageResp);
    }

    @Override
    public Boolean updateFeaturedFlag(FarmerFeatureUpdateReqDTO requestParam) {
        FarmerDO farmerDO = baseMapper.selectById(requestParam.getId());
        if (farmerDO == null) {
            throw new ClientException("农户不存在");
        }

        Integer targetFlag = requestParam.getFeaturedFlag();
        if (FEATURED_ON == targetFlag) {
            if (!ObjectUtil.equals(farmerDO.getReviewStatus(), ReviewStatusEnum.APPROVED.getStatus())) {
                throw new ClientException("仅审核通过的农户可以设为精选");
            }
            if (!hasShowcaseAssets(farmerDO)) {
                throw new ClientException("请先补充环境图片和营业执照图片后再设为精选");
            }
        }

        LambdaUpdateWrapper<FarmerDO> wrapper = Wrappers.lambdaUpdate(FarmerDO.class)
                .eq(FarmerDO::getId, requestParam.getId())
                .set(FarmerDO::getFeaturedFlag, targetFlag);
        int updated = baseMapper.update(wrapper);
        if (updated < 1) {
            throw new ServiceException("更新精选状态失败");
        }
        return true;
    }

    private LambdaQueryWrapper<FarmerDO> buildShowcaseWrapper() {
        return Wrappers.lambdaQuery(FarmerDO.class)
                .eq(FarmerDO::getReviewStatus, ReviewStatusEnum.APPROVED.getStatus())
                .eq(FarmerDO::getFeaturedFlag, FEATURED_ON)
                .isNotNull(FarmerDO::getEnvironmentImages)
                .ne(FarmerDO::getEnvironmentImages, "")
                .isNotNull(FarmerDO::getBusinessLicensePic)
                .ne(FarmerDO::getBusinessLicensePic, "");
    }

    private FarmerDO findLatestFarmerByUserId(Long userId) {
        LambdaQueryWrapper<FarmerDO> wrapper = Wrappers.lambdaQuery(FarmerDO.class)
                .eq(FarmerDO::getUserId, userId)
                .orderByDesc(FarmerDO::getCreateTime)
                .orderByDesc(FarmerDO::getId)
                .last("limit 1");
        return baseMapper.selectOne(wrapper);
    }

    private FarmerShowcaseRespDTO toShowcaseResp(FarmerDO farmerDO) {
        FarmerShowcaseRespDTO respDTO = new FarmerShowcaseRespDTO();
        respDTO.setId(farmerDO.getId());
        respDTO.setFarmName(farmerDO.getFarmName());
        respDTO.setBreedingType(farmerDO.getBreedingType());
        respDTO.setFarmArea(farmerDO.getFarmArea());
        respDTO.setFarmAddressMasked(maskFarmAddress(farmerDO.getFarmAddress()));
        respDTO.setEnvironmentImages(splitImages(farmerDO.getEnvironmentImages()));
        respDTO.setBusinessLicensePic(farmerDO.getBusinessLicensePic());
        return respDTO;
    }

    private FarmerMyShowcaseRespDTO toMyShowcaseResp(FarmerDO farmerDO) {
        FarmerMyShowcaseRespDTO respDTO = new FarmerMyShowcaseRespDTO();
        respDTO.setId(farmerDO.getId());
        respDTO.setFarmName(farmerDO.getFarmName());
        respDTO.setBreedingType(farmerDO.getBreedingType());
        respDTO.setFarmArea(farmerDO.getFarmArea());
        respDTO.setFarmAddress(farmerDO.getFarmAddress());
        respDTO.setBusinessLicensePic(farmerDO.getBusinessLicensePic());
        respDTO.setEnvironmentImages(splitImages(farmerDO.getEnvironmentImages()));
        respDTO.setReviewStatus(farmerDO.getReviewStatus());
        respDTO.setFeaturedFlag(ObjectUtil.defaultIfNull(farmerDO.getFeaturedFlag(), FEATURED_OFF));
        return respDTO;
    }

    private FarmerAdminPageRespDTO toAdminPageResp(FarmerDO farmerDO) {
        FarmerAdminPageRespDTO respDTO = new FarmerAdminPageRespDTO();
        respDTO.setId(farmerDO.getId());
        respDTO.setUserId(farmerDO.getUserId());
        respDTO.setFarmName(farmerDO.getFarmName());
        respDTO.setBreedingType(farmerDO.getBreedingType());
        respDTO.setFarmArea(farmerDO.getFarmArea());
        respDTO.setReviewStatus(farmerDO.getReviewStatus());
        respDTO.setBusinessLicensePic(farmerDO.getBusinessLicensePic());
        respDTO.setEnvironmentImages(splitImages(farmerDO.getEnvironmentImages()));
        respDTO.setFeaturedFlag(ObjectUtil.defaultIfNull(farmerDO.getFeaturedFlag(), FEATURED_OFF));
        respDTO.setUpdateTime(farmerDO.getUpdateTime());
        return respDTO;
    }

    private List<String> splitImages(String imageValue) {
        if (StrUtil.isBlank(imageValue)) {
            return Collections.emptyList();
        }
        return StrUtil.splitTrim(imageValue, ',')
                .stream()
                .filter(StrUtil::isNotBlank)
                .collect(Collectors.toList());
    }

    private String joinImages(List<String> imageList) {
        if (CollUtil.isEmpty(imageList)) {
            return "";
        }
        return String.join(",", imageList);
    }

    private List<String> normalizeImageUrls(List<String> imageList) {
        if (CollUtil.isEmpty(imageList)) {
            return Collections.emptyList();
        }
        return imageList.stream()
                .filter(StrUtil::isNotBlank)
                .map(StrUtil::trim)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
    }

    private boolean hasShowcaseAssets(FarmerDO farmerDO) {
        return StrUtil.isNotBlank(farmerDO.getBusinessLicensePic())
                && StrUtil.isNotBlank(farmerDO.getEnvironmentImages());
    }

    private String maskFarmAddress(String farmAddress) {
        if (StrUtil.isBlank(farmAddress)) {
            return "";
        }

        String trimmedAddress = StrUtil.trim(farmAddress);
        int cityIndex = -1;
        for (int i = 0; i < trimmedAddress.length(); i++) {
            char each = trimmedAddress.charAt(i);
            if (each == '市' && cityIndex < 0) {
                cityIndex = i;
            }
            if (each == '区' || each == '县') {
                return trimmedAddress.substring(0, i + 1);
            }
        }

        if (cityIndex >= 0) {
            return trimmedAddress.substring(0, cityIndex + 1);
        }

        if (trimmedAddress.length() <= 8) {
            return trimmedAddress;
        }
        return trimmedAddress.substring(0, 8) + "...";
    }
}
