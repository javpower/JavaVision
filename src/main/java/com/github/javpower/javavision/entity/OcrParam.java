package com.github.javpower.javavision.entity;

import lombok.Data;

import java.util.List;

@Data
public class OcrParam {

    private String key;

    private List<String> content;
}
