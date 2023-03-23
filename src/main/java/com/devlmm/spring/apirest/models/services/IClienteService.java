package com.devlmm.spring.apirest.models.services;

import com.devlmm.spring.apirest.models.entity.Cliente;
import com.devlmm.spring.apirest.models.entity.Region;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IClienteService {
    public List<Cliente> findAll();
    public Page<Cliente> findAll(Pageable pageable);
    public Cliente findById(Long id);
    public Cliente save(Cliente cliente);
    public void  delete(Long id);
    public List <Region> findAllRegiones();
}
