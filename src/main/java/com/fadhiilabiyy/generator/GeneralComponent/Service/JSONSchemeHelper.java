package com.fadhiilabiyy.generator.GeneralComponent.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.PathNotFoundException;

public class JSONSchemeHelper {
    public static <T> T getFieldOrDefault(JsonNode node, String fieldName, T defaultValue) {
        JsonNode fieldNode;

        try {
            if (fieldName.isEmpty()) {
                fieldNode = null;
            } else fieldNode = node.at("/" + fieldName.replaceAll("\\.", "/"));
        } catch (PathNotFoundException e) {
            fieldNode = null;
        }

        if (fieldNode != null && !fieldNode.isNull() && !fieldNode.isMissingNode()) {
            if (defaultValue instanceof String) {
                if (!(fieldNode instanceof TextNode))
                    return (T) fieldNode.toString();
                return (T) fieldNode.textValue();
            } else if (defaultValue instanceof Integer) {
                return (T) (Integer) fieldNode.asInt(0);
            } else if (defaultValue instanceof Long) {
                return (T) (Long) fieldNode.asLong(0);
            } else if (defaultValue instanceof Boolean) {
                return (T) (Boolean) fieldNode.asBoolean(false);
            } else if (defaultValue instanceof ObjectNode) {
                if (fieldNode instanceof MissingNode) {
                    return defaultValue;
                }
                return (T) fieldNode;
            } else if (defaultValue instanceof ArrayNode) {
                return (T) fieldNode;
            } else {
                throw new IllegalArgumentException("Unsupported type: " + defaultValue.getClass());
            }
        } else {
            return defaultValue;
        }
    }
}
