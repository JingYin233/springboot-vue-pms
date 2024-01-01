package com.pms.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.QueryPageParam;
import com.pms.common.Result;
import com.pms.dto.EquipmentPropertyDTO;
import com.pms.entity.Equipment;
import com.pms.entity.User;
import com.pms.service.EquipmentService;
import com.pms.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private PropertyController propertyController;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "查询设备分页", notes = "在验证用户登录状态后，根据查询参数进行设备的分页查询")
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query, HttpServletRequest request) {
        HashMap hashMap = query.getParam();
        String equipmentName = (String) hashMap.get("equipmentName");
        String equipmentStatus = (String)hashMap.get("equipmentStatus");
        String lastMaintenanceStartDate = (String)hashMap.get("lastMaintenanceStartDate");
        String lastMaintenanceEndDate = (String)hashMap.get("lastMaintenanceEndDate");

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

        // 创建一个Map对象来存储查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("communityId", user.getCommunityId());
        params.put("equipmentName", equipmentName);
        params.put("equipmentStatus", equipmentStatus);
        params.put("lastMaintenanceStartDate", lastMaintenanceStartDate);
        params.put("lastMaintenanceEndDate", lastMaintenanceEndDate);

        Page<EquipmentPropertyDTO> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        IPage result = equipmentService.getEquipmentsWithProperty(page, params);

        return Result.suc(result.getRecords(), result.getTotal());
    }

    @ApiOperation(value = "新增设备", notes = "在验证用户登录状态后，新增设备的接口")
    @PostMapping("/saveEquipment")
    public Result saveEquipment(@RequestBody Equipment equipment, HttpServletRequest request) {
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

        // 根据userId获取User对象
        User user = userService.getById(Integer.valueOf(userId));

        // 根据communityId查询property表以获取物业主键
        Integer propertyId = propertyController.getPropertyIdByCommunityId(user.getCommunityId());

        // 将物业主键赋值给equipment对象
        equipment.setPropertyId(propertyId);

        // 保存equipment对象
        boolean save = equipmentService.save(equipment);

        return save ? Result.suc() : Result.fail();
    }

    @ApiOperation(value = "删除", notes = "根据Id删除单条记录")
    @GetMapping("/delete")
    public boolean delete(Integer id) {
        return equipmentService.removeById(id);
    }

    @ApiOperation(value = "更新设备", notes = "根据设备的Id更新单条记录")
    @PostMapping("/update")
    public Result update(@RequestBody Equipment equipment, HttpSession session) {
        return equipmentService.updateById(equipment) ? Result.suc() : Result.fail();
    }
}
