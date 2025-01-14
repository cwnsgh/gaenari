package com.gaenari.backend.domain.inventory.controller;

import com.gaenari.backend.domain.inventory.dto.responseDto.MyEquipItems;
import com.gaenari.backend.domain.inventory.dto.responseDto.MyInventoryItems;
import com.gaenari.backend.domain.inventory.dto.responseDto.MyInventoryPets;
import com.gaenari.backend.domain.inventory.dto.responseDto.MySetsCnt;
import com.gaenari.backend.domain.inventory.service.InventoryService;
import com.gaenari.backend.domain.item.dto.responseDto.Items;
import com.gaenari.backend.domain.item.entity.Category;
import com.gaenari.backend.global.format.code.ErrorCode;
import com.gaenari.backend.global.format.code.ResponseCode;
import com.gaenari.backend.global.format.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {
    private final ApiResponse response;
    private final InventoryService inventoryService;

    @Operation(summary = "[Feign] 기본 아이템 적용", description = "회원가입시 자동 생성")
    @PostMapping("/items/{accountId}")
    public ResponseEntity<?> createNormalItems(@PathVariable String accountId){
        inventoryService.createNormalItems(accountId);
        return response.success(ResponseCode.EQUIP_ITEMS_SUCCESS);
    }

    @Operation(summary = "[Feign] 아이템 삭제", description = "회원탈퇴시 아이템 삭제")
    @DeleteMapping("/items/{accountId}")
    public ResponseEntity<?> deleteItems(@PathVariable String accountId){
        inventoryService.deleteItems(accountId);
        return response.success(ResponseCode.DELETE_MEMBER_ITEMS_SUCCESS);
    }

    @Operation(summary = "나의 보관함 보유 아이템 개수", description = "보관함 첫 화면 구성")
    @GetMapping("/items")
    public ResponseEntity<?> getSets(@Parameter(hidden = true) @RequestHeader("User-Info") String accountId){
        // memberId가 null이면 인증 실패
        if (accountId == null) {
            return response.error(ErrorCode.EMPTY_MEMBER.getMessage());
        }
        List<MySetsCnt> mySetsCntList = inventoryService.getSets(accountId);
        return response.success(ResponseCode.GET_MY_ITEMS_SUCCESS, mySetsCntList);
    }

    @Operation(summary = "나의 보관함 조회(아이템)", description = "보관함내의 아이템들 조회")
    @GetMapping("/items/{setId}")
    public ResponseEntity<?> getMyItems(@Parameter(hidden = true) @RequestHeader("User-Info") String accountId, @PathVariable int setId){
        // memberId가 null이면 인증 실패
        if (accountId == null) {
            return response.error(ErrorCode.EMPTY_MEMBER.getMessage());
        }
        MyInventoryItems myInventoryItemsList = inventoryService.getMyItems(accountId, setId);
        return response.success(ResponseCode.GET_MY_ITEMS_SUCCESS, myInventoryItemsList);
    }

    @Operation(summary = "나의 보관함 조회(펫)", description = "보관함내의 펫들 조회")
    @GetMapping("/pets")
    public ResponseEntity<?> getMyPets(@Parameter(hidden = true) @RequestHeader("User-Info") String accountId){
        // memberId가 null이면 인증 실패
        if (accountId == null) {
            return response.error(ErrorCode.EMPTY_MEMBER.getMessage());
        }
        List<MyInventoryPets> myInventoryItemsList = inventoryService.getMyPets(accountId);
        return response.success(ResponseCode.GET_MY_PETS_SUCCESS, myInventoryItemsList);
    }

    @Operation(summary = "카테고리 적용 아이템 조회", description = "카테고리 적용 아이템 조회")
    @GetMapping("/equip/{category}")
    public ResponseEntity<?> getEquipCategory(@Parameter(hidden = true) @RequestHeader("User-Info") String accountId,
                                              @PathVariable Category category){
        // memberId가 null이면 인증 실패
        if (accountId == null) {
            return response.error(ErrorCode.EMPTY_MEMBER.getMessage());
        }
        Long myEquipCategoryItemId = inventoryService.getEquipCategory(accountId, category);
        return response.success(ResponseCode.GET_EQUIP_ITEMS_SUCCESS, myEquipCategoryItemId);
    }

    @Operation(summary = "적용 아이템 조회", description = "적용 아이템 조회")
    @GetMapping("/equip")
    public ResponseEntity<?> getEquipItems(@Parameter(hidden = true) @RequestHeader("User-Info") String accountId){
        // memberId가 null이면 인증 실패
        if (accountId == null) {
            return response.error(ErrorCode.EMPTY_MEMBER.getMessage());
        }
        MyEquipItems myEquipItemsList = inventoryService.getEquipItems(accountId);
        return response.success(ResponseCode.GET_EQUIP_ITEMS_SUCCESS, myEquipItemsList);
    }

    @Operation(summary = "친구집 방문", description = "친구 집 적용 아이템 조회")
    @GetMapping("/mate/{memberId}")
    public ResponseEntity<?> getMateItems(@Parameter(hidden = true) @RequestHeader("User-Info") String accountId, @PathVariable Long memberId){
        // memberId가 null이면 인증 실패
        if (accountId == null) {
            return response.error(ErrorCode.EMPTY_MEMBER.getMessage());
        }
        String mateEmail = inventoryService.getMateAccountId(memberId);
        MyEquipItems myEquipItemsList = inventoryService.getEquipItems(mateEmail);
        return response.success(ResponseCode.VISIT_FRIEND_HOME_SUCCESS, myEquipItemsList);
    }

    @Operation(summary = "아이템 적용", description = "아이템 적용하기")
    @PutMapping("/equip/{category}/{itemId}")
    public ResponseEntity<?> updateItems(@Parameter(hidden = true) @RequestHeader("User-Info") String accountId,
                                         @PathVariable Category category,
                                         @PathVariable Long itemId){
        // memberId가 null이면 인증 실패
        if (accountId == null) {
            return response.error(ErrorCode.EMPTY_MEMBER.getMessage());
        }
        inventoryService.updateItems(accountId, category, itemId);
        return response.success(ResponseCode.UPDATE_EQUIP_ITEMS_SUCCESS);
    }

    @Operation(summary = "아이템 랜덤 구매", description = "아이템 랜덤 구매")
    @GetMapping("/purchase")
    public ResponseEntity<?> randomItem(@Parameter(hidden = true) @RequestHeader("User-Info") String accountId){
        // memberId가 null이면 인증 실패
        if (accountId == null) {
            return response.error(ErrorCode.EMPTY_MEMBER.getMessage());
        }
        Items randomItem = inventoryService.randomItem(accountId);
        return response.success(ResponseCode.PURCHASE_ITEMS_SUCCESS, randomItem);
    }



}
