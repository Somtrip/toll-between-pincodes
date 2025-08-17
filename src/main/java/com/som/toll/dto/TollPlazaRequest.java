package com.som.toll.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TollPlazaRequest {

    @NotBlank(message = "sourcePincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "sourcePincode must be a valid 6-digit Indian pincode")
    private String sourcePincode;

    @NotBlank(message = "destinationPincode is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "destinationPincode must be a valid 6-digit Indian pincode")
    private String destinationPincode;
}

