package org.example.models.move;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MoveResponse {
    private List<TransportAction> transports;
}
