package com.eduminds.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String type = "Bearer";

    // Infos utilisateur renvoyées au frontend
    private Long   id;
    private String name;
    private String email;
    private String filiere;
    private String level;
    private String niveauCalibre;
    private Integer xpTotal;
    private Integer streak;
    private Boolean diagnosticPassed;
    private String avatarUrl;
}
