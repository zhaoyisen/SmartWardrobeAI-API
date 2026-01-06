package com.smartwardrobeai.admin.controller;

import com.smartwardrobeai.admin.model.dto.AppUserQueryDTO;
import com.smartwardrobeai.admin.model.dto.AppUserSaveDTO;
import com.smartwardrobeai.admin.model.vo.AppUserVO;
import com.smartwardrobeai.admin.service.AppUserService;
import com.smartwardrobeai.common.Result;
import com.smartwardrobeai.common.model.entity.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/app-user")
@Tag(name = "【后台】App端用户管理")
@RequiredArgsConstructor
public class AppUserAdminController {

    private final AppUserService appUserService;

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    public Result<PageResult<AppUserVO>> page(AppUserQueryDTO queryDTO) {
        return Result.success(appUserService.pageQuery(queryDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取详情")
    public Result<AppUserVO> getDetail(@PathVariable Long id) {
        return Result.success(appUserService.getDetail(id));
    }

    @PutMapping("/update")
    @Operation(summary = "修改用户", description = "不包含密码字段，密码由用户自行修改")
    public Result<Void> update(@RequestBody @Valid AppUserSaveDTO saveDTO) {
        appUserService.updateAppUser(saveDTO);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户", description = "删除前会检查是否有关联的衣物数据")
    public Result<Void> delete(@PathVariable Long id) {
        appUserService.removeAppUserById(id);
        return Result.success(null);
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除", description = "删除前会检查是否有关联的衣物数据")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        appUserService.removeAppUserBatchByIds(ids);
        return Result.success(null);
    }

    @PatchMapping("/status/{id}/{status}")
    @Operation(summary = "修改状态", description = "1启用 0禁用")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        appUserService.updateStatus(id, status);
        return Result.success(null);
    }
}

