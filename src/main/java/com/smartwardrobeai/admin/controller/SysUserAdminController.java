package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.SysUserQueryDTO;
import com.smartwardrobeai.admin.model.dto.SysUserSaveDTO;
import com.smartwardrobeai.admin.model.vo.SysUserVO;
import com.smartwardrobeai.admin.service.SysUserService;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.model.entity.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sys-user")
@Tag(name = "【后台】管理端用户管理")
@RequiredArgsConstructor
public class SysUserAdminController {

    private final SysUserService sysUserService;

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public Result<PageResult<SysUserVO>> page(SysUserQueryDTO queryDTO) {
        return Result.success(sysUserService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public Result<SysUserVO> getDetail(@PathVariable Long id) {
        return Result.success(sysUserService.getDetail(id));
    }

    @PostMapping("/add")
    @Operation(summary = "新增用户")
    public Result<Void> add(@RequestBody @Valid SysUserSaveDTO saveDTO) {
        sysUserService.saveSysUser(saveDTO);
        return Result.success(null);
    }

    @PutMapping("/update")
    @Operation(summary = "修改用户")
    public Result<Void> update(@RequestBody @Valid SysUserSaveDTO saveDTO) {
        sysUserService.updateSysUser(saveDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.removeSysUserById(id);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        sysUserService.removeSysUserBatchByIds(ids);
        return Result.success(null);
    }

    @PatchMapping("/status/{id}/{status}")
    @Operation(summary = "修改状态", description = "1启用 0禁用")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        sysUserService.updateStatus(id, status);
        return Result.success(null);
    }

    @PostMapping("/reset-password/{id}")
    @Operation(summary = "重置密码", description = "重置为默认密码：123456")
    public Result<String> resetPassword(@PathVariable Long id) {
        String message = sysUserService.resetPassword(id);
        return Result.success(message, message);
    }
}

