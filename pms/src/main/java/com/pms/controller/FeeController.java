package com.pms.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.QueryPageParam;
import com.pms.common.Result;
import com.pms.dto.FeeResidentItemDTO;
import com.pms.entity.Fee;
import com.pms.entity.User;
import com.pms.service.FeeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/fee")
public class FeeController {
    @Autowired
    private FeeService feeService;

    @Autowired
    private ResidentController residentController;

    @Autowired
    private PropertyController propertyController;

    @ApiOperation(value = "删除", notes = "根据complaintId删除单条记录")
    @GetMapping("/delete")
    public boolean delete(Integer id) {
        return feeService.removeById(id);
    }

    @ApiOperation(value = "更新", notes = "根据complaintId更新单条记录")
    @PostMapping("/update")
    public Result update(@RequestBody Fee fee, HttpSession session) {
        System.out.println(fee);
        return feeService.updateById(fee)?Result.suc():Result.fail();
    }

    @ApiOperation(value = "查询分页", notes = "分页查询收费项目")
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query, HttpSession session) {
        HashMap hashMap = query.getParam();
        String residentName = (String) hashMap.get("residentName");
        String feeItemName = (String) hashMap.get("feeItemName");
        String status = (String)hashMap.get("status");
        String startDate = (String)hashMap.get("startDate");
        String endDate = (String)hashMap.get("endDate");

        User user = (User) session.getAttribute("user");

        if (user != null) {
            // 创建一个Map对象来存储查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("communityId", user.getCommunityId());
            params.put("residentName", residentName);
            params.put("feeItemName", feeItemName);
            params.put("status", status);
            params.put("startDate", startDate);
            params.put("endDate", endDate);


            Page<FeeResidentItemDTO> page = new Page();
            page.setCurrent(query.getPageNum());
            page.setSize(query.getPageSize());

            IPage result = feeService.getFeesWithResidents(page, params);

            return Result.suc(result.getRecords(), result.getTotal());
        } else {
            // 用户未登录
            return Result.fail();
        }
    }

    @ApiOperation(value = "新增收费项目", notes = "需要输入Fee的json数据，同时输入单元号和房间号得到住户主键，通过session获得物业主键")
    @PostMapping("/save")
    public Result save(@RequestBody Fee fee, String unitNumber, String roomNumber, HttpSession session) {
        // 检查fee是否为null
        if (fee == null) {
            return Result.fail("Fee cannot be null");
        }

        // 从session中获取communityId
        Integer communityId = (Integer) session.getAttribute("communityId");

        // 根据communityId查询property表以获取物业主键
        Integer propertyId = propertyController.getPropertyIdByCommunityId(communityId);

        // 将物业主键赋值给fee对象
        fee.setPropertyId(propertyId);

        // 根据单元号和房间号查询resident表以获取住户主键
        Integer residentId = residentController.getResidentIdByUnitAndRoom(unitNumber, roomNumber);

        // 将住户主键赋值给fee对象
        fee.setResidentId(residentId);

        // 保存fee对象
        boolean save = feeService.save(fee);

        return save ? Result.suc() : Result.fail();
    }
}
