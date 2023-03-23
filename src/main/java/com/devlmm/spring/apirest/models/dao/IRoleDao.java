package com.devlmm.spring.apirest.models.dao;

import com.devlmm.spring.apirest.models.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface IRoleDao extends JpaRepository<Role, Long> {
    @Query(value = "SELECT r FROM roles r WHERE r.name = ?1", nativeQuery = true)
    Optional<Role> findByName(String name);
    Optional<Role> findRoleById(Long id);

    List<Role> findAll();
}
