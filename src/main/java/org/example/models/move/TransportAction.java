package org.example.models.move;

import lombok.Data;

@Data
public class TransportAction {
    private String id;
    private Vector2D acceleration;
    private boolean activateShield;
    private Vector2D attack;
}
