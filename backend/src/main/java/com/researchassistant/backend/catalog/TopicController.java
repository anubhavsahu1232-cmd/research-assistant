package com.researchassistant.backend.catalog;
import com.researchassistant.backend.model.Topic;
import com.researchassistant.backend.repository.TopicRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/topics") public class TopicController {
 private final TopicRepository repo;
 public TopicController(TopicRepository repo){this.repo=repo;}
 public record Input(@NotBlank String name,String description){}
 @GetMapping public List<Topic> all(){return repo.findAll();}
 @GetMapping("/{id}") public Topic one(@PathVariable Long id){return repo.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));}
 @PreAuthorize("hasRole('ADMIN')") @PostMapping public ResponseEntity<Topic> create(@Valid @RequestBody Input i){Topic t=new Topic();t.name=i.name();t.description=i.description();return ResponseEntity.status(201).body(repo.save(t));}
 @PreAuthorize("hasRole('ADMIN')") @PutMapping("/{id}") public Topic update(@PathVariable Long id,@Valid @RequestBody Input i){Topic t=one(id);t.name=i.name();t.description=i.description();return repo.save(t);}
 @PreAuthorize("hasRole('ADMIN')") @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id){one(id);repo.deleteById(id);return ResponseEntity.noContent().build();}
}
