package com.student.portfolio.service.impl;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.student.portfolio.entity.UserPortfolio;
import com.student.portfolio.exception.CustomException;
import com.student.portfolio.model.CreatePortfolioRequest;
import com.student.portfolio.model.PortfolioResponse;
import com.student.portfolio.model.UpdatePortfolioRequest;
import com.student.portfolio.repository.UserPortfolioRepository;

@Service
public class PortfolioService {

	private final UserPortfolioRepository portfolioRepository;
	private final ModelMapper modelMapper;

	public PortfolioService(UserPortfolioRepository portfolioRepository, ModelMapper modelMapper) {
		this.portfolioRepository = portfolioRepository;
		this.modelMapper = modelMapper;
	}

	public PortfolioResponse createPortfolio(CreatePortfolioRequest request) throws IOException {
		String userId = getCurrentUserId();

		// Map non-file fields
		UserPortfolio portfolio = modelMapper.map(request, UserPortfolio.class);
		portfolio.setUserId(userId);

		// Encode photo
		if (request.getUserPhoto() != null && !request.getUserPhoto().isEmpty()) {
			portfolio.setPhoto(Base64.getEncoder().encodeToString(request.getUserPhoto().getBytes()));
		}

		

		UserPortfolio saved = portfolioRepository.save(portfolio);

		return modelMapper.map(saved, PortfolioResponse.class);
	}

	public PortfolioResponse updatePortfolio(String id, UpdatePortfolioRequest request) throws IOException {
	    UserPortfolio existingPortfolio = verifyPortfolioOwnership(id);

	    // Map non-file fields
	    modelMapper.map(request, existingPortfolio);

	    // Update photo if new file provided
	    if (request.getUserPhoto() != null && !request.getUserPhoto().isEmpty()) {
	        existingPortfolio.setPhoto(Base64.getEncoder()
			        .encodeToString(request.getUserPhoto().getBytes()));
	    }

	    

	    UserPortfolio updated = portfolioRepository.save(existingPortfolio);

	    return modelMapper.map(updated, PortfolioResponse.class);
	}


	public void deletePortfolio(String id) {
		// Verify the portfolio exists and belongs to the current user
		verifyPortfolioOwnership(id);
		portfolioRepository.deleteById(id);
	}

	public List<PortfolioResponse> getUserPortfolios() {
		String userId = getCurrentUserId();
		List<UserPortfolio> portfolios = portfolioRepository.findByUserId(userId);

		return portfolios.stream().map(portfolio -> modelMapper.map(portfolio, PortfolioResponse.class))
				.collect(Collectors.toList());
	}

	public PortfolioResponse getPortfolio(String id) {
		Optional<UserPortfolio> portfolio = portfolioRepository.findById(id);

		if (portfolio.isEmpty()) {
			throw new CustomException("Portfolio not found");
		}

		// Check if the portfolio is public or belongs to the current user
		UserPortfolio p = portfolio.get();
		String currentUserId = getCurrentUserId();

		if (!p.isPublic() && !p.getUserId().equals(currentUserId)) {
			throw new CustomException("Access denied to private portfolio");
		}

		// Convert entity to response DTO
		return modelMapper.map(p, PortfolioResponse.class);
	}

	public List<PortfolioResponse> getPublicPortfolios() {
		List<UserPortfolio> portfolios = portfolioRepository.findByIsPublic(true);

		// Convert entities to response DTOs
		return portfolios.stream().map(portfolio -> modelMapper.map(portfolio, PortfolioResponse.class))
				.collect(Collectors.toList());
	}

	public PortfolioResponse togglePortfolioVisibility(String id) {
		UserPortfolio portfolio = verifyPortfolioOwnership(id);
		portfolio.setPublic(!portfolio.isPublic());

		UserPortfolio updatedPortfolio = portfolioRepository.save(portfolio);

		// Convert entity to response DTO
		return modelMapper.map(updatedPortfolio, PortfolioResponse.class);
	}

	private UserPortfolio verifyPortfolioOwnership(String id) {
		Optional<UserPortfolio> portfolio = portfolioRepository.findById(id);

		if (portfolio.isEmpty()) {
			throw new CustomException("Portfolio not found");
		}

		String currentUserId = getCurrentUserId();
		if (!portfolio.get().getUserId().equals(currentUserId)) {
			throw new CustomException("You don't have permission to modify this portfolio");
		}

		return portfolio.get();
	}

	private String getCurrentUserId() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

		if (principal instanceof UserDetails) {
			return ((UserDetails) principal).getUsername();
		} else {
			return principal.toString();
		}
	}

	

}