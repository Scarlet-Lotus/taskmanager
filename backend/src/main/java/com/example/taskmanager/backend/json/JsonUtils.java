package com.example.taskmanager.backend.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonUtils {

    private static final ObjectMapper objectMapper;

    // Инициализация ObjectMapper с настройками по умолчанию
    static {
        objectMapper = new ObjectMapper();
        // Добавляем поддержку Java 8 Date/Time API
        objectMapper.registerModule(new JavaTimeModule());
        // Настройка красивого форматирования JSON
        objectMapper.writerWithDefaultPrettyPrinter();
    }

    /**
     * Преобразует объект в JSON-строку.
     *
     * @param object объект для преобразования
     * @return JSON-строка
     * @throws JsonProcessingException если произошла ошибка при сериализации
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Преобразует JSON-строку в объект заданного типа.
     *
     * @param json      JSON-строка
     * @param valueType класс объекта, в который нужно преобразовать JSON
     * @param <T>       тип объекта
     * @return десериализованный объект
     * @throws JsonProcessingException если произошла ошибка при десериализации
     */
    public static <T> T fromJson(String json, Class<T> valueType) throws JsonProcessingException {
        return objectMapper.readValue(json, valueType);
    }

    /**
     * Преобразует JSON-строку в объект сложного типа (например, List или Map).
     *
     * @param json          JSON-строка
     * @param typeReference TypeReference для сложного типа
     * @param <T>           тип объекта
     * @return десериализованный объект
     * @throws JsonProcessingException если произошла ошибка при десериализации
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) throws JsonProcessingException {
        return objectMapper.readValue(json, typeReference);
    }

    /**
     * Читает JSON из файла и преобразует его в объект заданного типа.
     *
     * @param file      файл с JSON
     * @param valueType класс объекта, в который нужно преобразовать JSON
     * @param <T>       тип объекта
     * @return десериализованный объект
     * @throws IOException если произошла ошибка при чтении файла
     */
    public static <T> T fromFile(File file, Class<T> valueType) throws IOException {
        return objectMapper.readValue(file, valueType);
    }

    /**
     * Записывает объект в файл в формате JSON.
     *
     * @param object объект для записи
     * @param file   файл, куда будет записан JSON
     * @throws IOException если произошла ошибка при записи файла
     */
    public static void toFile(Object object, File file) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, object);
    }

    /**
     * Преобразует объект в JSON-строку с красивым форматированием.
     *
     * @param object объект для преобразования
     * @return JSON-строка с форматированием
     * @throws JsonProcessingException если произошла ошибка при сериализации
     */
    public static String toPrettyJson(Object object) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    /**
     * Преобразует JSON-строку в Map.
     *
     * @param json JSON-строка
     * @return Map, представляющая JSON
     * @throws JsonProcessingException если произошла ошибка при десериализации
     */
    public static Map<String, Object> toMap(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Преобразует JSON-строку в List объектов заданного типа.
     *
     * @param json      JSON-строка
     * @param valueType класс элементов списка
     * @param <T>       тип элементов списка
     * @return List объектов
     * @throws JsonProcessingException если произошла ошибка при десериализации
     */
    public static <T> List<T> toList(String json, Class<T> valueType) throws JsonProcessingException {
        return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, valueType));
    }
}
