package com.student.portfolio.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.student.portfolio.model.CreatePortfolioRequest;
import com.student.portfolio.model.PortfolioResponse;
import com.student.portfolio.model.UpdatePortfolioRequest;
import com.student.portfolio.service.impl.PortfolioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/portfolios")
@Tag(name = "Portfolios", description = "API for managing user portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Operation(summary = "Create a new portfolio")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @ModelAttribute CreatePortfolioRequest request) throws IOException { // use @ModelAttribute for Multipart
        PortfolioResponse created = portfolioService.createPortfolio(request);
        return ResponseEntity.ok(created);
    }

    @Operation(summary = "Update existing portfolio")
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @PathVariable String id,
            @ModelAttribute UpdatePortfolioRequest request) throws IOException {
        PortfolioResponse updated = portfolioService.updatePortfolio(id, request);
        return ResponseEntity.ok(updated);
    }


    @Operation(summary = "Delete a portfolio")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable("id") String id) {
        portfolioService.deletePortfolio(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get all portfolios for the current user")
    @GetMapping("/my")
    public ResponseEntity<List<PortfolioResponse>> getUserPortfolios() {
        List<PortfolioResponse> portfolios = portfolioService.getUserPortfolios();
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "Get a specific portfolio by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable("id") String id) {
        PortfolioResponse portfolio = portfolioService.getPortfolio(id);
        return ResponseEntity.ok(portfolio);
    }

    @Operation(summary = "Get all public portfolios")
    @GetMapping("/public")
    public ResponseEntity<List<PortfolioResponse>> getPublicPortfolios() {
        List<PortfolioResponse> portfolios = portfolioService.getPublicPortfolios();
        return ResponseEntity.ok(portfolios);
    }

    @Operation(summary = "Toggle portfolio visibility (public/private)")
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<PortfolioResponse> togglePortfolioVisibility(@PathVariable("id") String id) {
        PortfolioResponse portfolio = portfolioService.togglePortfolioVisibility(id);
        return ResponseEntity.ok(portfolio);
    }
}