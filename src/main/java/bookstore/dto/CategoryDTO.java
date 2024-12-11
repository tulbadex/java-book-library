package bookstore.dto;

import jakarta.validation.constraints.NotBlank;


public class CategoryDTO {
    
    @NotBlank(message = "Name is required")
    private String name;

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
