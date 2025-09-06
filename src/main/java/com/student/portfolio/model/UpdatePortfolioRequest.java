package com.student.portfolio.model;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdatePortfolioRequest {
    private String name;
    private MultipartFile userPhoto;
    private String email;
    private String phoneNumber;
    private String address;
    private List<String> skills;
    private boolean isPublic;


}
