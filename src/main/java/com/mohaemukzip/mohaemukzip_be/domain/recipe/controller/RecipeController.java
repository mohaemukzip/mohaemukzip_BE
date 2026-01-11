package com.mohaemukzip.mohaemukzip_be.domain.recipe.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recipes")
@Tag(name = "Recipe")
@Validated
public class RecipeController {
}
