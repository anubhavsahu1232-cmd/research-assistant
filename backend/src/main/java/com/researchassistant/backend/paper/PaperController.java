package com.researchassistant.backend.paper;
import com.researchassistant.backend.model.*;
import com.researchassistant.backend.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
@RestController @RequestMapping("/api/papers") public class PaperController {
 private final PaperRepository papers;private final UserRepository users;private final CategoryRepository categories;private final TopicRepository topics;
 public PaperController(PaperRepository papers,UserRepository users,CategoryRepository categories,TopicRepository topics){this.papers=papers;this.users=users;this.categories=categories;this.topics=topics;}
 public record Input(@NotBlank String title,String abstractText,String authors,@Min(1000) @Max(2200) Integer publicationYear,String keywords,Long categoryId,Long topicId){}
 public record View(Long id,String title,String abstractText,String authors,Integer publicationYear,String keywords,Long categoryId,Long topicId,Long uploaderId,String pdfUrl){}
 public View view(Paper x){return new View(x.id,x.title,x.abstractText,x.authors,x.publicationYear,x.keywords,x.category==null?null:x.category.id,x.topic==null?null:x.topic.id,x.uploader==null?null:x.uploader.id,x.pdfFilePath==null?null:"/api/papers/"+x.id+"/pdf");}
 public Paper find(Long id){return papers.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Paper not found"));}
 private void fill(Paper x,Input i){
  x.title=i.title();x.abstractText=i.abstractText();x.authors=i.authors();x.publicationYear=i.publicationYear();x.keywords=i.keywords();
  x.category=i.categoryId()==null?null:categories.findById(i.categoryId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid category"));
  x.topic=i.topicId()==null?null:topics.findById(i.topicId()).orElseThrow(()->new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid topic"));
 }
 public void owner(Paper x,Authentication a){if(x.uploader==null || (!x.uploader.email.equals(a.getName()) && a.getAuthorities().stream().noneMatch(z->z.getAuthority().equals("ROLE_ADMIN")))) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not owner");}
 @GetMapping public Page<View> list(@RequestParam(required=false) String q,@RequestParam(required=false) Long categoryId,@RequestParam(required=false) Long topicId,@RequestParam(required=false) Integer year,@RequestParam(defaultValue="0") @Min(0) int page,@RequestParam(defaultValue="10") @Min(1) @Max(100) int size){
  Specification<Paper> spec=(root,query,cb)->cb.conjunction();
  if(q!=null&&!q.isBlank()){String term="%"+q.toLowerCase(Locale.ROOT)+"%";spec=spec.and((root,query,cb)->cb.or(cb.like(cb.lower(root.get("title")),term),cb.like(cb.lower(root.get("authors")),term),cb.like(cb.lower(root.get("keywords")),term)));}
  if(categoryId!=null)spec=spec.and((root,query,cb)->cb.equal(root.get("category").get("id"),categoryId));
  if(topicId!=null)spec=spec.and((root,query,cb)->cb.equal(root.get("topic").get("id"),topicId));
  if(year!=null)spec=spec.and((root,query,cb)->cb.equal(root.get("publicationYear"),year));
  return papers.findAll(spec,PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"id"))).map(this::view);
 }
 @GetMapping("/{id}") public View one(@PathVariable Long id){return view(find(id));}
 @PostMapping public ResponseEntity<View> create(@Valid @RequestBody Input i,Authentication a){
  Paper x=new Paper();fill(x,i);x.uploader=users.findByEmail(a.getName()).orElseThrow();return ResponseEntity.status(201).body(view(papers.save(x)));
 }
 @PutMapping("/{id}") public View update(@PathVariable Long id,@Valid @RequestBody Input i,Authentication a){Paper x=find(id);owner(x,a);fill(x,i);return view(papers.save(x));}
 @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id,Authentication a){Paper x=find(id);owner(x,a);papers.delete(x);return ResponseEntity.noContent().build();}
}
