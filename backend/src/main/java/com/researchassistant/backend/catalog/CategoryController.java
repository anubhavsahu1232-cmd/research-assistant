package com.researchassistant.backend.catalog;
import com.researchassistant.backend.model.Category;
import com.researchassistant.backend.repository.CategoryRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/categories") public class CategoryController {
 private final CategoryRepository repo;
 public CategoryController(CategoryRepository repo){this.repo=repo;}
 public record Input(@NotBlank String name,String description){}
 @GetMapping public List<Category> all(){return repo.findAll();}
 @GetMapping("/{id}") public Category one(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PreAuthorize("hasRole('ADMIN')") @PostMapping public ResponseEntity<Category> create(@Valid @RequestBody Input i){Category c=new Category();c.name=i.name();c.description=i.description();return ResponseEntity.status(201).body(repo.save(c));}
 @PreAuthorize("hasRole('ADMIN')") @PutMapping("/{id}") public Category update(@PathVariable Long id,@Valid @RequestBody Input i){Category c=one(id);c.name=i.name();c.description=i.description();return repo.save(c);}
 @PreAuthorize("hasRole('ADMIN')") @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){one(id);repo.deleteById(id);return ResponseEntity.noContent().build();}
}
