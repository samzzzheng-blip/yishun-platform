package com.kiss.yishun.controller.admin;

import com.kiss.yishun.auth.JwtUtil;
import com.kiss.yishun.common.Result;
import com.kiss.yishun.common.ResultGenerator;
import com.kiss.yishun.constant.SecurityConstant;
import com.kiss.yishun.entity.Menu;
import com.kiss.yishun.entity.User;
import com.kiss.yishun.entity.vo.MenuVo;
import com.kiss.yishun.service.MenuService;
import com.kiss.yishun.service.OperLogService;
import com.kiss.yishun.service.UserService;
import com.kiss.yishun.utils.IpUtils;
import com.kiss.yishun.utils.StrUtils;
import com.kiss.yishun.utils.TreeUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api("菜单管理模块")
@RequestMapping("api/usr")
public class MenuController {

	@Autowired
	MenuService menuService;
	@Autowired
	UserService userService;
	@Autowired
	OperLogService operLogService;

	@ApiOperation("查询角色操作权限的菜单列表")
	@RequestMapping(value = "/queryRoleMenuList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryRoleMenuList(@ApiParam(value = "操作id") @RequestParam(value = "operationId", required = false) String operationId) {
		return this.queryRoleMenuList(operationId == null?1: Integer.parseInt(operationId));
	}

	public Result queryRoleMenuList(int operationId) {
		String token = (String) SecurityUtils.getSubject().getPrincipal();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		User user = userService.findByUsername(userName);
		if (null == user) {
			return ResultGenerator.genFailureResult("查询不到用户");
		}
		if (null == user.getRole()) {
			return ResultGenerator.genFailureResult("查询不到角色信息");
		}

		List<Menu> list = menuService.findRoleMenuList(user.getRole().getId(),operationId);
		// 遍历查询所有子菜单
		List<Menu> result = new ArrayList<>();
		for (Menu menu: list) {
			List<Menu> nodeList = new ArrayList<>();
			// 获取子节点菜单
			getChildMenu(nodeList,menu.getId());
			// 根目录不包含
			if (menu.getParentId() == 0) {
				result.addAll(nodeList);
			} else {
				menu.setChildren(nodeList);
				// 追溯到一级目录
				Menu parentMenu = getParentMenu(menu,menu.getParentId());
				result.add(parentMenu);
			}
		}
		// 合并同一节点下的子菜单路径
		Map<Long,Menu> menuMap = new HashMap<>();
		for (Menu menu: result) {
			if (menuMap.containsKey(menu.getId())) {
				List<Menu> children = menuMap.get(menu.getId()).getChildren();
				children.addAll(menu.getChildren());
				Menu next = menuMap.get(menu.getId());
				next.setChildren(children);
				menuMap.put(menu.getId(),next);
			} else {
				menuMap.put(menu.getId(), menu);
			}
		}
		// 输出
		result = new ArrayList<>();
		for (Map.Entry<Long,Menu> entry: menuMap.entrySet()) {
			result.add(entry.getValue());
		}
		List<MenuVo> voList = new ArrayList<>();
		getChildMenuVo(voList, result);
		Map<String,List<MenuVo>> map = new HashMap<>();
		map.put("list",voList);
		return ResultGenerator.genSuccessResult(map);
	}

	/**
	 * 获取一级目录到节点菜单
	 * @param menu
	 * @param pid
	 * @return
	 */
	private Menu getParentMenu(Menu menu, long pid) {
		Menu parentMenu = menuService.findMenuByParentId(pid);
		// 去除根目录
		if (parentMenu == null || parentMenu.getParentId() == 0) {
			return menu;
		}
		List<Menu> childs = new ArrayList<>();
		childs.add(menu);
		parentMenu.setChildren(childs);
		if (parentMenu.getParentId() > 0) {
			return getParentMenu(parentMenu, parentMenu.getParentId());
		}
		return parentMenu;
	}

	private List<MenuVo> getChildMenuVo(List<MenuVo> voList, List<Menu> menuList) {
		for (Menu menu: menuList) {
			MenuVo vo = new MenuVo();
			vo.setId(menu.getId());
			vo.setResIcon("");
			vo.setResKey(menu.getPath());
			vo.setResName(menu.getName());
			vo.setChildren(getChildMenuVo(new ArrayList<>(),menu.getChildren()));
			voList.add(vo);
		}
		return voList;
	}


