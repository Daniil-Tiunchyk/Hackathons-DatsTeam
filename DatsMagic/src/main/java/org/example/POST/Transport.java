package org.example.POST;

import lombok.Data;

@Data
public class Transport {
    private String id;
    private Acceleration acceleration;
    private boolean activateShield;
    private Attack attack;
}
