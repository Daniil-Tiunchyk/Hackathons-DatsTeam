package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SpecialFood {
    private List<Point3D> golden;
    private List<Point3D> suspicious;
}
