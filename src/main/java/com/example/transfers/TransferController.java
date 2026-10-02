package com.example.transfers;

import com.example.transfers.TransferDtos.TransferRequest;
import com.example.transfers.TransferDtos.TransferResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    /** 201 for a new transfer, 200 when an idempotent replay returns the original result. */
    @PostMapping
    public ResponseEntity<TransferResponse> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        TransferService.TransferResult result = service.transfer(idempotencyKey, request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.transfer());
    }

    @GetMapping("/{id}")
    public TransferResponse get(@PathVariable UUID id) {
        return service.get(id);
    }
}
