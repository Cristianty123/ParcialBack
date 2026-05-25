package com.labback.dto;

import com.labback.enums.ServiceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Body de PATCH /services/{id}/status (HU-11).
 * Solo acepta ACTIVE o INACTIVE (el enum lo valida en deserialización).
 */
@Data
public class ServiceStatusRequest {

    @NotNull(message = "El estado es obligatorio. Valores permitidos: ACTIVE, INACTIVE")
    private ServiceStatus status;
}