package com.khabalita.sistemacomercial.dto.response;

import java.util.List;

public record ProductImportResult(
        boolean valid,
        int totalRows,
        int acceptedRows,
        List<String> errors) {
}
