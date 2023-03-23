package com.devlmm.spring.apirest.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
/*, com.devlmm.spring.apirest.models.dao.IClienteDao
* , com.devlmm.spring.apirest.models.services.IClienteService */
@Entity
@Data
@Table(name = "clientes")
public class Cliente implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Size(min = 3)
    @Column(nullable = false)
    private String nombre;
    @NotEmpty
    @Column(nullable = false)
    private String apellidos;
    @NotEmpty
    @Email(message = "No es una direccion de correo valida")
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "created_at")
    @Temporal(TemporalType.DATE)
    private Date createdAt;

    @NotNull(message = "No puede quedar vacio el campo")
    @Column(name = "fecha_nacimiento")
    @Temporal(TemporalType.DATE)
    private Date nacimiento;

    @NotNull(message = "El campo no puede estar vacio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Region region;

    private String foto;

    @PrePersist
    public void prePersist(){
        createdAt = new Date();
    }

    public void updateTo(Cliente cliente) {
        this.setNombre(cliente.getNombre());
        this.setApellidos(cliente.getApellidos());
        this.setEmail(cliente.getEmail());
        this.setNacimiento(cliente.getNacimiento());
        this.setRegion(cliente.getRegion());
    }

}
