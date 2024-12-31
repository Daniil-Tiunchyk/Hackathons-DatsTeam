package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Enemy {
    private List<List<Integer>> geometry;
    private String status;
    private int kills;
}
