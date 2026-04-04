package com.vv.cloudfarming.user.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * Public farmer showcase response.
 */
@Data
public class FarmerShowcaseRespDTO {

    private Long id;

    private String farmName;

    private String breedingType;

    private Double farmArea;

    private String farmAddressMasked;

    private List<String> environmentImages;

    private String businessLicensePic;
}
