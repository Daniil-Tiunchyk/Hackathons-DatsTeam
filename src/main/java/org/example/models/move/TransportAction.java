package org.example.models.move;

import lombok.Data;
import org.example.POST.Attack;

@Data
public class TransportAction {
    private String id;
    private Vector2D acceleration;
    private boolean activateShield;
    private Attack attack;
}
