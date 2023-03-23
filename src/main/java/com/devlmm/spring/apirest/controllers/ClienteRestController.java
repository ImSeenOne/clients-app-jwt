package com.devlmm.spring.apirest.controllers;

import com.devlmm.spring.apirest.models.entity.Cliente;
import com.devlmm.spring.apirest.models.entity.Region;
import com.devlmm.spring.apirest.models.services.IClienteService;
import com.devlmm.spring.apirest.models.services.IUploadFileService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = {"http://localhost:4200"})
@RestController
@RequestMapping("/api")
@Slf4j
public class ClienteRestController {
    @Autowired
    private IClienteService clienteService;

    @Autowired
    private IUploadFileService uploadFileService;

    HashMap<String, String> messageMap;

    @GetMapping("/clientes")
    public List<Cliente> index(){
    return clienteService.findAll();
    }

    @GetMapping("/clientes/page/{page}")
    public Page<Cliente> index(@PathVariable Integer page){
        return clienteService.findAll(PageRequest.of(page, 5));
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<?> show(@PathVariable Long id){
        Cliente cliente;
        Map<String, Object> response = new HashMap<>();
        try {
            cliente = clienteService.findById(id);
        }
        catch (DataAccessException e){
            messageMap.put("mensaje", "Error al realizar la consulta en la base  de datos");
            messageMap.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return getResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, null, response, false, messageMap);
        }
        if (cliente == null){
            messageMap.put("mensaje", "El cliente ID:".concat(id.toString().concat(" no existe en la base de datos!")));
            return getResponseEntity(HttpStatus.NOT_FOUND, null, response, false, messageMap);
        }
        return new ResponseEntity<>(cliente, HttpStatus.OK);
    }

    @PostMapping("/clientes")
    public ResponseEntity<?> create(@Valid @RequestBody Cliente cliente, BindingResult result){
        Cliente clienteNew;
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()){
            List<String> errors = result.getFieldErrors()
                    .stream().map(err -> err.getField() + " " + err.getDefaultMessage()).collect(Collectors.toList());

            response.put("errors", errors);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        try{
            clienteNew = clienteService.save(cliente);
        }
        catch (DataAccessException e){
            response.put("mensaje", "Error al realizar el insert en la base  de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje", "El cliente ha sido creado con exito!");
        response.put("cliente", clienteNew);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/clientes/{id}")
    public ResponseEntity<?> update(@Valid @RequestBody Cliente cliente, BindingResult result, @PathVariable Long id){
        Cliente clienteActual = clienteService.findById(id);
        Cliente clienteActualizado;
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()){
            return getResponseEntity(HttpStatus.BAD_REQUEST, result, response, true);
        }

        if (clienteActual == null){
           messageMap.put("message", "El cliente ID:".concat(id.toString().concat(" no se puede editar porque existe en la base de datos!")));
            return getResponseEntity(HttpStatus.NOT_FOUND, result, response, false, messageMap);
        }

        try {
            clienteActual.updateTo(cliente);

            clienteActualizado = clienteService.save(clienteActual);
        } catch (DataAccessException e){
            messageMap.put("mensaje", "Error al acturalizar valores en la base  de datos");
            messageMap.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));

            return getResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, result, response, false, messageMap);
        }

        response.put("mensaje", "El cliente ha sido actualizado con exito!");
        response.put("cliente", clienteActualizado);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @SafeVarargs
    private ResponseEntity<Map<String, Object>> getResponseEntity(HttpStatus status, BindingResult result, Map<String, Object> response, boolean hasFieldErrors, Map<String, String>... messages) {

        if(hasFieldErrors) {
            List<String> errors = result.getFieldErrors()
                    .stream().map(err -> "El Campo: " + err.getField() + " " + err.getDefaultMessage()).collect(Collectors.toList());
            response.put("errors", errors);
        }

        try {
            response.put("message(s)", messages);
        } catch (NullPointerException npe) {
            log.error(npe.getMessage());
        }

        return new ResponseEntity<>(response, status);
    }

    @DeleteMapping("/clientes/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        Map<String, Object> response = new HashMap<>();

        try {
            Cliente clienteExiste = clienteService.findById(id);
            String nombreFotoAnt = clienteExiste.getFoto();
            uploadFileService.eliminar(nombreFotoAnt);
            clienteService.delete(id);
        }
        catch (DataAccessException e){
            response.put("mensaje", "Error al eliminar el cliente en la base  de datos");
            response.put("error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        response.put("mensaje", "El cliente se a eliminado de la base  de datos con exito!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/clientes/upload")
    public  ResponseEntity<?> upload(@RequestParam("archivo") MultipartFile archivo, @RequestParam("id") Long id){
        Map<String, Object> response = new HashMap<>();

        Cliente cliente = clienteService.findById(id);
        if (!archivo.isEmpty()){
            String nombreArchivo = null;
            try {
                nombreArchivo = uploadFileService.copiar(archivo);
            } catch (IOException e) {
                response.put("mensaje", "Error al subir la imagen");
                response.put("error", e.getMessage().concat(": ").concat(e.getCause().getMessage()));
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String nombreFotoAnt = cliente.getFoto();
            uploadFileService.eliminar(nombreFotoAnt);

            cliente.setFoto(nombreArchivo);
            clienteService.save(cliente);
            response.put("cliente", cliente);
            response.put("mensaje", "Foto subida correctamente" + nombreArchivo);
        }
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/uploads/img/{nombreFoto:.+}")
    public ResponseEntity<Resource> verFoto(@PathVariable String nombreFoto){
        Resource recurso = null;
        try {
           recurso = uploadFileService.cargar(nombreFoto);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders cabecera = new HttpHeaders();
        cabecera.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\" " + recurso.getFilename() + "\"");

        return new ResponseEntity<Resource>(recurso,cabecera, HttpStatus.OK);
    }

    @GetMapping("/clientes/regiones")
    public List<Region> listarRegiones(){
        return clienteService.findAllRegiones();
    }

}
