package com.quincus.db.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
@Entity
@Table(name = "organization")
public class OrganizationEntity extends BaseEntity {

    @Id
    @EqualsAndHashCode.Include
    @ToString.Include
    @NotNull
    @Size(min = 1, max = 36)
    private String id;

}
