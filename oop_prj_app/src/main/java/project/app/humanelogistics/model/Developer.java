package project.app.humanelogistics.model;

public class Developer {
    private final String name;
    private final String role;
    private final String imagePath;

    public Developer(String name, String role, String imagePath) {
        this.name = name;
        this.role = role;
        this.imagePath = imagePath;
    }

    public String getName() { return name; }
    public String getRole() { return role; }
    public String getImagePath() { return imagePath; }
}