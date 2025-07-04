package com.vitacheck.controller;

import com.vitacheck.dto.UserDto;
import com.vitacheck.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDto.InfoResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails){
        String email = userDetails.getUsername();
        UserDto.InfoResponse myInfo = userService.getMyInfo(email);
        return ResponseEntity.ok(myInfo);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto.InfoResponse> updateMyInfo(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserDto.UpdateRequest request
    ) {
        String email = userDetails.getUsername();
        UserDto.InfoResponse updatedInfo = userService.updateMyInfo(email, request);
        return ResponseEntity.ok(updatedInfo);
    }
}