	/**
	 * 获取节点下子菜单
	 * @param list
	 * @param cid
	 * @return
	 */
	private List<Menu> getChildMenu(List<Menu> list, long cid) {
		List<Menu> childMenuList = menuService.findMenuChildList(cid);
		if (childMenuList != null && childMenuList.size()>0) {
			for (Menu menu: childMenuList) {
				List<Menu> childNodeList = new ArrayList<>();
				menu.setChildren(getChildMenu(childNodeList, menu.getId()));
				list.add(menu);
			}
		}
		return list;
	}

	@ApiOperation("查询菜单列表，若是编辑查询已知节点外的列表")
	@RequestMapping(value = "/queryMenuList", method = {RequestMethod.GET})
	@ResponseBody
	public Result queryMenuList(@ApiParam(value = "节点编号") @RequestParam(value = "cId", required = false) String cId) {
		String token = (String) SecurityUtils.getSubject().getPrincipal();
		String userName = JwtUtil.getAccount(token, SecurityConstant.ACCOUNT);
		User user = userService.findByUsername(userName);
		if (null == user) {
			return ResultGenerator.genFailureResult("查询不到用户");
		}
		if (null == user.getRole()) {
			return ResultGenerator.genFailureResult("查询不到角色信息");
		}

		List<Menu> list = menuService.findMenuList(cId);
		Menu menu = null;
		if (null != list && list.size() > 0) {
			menu = list.get(0);
			menu.setChildren(TreeUtils.getMenuChild(list.get(0).getId(),list));
		}

		List<Menu> result = new ArrayList<>();
		result.add(menu);
		return ResultGenerator.genSuccessResult(result);
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("新增菜单")
	@RequestMapping(value = "/addMenu", method = {RequestMethod.POST})
	@ResponseBody
	public Result addMenu(@ApiParam("menu") @RequestBody Menu menu) throws Exception {
		if (StrUtils.isEmpty(menu.getName())) {
			return ResultGenerator.genFailureResult("菜单名称不能为空");
		}
		if (StrUtils.isEmpty(menu.getPath())) {
			return ResultGenerator.genFailureResult("菜单路径不能为空");
		}
		if (null == menu.getParentId()) {
			return ResultGenerator.genFailureResult("上级目录不能为空");
		}
//		if (null == menu.getPermissionList() || menu.getPermissionList().size() == 0) {
//			return ResultGenerator.genFailureResult("权限不能为空");
//		}
		Menu sMenu = menuService.findMenuByName(menu.getName());
		if (sMenu == null) {
			menuService.addMenu(menu);
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("已有相同菜单名称");
		}
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("更新菜单")
	@RequestMapping(value = "/updateMenu", method = {RequestMethod.POST})
	@ResponseBody
	public Result updateMenu(@ApiParam("menu") @RequestBody Menu menu) {
		if (StrUtils.isEmpty(menu.getName())) {
			return ResultGenerator.genFailureResult("菜单名称不能为空");
		}
		if (StrUtils.isEmpty(menu.getPath())) {
			return ResultGenerator.genFailureResult("菜单路径不能为空");
		}
		if (null == menu.getParentId()) {
			return ResultGenerator.genFailureResult("上级目录不能为空");
		}
//		if (null == menu.getPermissionList() || menu.getPermissionList().size() == 0) {
//			return ResultGenerator.genFailureResult("权限不能为空");
//		}
		long id = menu.getId();
		Menu sMenu = menuService.findMenuById(id);
		if (sMenu!=null) {
			sMenu.setName(menu.getName());
			sMenu.setParentId(menu.getParentId());
			sMenu.setPath(menu.getPath());
//			sMenu.setPermissionList(menu.getPermissionList());
			menuService.updateMenu(sMenu);
			return ResultGenerator.genSuccessResult();
		} else {
			return ResultGenerator.genFailureResult("找不到该菜单");
		}
	}

	@RequiresRoles(value = {"admin","superadmin"}, logical = Logical.OR)
	@ApiOperation("删除菜单")
	@RequestMapping(value = "/deleteMenu", method = {RequestMethod.GET})
	@ResponseBody
	public Result deleteMenu(@ApiParam(value = "编号",required = true,example = "1") @RequestParam("id") String id, HttpServletRequest request) {
		Menu menu = menuService.findMenuById(Long.parseLong(id));
		if (menu!=null) {
			menuService.deleteMenu(Long.parseLong(id));
		}
		operLogService.addOperLog("deleteMenu:"+id, IpUtils.getIp(request));
		return ResultGenerator.genSuccessResult();
	}
	
}
