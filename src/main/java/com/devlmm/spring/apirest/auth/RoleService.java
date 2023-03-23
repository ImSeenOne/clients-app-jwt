package com.devlmm.spring.apirest.auth;

import com.devlmm.spring.apirest.models.dao.IRoleDao;
import com.devlmm.spring.apirest.models.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final IRoleDao roleDao;

    public Role getRoleByName(String name) {
        return roleDao.findByName(name).orElseThrow();
    }

    public Role getRoleById(Long id) {
        return roleDao.findRoleById(id).orElseThrow();
    }

    public List<Role> getAllRoles(){
        return roleDao.findAll();
    }

    public List<Role> getRolesByNames(String... names) {
        ArrayList<Role> roles = new ArrayList<>();
        Arrays.stream(names).forEach((name) -> roles.add(this.getRoleByName(name)));
        return roles;
    }

}
