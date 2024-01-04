package com.pms.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.pms.entity.Resident;
import com.pms.entity.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ClassName: UserResidentDTO
 * Package: com.pms.dto
 * Description:
 * Author: JingYin233
 * Create: 2024/1/2 - 19:58
 */

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="User和Resident的DTO对象", description="")
public class UserResidentDTO {
    private Integer id;
    private String no;
    private String name;
    private String password;
    private Integer communityId;
    private Integer age;
    private Integer sex;
    private String phone;
    private Integer residentId;

    private String unitNumber;
    private String roomNumber;

    public UserResidentDTO(User user, Resident resident) {
        this.id = user.getId();
        this.no = user.getNo();
        this.name = user.getName();
        this.password = user.getPassword();
        this.communityId = user.getCommunityId();
        this.age = user.getAge();
        this.sex = user.getSex();
        this.phone = user.getPhone();
        this.residentId = user.getResidentId();
        this.unitNumber = resident.getUnitNumber();
        this.roomNumber = resident.getRoomNumber();

    }
}
