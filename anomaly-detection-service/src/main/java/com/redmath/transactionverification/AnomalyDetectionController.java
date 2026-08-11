package com.redmath.transactionverification;


import com.redmath.transactionverification.dto.TransactionEvaluationRequest;
import com.redmath.transactionverification.dto.TransactionEvaluationResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/risk")
@RestController
public class AnomalyDetectionController {

    private static final Logger log = LoggerFactory.getLogger(AnomalyDetectionController.class);
    private final AnomalyDetectionService anomalyDetectionService;

    public AnomalyDetectionController(AnomalyDetectionService anomalyDetectionService) {
        this.anomalyDetectionService = anomalyDetectionService;
    }


    @PostMapping("/evaluate")
    public ResponseEntity<TransactionEvaluationResponse> evaluateTransaction(@RequestBody TransactionEvaluationRequest request) {
        TransactionEvaluationResponse response = anomalyDetectionService.evaluateTransaction(request);
        log.info(String.valueOf(response));
        return ResponseEntity.ok(response);
    }

}
