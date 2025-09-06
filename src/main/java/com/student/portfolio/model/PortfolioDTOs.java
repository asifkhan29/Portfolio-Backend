//package com.student.portfolio.model;
//
//import java.util.List;
//
//public record PortfolioDTOs() {
//    
//    public record CreatePortfolioRequest(
//        String name,
//        String photoUrl,
//        String email,
//        String phoneNumber,
//        String address,
//        List<String> skills,
//        List<ProjectRequest> projects,
//        boolean isPublic
//    ) {}
//    
//    public record UpdatePortfolioRequest(
//        String name,
//        String photoUrl,
//        String email,
//        String phoneNumber,
//        String address,
//        List<String> skills,
//        List<ProjectRequest> projects,
//        boolean isPublic
//    ) {}
//    
//    public record PortfolioResponse(
//        String id,
//        String name,
//        String photoUrl,
//        String email,
//        String phoneNumber,
//        String address,
//        List<String> skills,
//        List<ProjectResponse> projects,
//        boolean isPublic,
//        String userId
//    ) {}
//    
//    public record ProjectRequest(
//        String title,
//        String description,
//        String projectUrl,
//        String imageUrl
//    ) {}
//    
//    public record ProjectResponse(
//        String title,
//        String description,
//        String projectUrl,
//        String imageUrl
//    ) {}
//}