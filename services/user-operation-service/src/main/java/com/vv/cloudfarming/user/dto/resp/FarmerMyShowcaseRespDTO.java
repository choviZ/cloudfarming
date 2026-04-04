package com.vv.cloudfarming.user.dto.resp;

import lombok.Data;

import java.util.List;

/**
 * Farmer self showcase response.
 */
@Data
public class FarmerMyShowcaseRespDTO {

    private Long id;

    private String farmName;

    private String breedingType;

    private Double farmArea;

    private String farmAddress;

    private String businessLicensePic;

    private List<String> environmentImages;

    private Integer reviewStatus;

    private Integer featuredFlag;
}
