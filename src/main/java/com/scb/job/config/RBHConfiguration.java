package com.scb.job.config;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

@Configuration
public class RBHConfiguration {

  @Getter
  private String apikey;


  @SneakyThrows
  public RBHConfiguration(@Value("${secretsPath}") String secretsPath) {
    final URI apiKeyPath = ResourceUtils.getURL(secretsPath + "/RBHCALLBACK_API_KEY").toURI();
    this.apikey = sanitize(Files.readAllBytes(Paths.get(apiKeyPath)));
  }

  @SneakyThrows
  private String sanitize(byte[] strBytes) {
    return new String(strBytes)
        .replace("\r", "")
        .replace("\n", "");
  }
}