package com.vv.cloudfarming.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vv.cloudfarming.user.dao.entity.FarmerDO;
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

/**
 * Farmer service.
 */
public interface FarmerService extends IService<FarmerDO> {

    void submitApply(FarmerApplyReqDO requestParam);

    FarmerReviewRespDTO getReviewStatus();

    void updateReviewState(UpdateReviewStatusReqDTO requestParam);

    IPage<FarmerShowcaseRespDTO> pageShowcase(FarmerShowcasePageReqDTO requestParam);

    FarmerShowcaseRespDTO getShowcaseDetail(Long id);

    FarmerMyShowcaseRespDTO getMyShowcase();

    Boolean updateMyShowcase(FarmerShowcaseUpdateReqDTO requestParam);

    IPage<FarmerAdminPageRespDTO> pageAdminFarmers(FarmerAdminPageReqDTO requestParam);

    Boolean updateFeaturedFlag(FarmerFeatureUpdateReqDTO requestParam);
}
