package com.demo.project90.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Audit implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;
    @Size(max = 45)
    private String username;
    @Column(unique=true)
    private String token;
    private String message;
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd hh:mm")
    private LocalDateTime logDate;
    private Long itemId;
    @Version
    private int version;

}
