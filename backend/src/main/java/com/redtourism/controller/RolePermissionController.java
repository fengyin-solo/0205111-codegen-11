package com.redtourism.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redtourism.common.Constants;
import com.redtourism.common.Result;
import com.redtourism.entity.SysRole;
import com.redtourism.entity.SysRoleMenu;
import com.redtourism.entity.User;
import com.redtourism.mapper.SysRoleMapper;
import com.redtourism.mapper.SysRoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/admin/role")
public class RolePermissionController {

    @Autowired private SysRoleMapper roleMapper;
    @Autowired private SysRoleMenuMapper menuMapper;

    @GetMapping("/list")
    public Result<List<SysRole>> listRoles() {
        return Result.success(roleMapper.selectList(null));
    }

    @GetMapping("/save")
    public Result<String> saveRole(@RequestParam(required = false) Long id,
                                    @RequestParam String code,
                                    @RequestParam String name,
                                    @RequestParam(required = false) String description) {
        SysRole role = id != null ? roleMapper.selectById(id) : new SysRole();
        if (role == null) role = new SysRole();
        role.setCode(code);
        role.setName(name);
        role.setDescription(description);
        if (id != null) { role.setId(id); roleMapper.updateById(role); }
        else roleMapper.insert(role);
        return Result.success("保存成功", null);
    }

    @GetMapping("/delete")
    public Result<String> deleteRole(@RequestParam Long id) {
        SysRole role = roleMapper.selectById(id);
        if (role != null) {
            menuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleCode, role.getCode()));
            roleMapper.deleteById(id);
        }
        return Result.success("删除成功", null);
    }

    @GetMapping("/menu/list")
    public Result<List<SysRoleMenu>> listMenus(@RequestParam String roleCode, HttpSession session) {
        User current = (User) session.getAttribute(Constants.SESSION_USER);
        if (current == null) return Result.error(401, "请先登录");
        if (!Constants.ROLE_ADMIN.equals(current.getRole()) && !roleCode.equals(current.getRole())) {
            return Result.error("无权查看其他角色权限");
        }
        return Result.success(menuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleCode, roleCode)));
    }

    @GetMapping("/menu/save")
    public Result<String> saveMenu(@RequestParam(required = false) Long id,
                                    @RequestParam String roleCode,
                                    @RequestParam String menuKey,
                                    @RequestParam(required = false) String menuName,
                                    @RequestParam(required = false, defaultValue = "1") Integer enabled) {
        SysRoleMenu m = id != null ? menuMapper.selectById(id) : new SysRoleMenu();
        if (m == null) m = new SysRoleMenu();
        m.setRoleCode(roleCode);
        m.setMenuKey(menuKey);
        m.setMenuName(menuName);
        m.setEnabled(enabled);
        if (id != null) menuMapper.updateById(m);
        else menuMapper.insert(m);
        return Result.success("保存成功", null);
    }

    @GetMapping("/menu/toggle")
    public Result<String> toggleMenu(@RequestParam Long id) {
        SysRoleMenu m = menuMapper.selectById(id);
        if (m != null) {
            m.setEnabled(m.getEnabled() == 1 ? 0 : 1);
            menuMapper.updateById(m);
        }
        return Result.success("已切换", null);
    }

    @GetMapping("/menu/delete")
    public Result<String> deleteMenu(@RequestParam Long id) {
        menuMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}
