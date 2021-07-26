package com.yxx.app.bean;

import java.io.Serializable;
import java.util.List;

/**
 * Author: yangxl
 * Date: 2021/7/13 10:20
 * Description:
 */
public class ProInfo implements Serializable {

    private String id;

    private String name;

    private String file;

    private List<ProInfo> cities;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<ProInfo> getCities() {
        return cities;
    }

    public void setCities(List<ProInfo> cities) {
        this.cities = cities;
    }
}
