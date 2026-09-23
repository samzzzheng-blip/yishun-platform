package com.kiss.yishun.controller.admin;

import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.entity.enums.OperationEnum;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.*;
import com.kiss.yishun.entity.vo.RoleVo;
import com.kiss.yishun.service.*;
import com.kiss.yishun.utils.IpUtils;
import com.kiss.yishun.utils.StrUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api("角色管理模块")
@RequestMapping("api/usr")
public class RoleController {

	@Resource
	RoleService roleService;
	@Resource
	OperationService operationService;
	@Resource
	PermissionService permissionService;
	@Resource
	MenuService menuService;
	@Resource
	UserService userService;
	@Resource
	OperLogService operLogService;

	@ApiOperation("查询角色列表")
	@RequestMapping(value = "/queryRoleList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryRoleList() {
		String token = (String) SecurityUtils.getSubject().getPrincipal();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		User user = userService.findByUsername(userName);
		if (null == user) {
			return ResultGenerator.genFailureResult("查询不到用户");
		}
		if (null == user.getRole()) {
			return ResultGenerator.genFailureResult("查询不到角色信息");
		}
		List<Role> roleList = roleService.findRoleList(user.getRole().getLevel());
		return ResultGenerator.genSuccessResult(roleList);
	}

	@ApiOperation("角色查询权限列表")
	@RequestMapping(value = "/queryPermissionByRoleId", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryPermissionByRoleId(@ApiParam(value = "roleId", required = true) @RequestParam("roleId") String roleId) {
		Role role = roleService.findRoleById(Long.parseLong(roleId));
		if (null == role) {
			return ResultGenerator.genFailureResult("角色不存在");
		}
		return ResultGenerator.genSuccessResult(role.getPermissionList());
	}

	/**
	 * 解析获取permissionList
	 * @param operationCode
	 * @param menus
	 * @return
	 */
	private List<Permission> getRolePermissionList(String operationCode,String menus) {
		if (!StrUtils.isEmpty(menus)) {
			Operation operation =operationService.findOperationByCode(operationCode);
			if (null == operation) {
				return null;
			}
			String[] menuArr = menus.split(",");
			if (menuService.existsMenus(StrUtils.tranStringArray2LongArray(menuArr))) {
				List<Permission> permissionList = new ArrayList<>();
				for (String menuId: menuArr) {
					Menu menu = menuService.findMenuById(Long.parseLong(menuId));
					Permission permission = new Permission();
					permission.setOperation(operation);
					permission.setMenu(menu);
					permission.setName(operation.getName()+menu.getName());
					permission.setCode(menu.getPath()+":"+operation.getCode());
					Permission query = permissionService.findPermissionByCode(permission.getCode());
					if (null == query) {
						permissionService.addPermission(permission);
						Permission result = permissionService.findPermissionByCode(permission.getCode());
						permissionList.add(result);
					} else {
						permissionList.add(query);
					}
				}
				return permissionList;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("添加角色")
	@RequestMapping(value = "/addRole", method = {RequestMethod.POST})
	@ResponseBody
	public Result addRole(@ApiParam("roleAddVo") @RequestBody RoleVo roleAddVo) {
		if (StrUtils.isEmpty(roleAddVo.getCode())) {
			return ResultGenerator.genFailureResult("角色编号不能为空");
		}
		if (StrUtils.isEmpty(roleAddVo.getName())) {
			return ResultGenerator.genFailureResult("角色名称不能为空");
		}
		// level校验
		String token = (String) SecurityUtils.getSubject().getPrincipal();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		User user = userService.findByUsername(userName);
		if (null == user) {
			return ResultGenerator.genFailureResult("查询不到用户");
		}
		if (null == user.getRole()) {
			return ResultGenerator.genFailureResult("查询不到角色信息");
		}
		if (user.getRole().getLevel() > roleAddVo.getLevel()) {
			return ResultGenerator.genFailureResult("角色等级设置错误");
		}
		Role role = roleService.findRoleByCode(roleAddVo.getCode());
		if (null == role) {
			role = new Role();
			role.setCode(roleAddVo.getCode());
			role.setName(roleAddVo.getName());
			role.setLevel(roleAddVo.getLevel());
			List<Permission> permissionList = new ArrayList<>();
			if (!StrUtils.isEmpty(roleAddVo.getViewPermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.View.getOperation(),roleAddVo.getViewPermission()));
			}
			if (!StrUtils.isEmpty(roleAddVo.getAddPermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.Add.getOperation(),roleAddVo.getAddPermission()));
			}
			if (!StrUtils.isEmpty(roleAddVo.getUpdatePermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.Update.getOperation(),roleAddVo.getUpdatePermission()));
			}
			if (!StrUtils.isEmpty(roleAddVo.getDeletePermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.Delete.getOperation(),roleAddVo.getDeletePermission()));
			}
			if (!StrUtils.isEmpty(roleAddVo.getExportPermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.Export.getOperation(),roleAddVo.getExportPermission()));
			}
			role.setPermissionList(permissionList);
			roleService.addRole(role);
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("角色已存在");
		}
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("更新角色")
	@RequestMapping(value = "/updateRole", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateRole(@ApiParam("role") @RequestBody RoleVo roleVo) {
		if (StrUtils.isEmpty(roleVo.getCode())) {
			return ResultGenerator.genFailureResult("角色编号不能为空");
		}
		// level校验
		String token = (String) SecurityUtils.getSubject().getPrincipal();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		User user = userService.findByUsername(userName);
		if (null == user) {
			return ResultGenerator.genFailureResult("查询不到用户");
		}
		if (null == user.getRole()) {
			return ResultGenerator.genFailureResult("查询不到角色信息");
		}
		if (user.getRole().getLevel() > roleVo.getLevel()) {
			return ResultGenerator.genFailureResult("角色等级设置错误");
		}
		Role role = roleService.findRoleByCode(roleVo.getCode());
		if (null != role) {
			role.setCode(roleVo.getCode());
			role.setName(roleVo.getName());
			role.setLevel(roleVo.getLevel());
			List<Permission> permissionList = new ArrayList<>();
			if (!StrUtils.isEmpty(roleVo.getViewPermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.View.getOperation(),roleVo.getViewPermission()));
			}
			if (!StrUtils.isEmpty(roleVo.getAddPermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.Add.getOperation(),roleVo.getAddPermission()));
			}
			if (!StrUtils.isEmpty(roleVo.getUpdatePermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.Update.getOperation(),roleVo.getUpdatePermission()));
			}
			if (!StrUtils.isEmpty(roleVo.getDeletePermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.Delete.getOperation(),roleVo.getDeletePermission()));
			}
			if (!StrUtils.isEmpty(roleVo.getExportPermission())) {
				permissionList.addAll(getRolePermissionList(OperationEnum.Export.getOperation(),roleVo.getExportPermission()));
			}
			role.setPermissionList(permissionList);
			roleService.updateRole(role);
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("角色不存在");
		}
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("批量删除角色")
	@RequestMapping(value = "/deleteRole", method = {RequestMethod.GET})
	@ResponseBody
	public Result deleteRole(@ApiParam(value = "编号",required = true,example = "1,2,3") @RequestParam("id") String id, HttpServletRequest request) {
		String[] ids = id.split(",");
		for (int i=0;i<ids.length;i++) {
			Role role = roleService.findRoleById(Long.parseLong(ids[i]));
			if (role!=null) {
				roleService.deleteRole(Long.parseLong(ids[i]));
			}
		}
		operLogService.addOperLog("deleteRole:"+id, IpUtils.getIp(request));
		return ResultGenerator.genSuccessResult();
	}
	
}
