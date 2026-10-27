package com.khabalita.sistemacomercial.Entities;

import com.khabalita.sistemacomercial.Entities.Base.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessSettings extends AuditableEntity {

    @Column(name = "logo_storage_key", length = 255)
    private String logoStorageKey;

    @Column(name = "logo_content_type", length = 50)
    private String logoContentType;
}
