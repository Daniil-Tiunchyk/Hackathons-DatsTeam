package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SpecialFood {
    private List<List<Integer>> golden;
    private List<List<Integer>> suspicious;
}
