package com.example.demo.pojo;


import lombok.*;

@Setter
@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    private  int id;
    private  String username;
    private  String password;

}
