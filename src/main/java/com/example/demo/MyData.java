package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minidev.json.JSONObject;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyData {
    private JsonNode data;
    private String path;
}