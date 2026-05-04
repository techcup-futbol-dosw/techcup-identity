package edu.eci.dosw.model;

import java.util.ArrayList;
import java.util.List;

public class Role {

    private Long id;
    private String name;
    private List<Permission> permissions;

    public Role() {}

    public Role(Long id, String name, List<Permission> permissions) {
        this.id = id;
        this.name = name;
        this.permissions = permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Permission> getPermissions() { return permissions; }
    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions != null ? new ArrayList<>(permissions) : new ArrayList<>();
    }

    public boolean hasPermission(String permissionName) {
        return permissions.stream().anyMatch(p -> p.getName().equalsIgnoreCase(permissionName));
    }
}
