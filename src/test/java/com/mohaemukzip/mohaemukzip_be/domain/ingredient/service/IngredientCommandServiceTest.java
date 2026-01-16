package com.mohaemukzip.mohaemukzip_be.domain.ingredient.service;

import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientRequestDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.dto.IngredientResponseDTO;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.Ingredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.MemberIngredient;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.entity.enums.StorageType;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.IngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.ingredient.repository.MemberIngredientRepository;
import com.mohaemukzip.mohaemukzip_be.domain.member.entity.Member;
import com.mohaemukzip.mohaemukzip_be.domain.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IngredientCommandServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private IngredientRepository ingredientRepository;

    @Mock
    private MemberIngredientRepository memberIngredientRepository;

    @InjectMocks
    private IngredientCommandServiceImpl ingredientCommandService;

    @Test
    @DisplayName("냉장고 재료 저장 테스트 ")
    void addFridgeIngredient_Success() {

        // Given
        Long memberId = 1L;
        Long ingredientId = 10L;

        IngredientRequestDTO.AddFridge request = IngredientRequestDTO.AddFridge.builder()
                .ingredientId(ingredientId)
                .storageType(StorageType.FREEZER)
                .expireDate(LocalDate.now())
                .weight(100.0)
                .build();

        Member mockMember = Member.builder().id(memberId).build();
        Ingredient mockIngredient = Ingredient.builder().id(ingredientId).build();
        MemberIngredient mockSaved = MemberIngredient.builder()
                .id(500L)
                .storageType(request.getStorageType())
                .expireDate(request.getExpireDate())
                .weight(request.getWeight())
                .build();

        // Mocking 설정
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(mockMember));
        when(ingredientRepository.findById(ingredientId)).thenReturn(Optional.of(mockIngredient));
        when(memberIngredientRepository.save(any(MemberIngredient.class))).thenReturn(mockSaved);

        // When
        IngredientResponseDTO.AddFridgeResult result = ingredientCommandService.addFridgeIngredient(memberId, request);

        // Then
        assertNotNull(result);
        assertEquals(500L, result.getMemberIngredientId());

        verify(memberIngredientRepository, times(1)).save(any(MemberIngredient.class));
        assertEquals(StorageType.FREEZER, mockSaved.getStorageType());
        assertEquals(100.0, mockSaved.getWeight());

    }
}
