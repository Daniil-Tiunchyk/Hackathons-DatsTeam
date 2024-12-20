package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Snake {
    private String id;
    private int[] direction;
    private int[] oldDirection;
    private List<List<Integer>> geometry;
    private String status;
}
