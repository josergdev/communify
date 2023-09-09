package dev.joserg.communify.controller;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RootController {

  private final BuildProperties buildProperties;

  @SneakyThrows
  @GetMapping("/public")
  public String publicResource(@RequestParam Integer back, @RequestParam Integer depth) {
    var fileList = new ArrayList<String>();
    Files.walkFileTree(this.goBack(Paths.get(System.getProperty("user.dir")), back), Set.of(), depth, new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        if (!Files.isDirectory(file)) {
          fileList.add(file.toFile().getAbsolutePath());
        }
        return FileVisitResult.CONTINUE;
      }
    });
    return MessageFormat.format("<pre>{0}</pre>", String.join(System.lineSeparator(), fileList));
  }

  private Path goBack(Path current, Integer times) {
    if (times <= 0) {
      return current;
    }
    return goBack(current.getParent(), times - 1);
  }

  @GetMapping("/private")
  @PreAuthorize("hasAuthority('SCOPE_Root.Private')")
  public Map<String, Object> privateResource(JwtAuthenticationToken jwtAuthenticationToken) {
    return jwtAuthenticationToken.getTokenAttributes();
  }

}
