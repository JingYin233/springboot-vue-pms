package com.pms.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.QueryPageParam;
import com.pms.common.Result;
import com.pms.dto.EquipmentPropertyDTO;
import com.pms.dto.FeeItemPropertyDTO;
import com.pms.entity.Equipment;
import com.pms.entity.FeeItem;
import com.pms.entity.User;
import com.pms.service.EquipmentService;
import com.pms.service.FeeItemService;
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
@RequestMapping("/fee-item")
public class FeeItemController {

    @Autowired
    private FeeItemService feeItemService;

    @Autowired
    private PropertyController propertyController;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "分页查询收费项目信息", notes = "在验证用户登录状态后，根据查询参数进行分页查询收费项目信息")
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
        String feeItemName = (String) hashMap.get("feeItemName");

        // 创建一个Map对象来存储查询参数
        Map<String, Object> params = new HashMap<>();
        params.put("communityId", user.getCommunityId());
        params.put("feeItemName", feeItemName);

        Page<FeeItemPropertyDTO> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        IPage result = feeItemService.getFeeItemsWithProperty(page, params);

        return Result.suc(result.getRecords(), result.getTotal());
    }

    @ApiOperation(value = "删除收费项目信息", notes = "根据提供的收费项目ID删除对应的收费项目信息")
    @GetMapping("/delete")
    public boolean delete(Integer id) {
        return feeItemService.removeById(id);
    }

    @ApiOperation(value = "更新费用项", notes = "根据费用项的Id更新单条记录")
    @PostMapping("/update")
    public Result update(@RequestBody FeeItem feeItem) {
        System.out.println(feeItem);
        return feeItemService.updateById(feeItem) ? Result.suc() : Result.fail();
    }



    @ApiOperation(value = "新增", notes = "收费项目的新增接口")
    @PostMapping("/saveFeeItem")
    public Result saveFeeItem(@RequestBody FeeItem feeItem, HttpSession session) {
        // 从session中获取communityId
        Integer communityId = (Integer) session.getAttribute("communityId");

        // 根据communityId查询property表以获取物业主键
        Integer propertyId = propertyController.getPropertyIdByCommunityId(communityId);

        // 将物业主键赋值给feeItem对象
        feeItem.setPropertyId(propertyId);

        // 保存feeItem对象
        boolean save = feeItemService.save(feeItem);

        return save ? Result.suc() : Result.fail();
    }

}
