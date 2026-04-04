package com.vv.cloudfarming.user.dto.resp;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Admin farmer page response.
 */
@Data
public class FarmerAdminPageRespDTO {

    private Long id;

    private Long userId;

    private String farmName;

    private String breedingType;

    private Double farmArea;

    private Integer reviewStatus;

    private String businessLicensePic;

    private List<String> environmentImages;

    private Integer featuredFlag;

    private Date updateTime;
}
