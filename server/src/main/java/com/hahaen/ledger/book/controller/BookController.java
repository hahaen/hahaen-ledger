package com.hahaen.ledger.book.controller;

import com.hahaen.ledger.book.entity.LedgerBook;
import com.hahaen.ledger.book.service.BookService;
import com.hahaen.ledger.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/app/books") @RequiredArgsConstructor
public class BookController {
    private final BookService bookService;
    @GetMapping("/current") public ApiResponse<LedgerBook> current() { return ApiResponse.ok(bookService.current()); }
}
