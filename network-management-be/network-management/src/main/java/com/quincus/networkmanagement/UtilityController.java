package com.quincus.networkmanagement;

import com.fasterxml.jackson.databind.JsonNode;
import com.quincus.mme.model.MmeGetTrainedModelRequest;
import com.quincus.networkmanagement.api.domain.TrainingLog;
import com.quincus.networkmanagement.api.filter.SearchFilterResult;
import com.quincus.web.common.model.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;

@RequestMapping("/utilities")
@Tag(name = "utilities", description = "Utility endpoints")
public interface UtilityController {

    @GetMapping("/generate-training-input")
    @Operation(summary = "Generate Training Input API", description = "Generate training input for MME", tags = "utilities")
    JsonNode generateTrainingInput(@RequestParam(value = "date", required = false) String date) throws ParseException;

    @GetMapping("/mme-train-model")
    @Operation(summary = "Initiate Train Model API", description = "Get payload required to trigger train model", tags = "utilities")
    TrainingLog trainModel();

    @PostMapping("/mme-get-train-status")
    @Operation(summary = "Get the status of the Trained model", description = "Get the status of the trained model from unique_id", tags = "utilities")
    TrainingLog getTrainedModel(@RequestBody MmeGetTrainedModelRequest trainRequest);

    @GetMapping("/training-logs")
    @Operation(summary = "List Training Logs", description = "List existing training logs", tags = "utilities")
    Response<SearchFilterResult<TrainingLog>> list(
            @PageableDefault(size = 5, sort = {"createTime"}, direction = Sort.Direction.DESC) Pageable pageable
    );
}
