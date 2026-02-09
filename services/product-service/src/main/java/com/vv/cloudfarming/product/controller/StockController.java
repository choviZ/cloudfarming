package com.vv.cloudfarming.product.controller;

import com.vv.cloudfarming.common.result.Result;
import com.vv.cloudfarming.common.result.Results;
import com.vv.cloudfarming.product.service.StockService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "库存控制层")
@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping("/api/stock/lock")
    public Result<Void> lock(@RequestParam @NotNull Long id, @RequestParam @NotNull int quantity, @RequestParam @NotNull int bizType){
        stockService.lock(id, quantity, bizType);
        return Results.success();
    }

    @GetMapping("/api/stock/unlock")
    public Result<Void> unlock(@RequestParam @NotNull Long id, @RequestParam @NotNull int quantity, @RequestParam @NotNull int bizType){
        stockService.unlock(id, quantity, bizType);
        return Results.success();
    }
}
