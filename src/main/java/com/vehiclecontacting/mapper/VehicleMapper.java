package com.vehiclecontacting.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vehiclecontacting.pojo.Vehicle;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VehicleMapper extends BaseMapper<Vehicle> {
}
