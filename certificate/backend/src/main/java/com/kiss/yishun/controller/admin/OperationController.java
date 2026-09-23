package com.kiss.yishun.controller.admin;

import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.entity.Menu;
import com.kiss.yishun.entity.Operation;
import com.kiss.yishun.entity.Permission;
import com.kiss.yishun.service.MenuService;
import com.kiss.yishun.service.OperationService;
import com.kiss.yishun.service.PermissionService;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api("操作管理模块")
@RequestMapping("api/usr")
public class OperationController {

	@Autowired
	OperationService operationService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	MenuService menuService;

	@ApiOperation("查询操作列表")
	@RequestMapping(value = "/queryOperationList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryOperationList() {
		List<Operation> OperationList = operationService.findOperationList();

		return ResultGenerator.genSuccessResult(OperationList);
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("添加操作")
	@RequestMapping(value = "/addOperation", method = {RequestMethod.POST})
	@ResponseBody
	public Result addRole(@ApiParam("Operation") @RequestBody Operation pro) {
		if (StrUtils.isEmpty(pro.getCode())) {
			return ResultGenerator.genFailureResult("操作编号不能为空");
		}
		if (StrUtils.isEmpty(pro.getName())) {
			return ResultGenerator.genFailureResult("操作名称不能为空");
		}
//		if (StrUtils.isEmpty(pro.getParentId())) {
//			return ResultGenerator.genFailureResult("父类编号不能为空");
//		}
		Operation Operation = operationService.findOperationByCode(pro.getCode());
		if (null == Operation) {
			operationService.addOperation(pro);
			List<Menu> munulist = menuService.findMenuList(null);
			for (Menu menu : munulist) {
				// permission更新
				Permission query = permissionService.findPermissionByCode(menu.getPath()+":"+pro.getCode());
				boolean addFlag = false;
				if (query == null) {
					query = new Permission();
					addFlag = true;
				}
				query.setOperation(pro);
				query.setCode(menu.getPath()+":"+pro.getCode());
				query.setName(pro.getName()+menu.getName());
				query.setMenu(menu);
				if (addFlag) {
					permissionService.addPermission(query);
				} else {
					permissionService.updatePermission(query);
				}
			}

			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("操作已存在");
		}
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("更新操作")
	@RequestMapping(value = "/updateOperation", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateOperation(@ApiParam("Operation") @RequestBody Operation pro) {
		if (StrUtils.isEmpty(pro.getCode())) {
			return ResultGenerator.genFailureResult("操作编号不能为空");
		}
		if (StrUtils.isEmpty(pro.getName())) {
			return ResultGenerator.genFailureResult("操作名称不能为空");
		}
		Operation Operation = operationService.findOperationByCode(pro.getCode());
		if (null != Operation) {
			Operation.setName(pro.getName());
			operationService.updateOperation(Operation);
			List<Menu> munulist = menuService.findMenuList(null);
			for (Menu menu : munulist) {
				// permission更新
				Permission query = permissionService.findPermissionByCode(menu.getPath()+":"+pro.getCode());
				boolean addFlag = false;
				if (query == null) {
					query = new Permission();
					addFlag = true;
				}
				query.setOperation(pro);
				query.setCode(menu.getPath()+":"+pro.getCode());
				query.setName(pro.getName()+menu.getName());
				query.setMenu(menu);
				if (addFlag) {
					permissionService.addPermission(query);
				} else {
					permissionService.updatePermission(query);
				}
			}
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("操作不存在");
		}
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("批量删除操作")
	@RequestMapping(value = "/deleteOperation", method = {RequestMethod.GET})
	@ResponseBody
	public Result deleteOperation(@ApiParam(value = "编号",required = true,example = "1,2,3") @RequestParam("id") String id) {
		String[] ids = id.split(",");
		for (int i=0;i<ids.length;i++) {
			Operation Operation = operationService.findOperationById(Long.parseLong(ids[i]));
			if (Operation!=null) {
				operationService.deleteOperation(Long.parseLong(ids[i]));
			}
		}
		return ResultGenerator.genSuccessResult();
	}
	
}
