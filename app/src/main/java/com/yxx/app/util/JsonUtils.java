package com.yxx.app.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Created by yangla on 2018/7/11 0011.
 * Json 解析帮助类
 */

public class JsonUtils {

    /**
     * json字符串转JavaBean
     * @param json json字符串
     * @param <T>
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.fromJson(json,clazz);
    }

    /**
     * json字符串转List集合
     * @param json  json字符串
     * @param clazz 实体类
     * @return
     */
    public static <T> List<T> fromJsonArray(String json, Class clazz) throws JsonSyntaxException {
        Gson gson = new Gson();
        Type objectType = type(List.class, clazz);
        return gson.fromJson(json, objectType);
    }

    /**
     * 实体类转json字符串
     * @param clazz 实体类
     * @return
     */
    public static <T> String toJson(Class<T> clazz) throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.toJson(clazz);
    }

    public static <T> String toJson(T t) throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.toJson(t);
    }

    public static <T> String toJson(List<T> list){
        Gson gson = new Gson();
        return gson.toJson(list);
    }

    /**
     * 实体类转为json字符串
     * @param list
     * @param options  需要过滤的字段
     * @param <T>
     * @return
     */
    public static <T> String toJson(List<T> list, String... options){

        ExclusionStrategy excludeStrategy = new SetterExclusionStrategy(options);
        Gson gson = new GsonBuilder().setExclusionStrategies(excludeStrategy)
                .create();
        return gson.toJson(list);
    }

    public static ParameterizedType type(final Class raw, final Type... args) {
        return new ParameterizedType() {
            public Type getRawType() {
                return raw;
            }

            public Type[] getActualTypeArguments() {
                return args;
            }

            public Type getOwnerType() {
                return null;
            }
        };
    }

    /**
     * 转JSON 字符串， 单个对象
     * @param params
     * @return
     * @throws Exception
     */
    public static String toJson(Map<String, String> params){
        StringBuffer buffer = new StringBuffer();
        buffer.append("{");
        if(params != null){
            try {
                int num = 0;
                for(Map.Entry<String, String> entry : params.entrySet()){
                    buffer.append("\"" + entry.getKey() + "\":");
                    buffer.append("\"" + entry.getValue() + "\"");
                    if(num != params.size() - 1){
                        buffer.append(",");
                    }
                    num++;
                }
            }catch (Exception e){}
        }
        buffer.append("}");
        return buffer.toString();
    }

    /**
     * 过滤帮助类
     *
     * @author bamboo
     */
    private static class SetterExclusionStrategy implements ExclusionStrategy {
        private String[] fields;

        public SetterExclusionStrategy(String[] fields) {
            this.fields = fields;

        }

        @Override
        public boolean shouldSkipClass(Class<?> arg0) {
            return false;
        }

        /**
         * 过滤字段的方法
         */
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            if (fields != null) {
                for (String name : fields) {
                    if (f.getName().equals(name)) {
                        /** true 代表此字段要过滤 */
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
