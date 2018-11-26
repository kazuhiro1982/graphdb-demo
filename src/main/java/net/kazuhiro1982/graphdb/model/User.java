package net.kazuhiro1982.graphdb.model;

import java.util.List;

public class User {

    private String name;

    private List<String> recommends;

    private List<String> purchased;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRecommends() {
        return recommends;
    }

    public void setRecommends(List<String> recommends) {
        this.recommends = recommends;
    }

    public List<String> getPurchased() {
        return purchased;
    }

    public void setPurchased(List<String> purchased) {
        this.purchased = purchased;
    }

}
