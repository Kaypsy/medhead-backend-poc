package com.medhead.bedallocation.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * DTO standard pour les réponses d'erreur HTTP.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Structure de réponse d'erreur standard")
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Horodatage de l'erreur", example = "2026-01-06T15:06:57")
    private LocalDateTime timestamp;

    @Schema(description = "Code de statut HTTP", example = "400")
    private int status;

    @Schema(description = "Libellé de l'erreur HTTP", example = "Bad Request")
    private String error;

    @Schema(description = "Message détaillé de l'erreur", example = "Le nom de l'hôpital est requis")
    private String message;

    @Schema(description = "Chemin de l'URI ayant provoqué l'erreur", example = "/api/hospitals")
    private String path;

    public static ErrorResponse of(HttpStatus status, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(path)
                .build();
    }
}
