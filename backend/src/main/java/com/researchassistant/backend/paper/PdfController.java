package com.researchassistant.backend.paper;
import com.researchassistant.backend.model.Paper;
import com.researchassistant.backend.repository.PaperRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;
@RestController @RequestMapping("/api/papers") public class PdfController {
 private final PaperController controller;private final PaperRepository papers;private final Path uploadRoot;
 public PdfController(PaperController controller,PaperRepository papers,@Value("${app.upload-dir}") String uploadDir){this.controller=controller;this.papers=papers;this.uploadRoot=Path.of(uploadDir).toAbsolutePath().normalize();}
 @PostMapping(value="/{id}/pdf",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) public PaperController.View upload(@PathVariable Long id,@RequestParam("file") MultipartFile file,Authentication auth)throws IOException {
  Paper paper=controller.find(id);controller.owner(paper,auth);
  if(file.isEmpty()||file.getSize()>15L*1024*1024)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid PDF size");
  byte[] header=new byte[5];try(InputStream in=file.getInputStream()){if(in.read(header)!=5||!java.util.Arrays.equals(header,"%PDF-".getBytes(java.nio.charset.StandardCharsets.US_ASCII)))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"File must be a PDF");}
  Files.createDirectories(uploadRoot);String name=UUID.randomUUID()+".pdf";Path dest=uploadRoot.resolve(name).normalize();
  try(InputStream in=file.getInputStream()){Files.copy(in,dest,StandardCopyOption.REPLACE_EXISTING);}
  String old=paper.pdfFilePath;paper.pdfFilePath=name;papers.save(paper);
  if(old!=null)Files.deleteIfExists(uploadRoot.resolve(old).normalize());
  return controller.view(paper);
 }
 @GetMapping("/{id}/pdf") public ResponseEntity<Resource> download(@PathVariable Long id)throws IOException {
  Paper paper=controller.find(id);
  if(paper.pdfFilePath==null)throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  Path path=uploadRoot.resolve(paper.pdfFilePath).normalize();
  if(!path.startsWith(uploadRoot)||!Files.isRegularFile(path))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
  return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF).header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\"paper-"+id+".pdf\"").body(new FileSystemResource(path));
 }
}
