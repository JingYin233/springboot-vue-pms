package com.pms.controller;


import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pms.common.QueryPageParam;
import com.pms.common.Result;
import com.pms.dto.ComplaintResidentDTO;
import com.pms.entity.Complaint;
import com.pms.entity.User;
import com.pms.service.ComplaintService;
import com.pms.service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
@RequestMapping("/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserService userService;

    @ApiOperation(value = "查询投诉分页", notes = "在验证用户登录状态后，根据查询参数进行投诉的分页查询")
    @PostMapping("/listPage")
    public Result listPage(@RequestBody QueryPageParam query, HttpServletRequest request) {
        HashMap hashMap = query.getParam();
        String name = (String) hashMap.get("name");
        String contact = (String) hashMap.get("contact");
        String status = (String)hashMap.get("status");
        String startDate = (String)hashMap.get("startDate");
        String endDate = (String)hashMap.get("endDate");
        String keyword = (String)hashMap.get("keyword");

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
        params.put("name", name);
        params.put("contact", contact);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("keyword", keyword);
        params.put("residentId", user.getResidentId());

        Page<ComplaintResidentDTO> page = new Page();
        page.setCurrent(query.getPageNum());
        page.setSize(query.getPageSize());

        IPage result = complaintService.getComplaintsWithResidents(page, params);

        return Result.suc(result.getRecords(), result.getTotal());
    }

    @ApiOperation(value = "保存投诉请求", notes = "获取用户ID，验证登录状态，然后保存用户的投诉请求")
    @PostMapping("/save")
    public Result save(@RequestBody Complaint complaint, HttpServletRequest request) {
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

        complaint.setResidentId(user.getResidentId());

        boolean save = complaintService.save(complaint);

        return save ? Result.suc() : Result.fail();
    }

    @ApiOperation(value = "删除投诉", notes = "根据投诉的Id删除单条记录")
    @GetMapping("/deleteComplaint")
    public boolean deleteComplaint(Integer id) {
        return complaintService.removeById(id);
    }

    @ApiOperation(value = "更新投诉", notes = "根据投诉的Id更新单条记录")
    @PostMapping("/update")
    public Result update(@RequestBody Complaint complaint, HttpSession session) {
        return complaintService.updateById(complaint) ? Result.suc() : Result.fail();
    }

    @ApiOperation(value = "导出分页查询的投诉信息到Excel文件", notes = "在验证用户登录状态后，根据查询参数进行分页查询投诉信息，并将数据导出到Excel文件")
    @PostMapping("/exportPage")
    public void exportPage(@RequestBody QueryPageParam query, HttpServletRequest request, HttpServletResponse response) throws IOException {
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String contact = (String)param.get("contact");
        String status = (String)param.get("status");
        String startDate = (String)param.get("startDate");
        String endDate = (String)param.get("endDate");
        String keyword = (String)param.get("keyword");

        // 从请求中获取Cookie
        Cookie[] cookies = request.getCookies();
        String userId = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("userId")) {
                userId = cookie.getValue();
                break;
            }
        }

        List<ComplaintResidentDTO> dataList = new ArrayList<>();
        if (userId != null) {
            // 用户已登录，返回用户信息
            User user = userService.getById(Integer.valueOf(userId));
            Map<String, Object> params = new HashMap<>();
            params.put("communityId", user.getCommunityId());
            params.put("name", name);
            params.put("contact", contact);
            params.put("status", status);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("keyword", keyword);

            Page<ComplaintResidentDTO> page = new Page();
            page.setCurrent(query.getPageNum());
            page.setSize(query.getPageSize());

            IPage<ComplaintResidentDTO> result = complaintService.getComplaintsWithResidents(page, params);
            dataList = result.getRecords();
        }

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("complaints.xlsx", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        // 创建一个ExcelWriter
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), ComplaintResidentDTO.class).build();

        // 写入数据
        WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();
        excelWriter.write(dataList, writeSheet);

        // 关闭ExcelWriter
        excelWriter.finish();
    }

    @ApiOperation(value = "导出所有投诉信息到Excel文件", notes = "在验证用户登录状态后，查询所有满足条件的投诉信息，并将数据导出到Excel文件")
    @PostMapping("/exportAll")
    public void exportAll(@RequestBody QueryPageParam query, HttpServletRequest request, HttpServletResponse response) throws IOException {
        HashMap param = query.getParam();
        String name = (String)param.get("name");
        String contact = (String)param.get("contact");
        String status = (String)param.get("status");
        String startDate = (String)param.get("startDate");
        String endDate = (String)param.get("endDate");
        String keyword = (String)param.get("keyword");

        // 从请求中获取Cookie
        Cookie[] cookies = request.getCookies();
        String userId = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("userId")) {
                userId = cookie.getValue();
                break;
            }
        }

        List<ComplaintResidentDTO> dataList = new ArrayList<>();
        if (userId != null) {
            // 用户已登录，返回用户信息
            User user = userService.getById(Integer.valueOf(userId));
            Map<String, Object> params = new HashMap<>();
            params.put("communityId", user.getCommunityId());
            params.put("name", name);
            params.put("contact", contact);
            params.put("status", status);
            params.put("startDate", startDate);
            params.put("endDate", endDate);
            params.put("keyword", keyword);

            // 创建一个大的分页对象，以便获取所有的记录
            Page<ComplaintResidentDTO> page = new Page();
            page.setCurrent(1);
            page.setSize(Integer.MAX_VALUE);

            IPage<ComplaintResidentDTO> result = complaintService.getComplaintsWithResidents(page, params);
            dataList = result.getRecords();
        }

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("complaints.xlsx", "UTF-8");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

        // 创建一个ExcelWriter
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), ComplaintResidentDTO.class).build();

        // 写入数据
        WriteSheet writeSheet = EasyExcel.writerSheet("Sheet1").build();
        excelWriter.write(dataList, writeSheet);

        // 关闭ExcelWriter
        excelWriter.finish();
    }
}
