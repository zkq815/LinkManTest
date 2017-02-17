package com.meizu.linkmantest.model;

import java.util.ArrayList;

/**
 * Created by zkq on 2017/1/18.
 */

public class LinkManBean {
    public String id;
    public String name;
    public String sort_key_primary;
    public ArrayList<String> phoneNumbers;

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

    public String getSort_key_primary() {
        return sort_key_primary;
    }

    public void setSort_key_primary(String sort_key_primary) {
        this.sort_key_primary = sort_key_primary;
    }

    public ArrayList<String> getPhoneNumbers() {
        return phoneNumbers;
    }

    public void setPhoneNumbers(ArrayList<String> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }
}
