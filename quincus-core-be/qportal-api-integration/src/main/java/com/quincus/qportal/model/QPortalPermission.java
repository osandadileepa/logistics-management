package com.quincus.qportal.model;

import lombok.Data;

import java.util.List;

@Data
public class QPortalPermission {
    private String id;
    private String name;
    private String version;
    private List<Page> pages;

    @Data
    public static class Page {
        private String id;
        private String name;
        private List<Action> actions;

        @Data
        public static class Action {
            private String id;
            private String name;
        }
    }
}
