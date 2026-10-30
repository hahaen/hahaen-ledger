package com.hahaen.ledger.lottery.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hahaen.ledger.common.response.ApiResponse;
import com.hahaen.ledger.lottery.dto.*;
import com.hahaen.ledger.lottery.service.LotteryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/app/lottery")
@RequiredArgsConstructor
public class LotteryController {
    private final LotteryService service;
    @GetMapping public ApiResponse<ObjectNode> list(@RequestParam(defaultValue="DLT") String game,@RequestParam(defaultValue="single") String kind,@RequestParam(defaultValue="1") int page){return ApiResponse.ok(service.overview(game,kind,page));}
    @GetMapping("/draws") public ApiResponse<ObjectNode> draws(@RequestParam(defaultValue="DLT") String game,@RequestParam(defaultValue="1") int page){return ApiResponse.ok(service.draws(game,page));}
    @GetMapping("/{kind}/{id}") public ApiResponse<ObjectNode> detail(@PathVariable String kind,@PathVariable long id,@RequestParam(defaultValue="1") int page){return ApiResponse.ok(service.detail(kind,id,page));}
    @PostMapping("/{kind}") public ApiResponse<String> create(@PathVariable String kind,@Valid @RequestBody LotterySaveRequest request){return ApiResponse.ok(service.create(kind,request));}
    @PostMapping("/plan/{id}/purchase") public ApiResponse<String> purchase(@PathVariable long id,@Valid @RequestBody LotterySaveRequest request){return ApiResponse.ok(service.purchase(id,request));}
    @PutMapping("/{kind}/{id}") public ApiResponse<String> edit(@PathVariable String kind,@PathVariable long id,@Valid @RequestBody LotterySaveRequest request){return ApiResponse.ok(service.edit(kind,id,request));}
    @PostMapping("/{kind}/{id}/{action}") public ApiResponse<String> action(@PathVariable String kind,@PathVariable long id,@PathVariable String action,@Valid @RequestBody LotteryActionRequest request){return ApiResponse.ok(service.action(kind,id,action,request));}
}
