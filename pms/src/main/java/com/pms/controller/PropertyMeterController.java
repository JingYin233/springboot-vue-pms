package com.pms.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.QueryPageParam;
import com.pms.common.Result;
import com.pms.dto.PropertyMeterPropertyDTO;
import com.pms.entity.MeterData;
import com.pms.entity.PropertyMeter;
import com.pms.entity.User;
import com.pms.service.MeterDataService;
import com.pms.service.PropertyMeterService;
import com.pms.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author pms
 * @since 2023-12-10
 */
@RestController
@RequestMapping("/property-meter")
public class PropertyMeterController {
    @Autowired
    private PropertyMeterService propertyMeterService;

    @Autowired
    private PropertyController propertyController;

    @Autowired
    private MeterDataService meterDataService;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "分页查询仪表信息", notes = "在验证用户登录状态后，根据查询参数进行分页查询仪表信息")
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query, HttpServletRequest request) {
        // 从请求中获取Cookie
        Cookie[] cookies = request.getCookies();
        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("userId")) {
                    userId = cookie.getValue();
                    break;
                }
            }
        }

        // 用户未登录
        if(userId == null) {
            return Result.fail("User is not logged in");
        }

        User user = userService.getById(Integer.valueOf(userId));
        HashMap hashMap = query.getParam();
        String equipmentName = (String) hashMap.get("equipmentName");
        String equipmentStatus = (String)hashMap.get("equipmentStatus");
        String lastMaintenanceStartDate = (String)hashMap.get("lastMaintenanceStartDate");
        String lastMaintenanceEndDate = (String)hashMap.get("lastMaintenanceEndDate");

        // 创建一个Map对象来存储查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("communityId", user.getCommunityId());
        params.put("equipmentName", equipmentName);
        params.put("equipmentStatus", equipmentStatus);
        params.put("lastMaintenanceStartDate", lastMaintenanceStartDate);
        params.put("lastMaintenanceEndDate", lastMaintenanceEndDate);

        Page<PropertyMeterPropertyDTO> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        IPage result = propertyMeterService.getPropertyMetersWithProperty(page, params);

        return Result.suc(result.getRecords(), result.getTotal());
    }

    @ApiOperation(value = "新增物业仪表信息", notes = "在验证用户登录状态后，新增物业仪表信息")
    @PostMapping("/save")
    public Result save(@RequestBody PropertyMeter propertyMeter, HttpServletRequest request) {
        // 从请求中获取Cookie
        Cookie[] cookies = request.getCookies();
        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("userId")) {
                    userId = cookie.getValue();
                    break;
                }
            }
        }

        // 用户未登录
        if(userId == null) {
            return Result.fail("User is not logged in");
        }

        User user = userService.getById(Integer.valueOf(userId));
        // 根据communityId查询property表以获取物业主键
        Integer propertyId = propertyController.getPropertyIdByCommunityId(user.getCommunityId());

        // 将物业主键赋值给propertyMeter对象
        propertyMeter.setPropertyId(propertyId);

        // 保存propertyMeter对象
        boolean save = propertyMeterService.save(propertyMeter);

        return save ? Result.suc() : Result.fail();
    }

    @Transactional
    @ApiOperation(value = "删除物业仪表信息", notes = "根据提供的物业仪表ID删除对应的物业仪表信息以及相关的数据信息")
    @GetMapping("/delete")
    public boolean delete(Integer id) {
        // 先删除meter_data表中的相关记录
        LambdaQueryWrapper<MeterData> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(MeterData::getMeterId, id);
        meterDataService.remove(lambdaQueryWrapper);

        // 然后删除property_meter表中的记录
        return propertyMeterService.removeById(id);
    }

    @ApiOperation(value = "更新物业仪表信息", notes = "在验证用户登录状态后，根据提供的物业仪表信息更新对应的记录")
    @PostMapping("/update")
    public Result update(@RequestBody PropertyMeter propertyMeter, HttpServletRequest request) {
        // 从请求中获取Cookie
        Cookie[] cookies = request.getCookies();
        String userId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("userId")) {
                    userId = cookie.getValue();
                    break;
                }
            }
        }

        // 用户未登录
        if(userId == null) {
            return Result.fail("User is not logged in");
        }

        return propertyMeterService.updateById(propertyMeter)?Result.suc():Result.fail();
    }
}
