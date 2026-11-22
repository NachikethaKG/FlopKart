package com.flopkart.common.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockReservationRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<StockItemDto> items;

    public StockReservationRequest() {
        this.items = new ArrayList<>();
    }

    public StockReservationRequest(List<StockItemDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public List<StockItemDto> getItems() {
        return items;
    }

    public void setItems(List<StockItemDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(StockItemDto item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
}
