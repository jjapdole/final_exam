package org.zerock.final_test.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zerock.final_test.service.MainService;

import java.io.*;
import java.util.*;
import java.util.zip.*;

@Controller
@RequestMapping("/controller")
public class MainController {

    private final MainService mainService;

    public MainController(MainService mainService) {
        this.mainService = mainService;
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // login.html 반환
    }

    @GetMapping("/signup")
    public String showSignupPage() {
        return "signup"; // signup.html 반환
    }

    @GetMapping("/main")
    public String showMainPage() {
        return "main"; // main.html 반환
    }

    @GetMapping("/loginError")
    public String showLoginErrorPage() {
        return "login_error"; // login_error.html 반환
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password, RedirectAttributes redirectAttributes) {
        try {
            mainService.register(username, password);
            return "redirect:/controller/login"; // 성공 시 로그인 페이지로 리다이렉트
        } catch (IllegalStateException e) {
            return "redirect:/controller/signUpError"; // 실패 시 에러 페이지로 리다이렉트
        }
    }

    @GetMapping("/signUpError")
    public String showSignUpErrorPage() {
        return "signup_error"; // login_error.html 반환
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }

    @PostMapping("/uploadMapBatch")
    public String uploadMapBatch(@RequestParam("mapZipFile") MultipartFile zipFile, RedirectAttributes redirectAttributes) {
        if (zipFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please upload a ZIP file.");
            return "redirect:/controller/main";
        }

        try {
            String uploadDir = "src/main/resources/static/maps";
            File uploadPath = new File(uploadDir);
            if (!uploadPath.exists()) {
                uploadPath.mkdirs();
            }

            // ZIP 파일 해제
            try (InputStream inputStream = zipFile.getInputStream();
                 ZipInputStream zis = new ZipInputStream(inputStream)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    String filePath = uploadDir + File.separator + entry.getName();

                    if (entry.isDirectory()) {
                        new File(filePath).mkdirs();
                        continue;
                    }

                    try (FileOutputStream fos = new FileOutputStream(filePath)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length); // 올바른 메서드 호출
                        }
                    }
                }
            }

            redirectAttributes.addFlashAttribute("message", "Maps uploaded successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Failed to upload maps: " + e.getMessage());
        }

        return "redirect:/controller/main";
    }

    @GetMapping("/getMap")
    public ResponseEntity<Map<String, String>> getMapTile(@RequestParam int x, @RequestParam int y) {
        String fileName = "tile_" + x + "_" + y + ".png";
        String filePath = "src/main/resources/static/maps/map_tile_data/" + fileName;

        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Requested map tile not found"));
        }

        Map<String, String> response = new HashMap<>();
        response.put("fileName", "maps/map_tile_data/" + fileName);
        return ResponseEntity.ok(response);
    }
}