package com.projects.airBnBApp.entity;


import com.projects.airBnBApp.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "app_user")
//Table is provided app_user cause
//Postgres internally uses User Table
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( unique = true , nullable = false)
    private String email;

    //encoded
    @Column(nullable = false)
    private String password;

    private String name;

    //Using ORDINAL won't be able to know 1 point to USER or Managaer
    //Better to use String here
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles;

}
