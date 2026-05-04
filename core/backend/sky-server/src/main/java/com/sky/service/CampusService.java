package com.sky.service;

import com.sky.entity.Campus;

public interface CampusService {

    Campus getDefaultCampus();

    Campus getById(Long id);

    Integer getCampusStatus();

    void updateStatus(Integer status);
}
