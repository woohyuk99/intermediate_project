package com.deokhugam.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@Profile("prod")
@RequestMapping("/api/attachments")
public class AttachmentRedirectController {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Value("${cloud.aws.s3.prefix:uploads}")
    private String prefix;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Void> redirectToS3(@PathVariable String filename) {
        String enc = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String url = String.format(
                "https://%s.s3.%s.amazonaws.com/%s/attachments/%s",
                bucket, region, prefix, enc
        );
        return ResponseEntity.status(302).location(URI.create(url)).build();
    }

    @GetMapping("/_ping")
    public String ping() { return "ok"; }
}
