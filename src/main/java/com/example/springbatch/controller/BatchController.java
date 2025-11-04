package com.example.springbatch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Slf4j
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job job;

    public BatchController(JobLauncher jobLauncher, Job job) {
        this.jobLauncher = jobLauncher;
        this.job = job;
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<?> receiveFile(@RequestParam(name = "file") MultipartFile multipartFile){

        try {
            // Crear archivo temporal en el sistema
            String prefix = "uploaded-"+ System.currentTimeMillis();
            Path tempFile = File.createTempFile(prefix, ".csv").toPath();
            log.info("Creado archivo temporal en disco");

            // Copia el contenido del archivo que manda el usuario al archivo temporal creado
            multipartFile.transferTo(tempFile);
            log.info("Copiado archivo de usuario al archivo temporal");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("input.file.path", tempFile.toString())
                    .addDate("date", new Date())
                    .toJobParameters();

            jobLauncher.run(job, jobParameters);
            log.info("Job lanzado");

            return  ResponseEntity.ok(response());

        }catch (IOException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }catch (JobExecutionException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fallo en el procesamiento del Job");
        }

    }

    private Map<String, String> response(){
        return Map.of(
                "Estado", "Archivo recibido"
        );
    }
}
