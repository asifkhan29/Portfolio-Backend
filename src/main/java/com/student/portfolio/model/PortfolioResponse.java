package com.student.portfolio.model;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioResponse {
    private String id;
    private String name;
    private String photo; 
    private String email;
    private String phoneNumber;
    private String address;
    private List<String> skills;
    private boolean isPublic;
    private String userId;


}
