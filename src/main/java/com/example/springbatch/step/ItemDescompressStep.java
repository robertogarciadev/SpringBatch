package com.example.springbatch.step;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Slf4j
public class ItemDescompressStep implements Tasklet {

    // Inyecta el parámetro del job, que contiene la ruta completa del archivo temporal
    // Este valor se pasa desde el controller cuando se lanza el job:
    // new JobParametersBuilder().addString("input.file.path", tempFile.toString())
    @Value("#{jobParameters['input.file.path']}")
    private String inputFilePath;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("-------------INICIO DEL PASO DE DESCOMPRESIÓN-------------");

        // Recupera CSV guardado desde el controller
        // FileSystemResources para archivos para recuperar archivos temporales en disco
        Resource resource = new FileSystemResource(inputFilePath);

        if (!resource.exists()) {
            log.error("El archivo no existe en la ruta: {}", inputFilePath);
            throw new FileNotFoundException("Archivo no encontrado: " + inputFilePath);
        }

        // 📦 Obtiene una referencia java.io.File al archivo comprimido real.
        File compressedFile = resource.getFile();

        // 📂 Define referencia para descomprimir archivo
        File outputDir = new File(compressedFile.getParent(), "decompressed");

        unzip(compressedFile, outputDir);

        // 💾 Guarda la ruta de la carpeta descomprimida en el contexto del Job.
        // Así, otros steps podrán acceder a ella.
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("decompressed.path", outputDir.getAbsolutePath());

        return RepeatStatus.FINISHED;
    }

    private void unzip(File fileCompressed, File destinationFile) throws IOException {

        // 🗜️ Crea un objeto ZipFile para leer el contenido del ZIP.
        ZipFile zipFile = new ZipFile(fileCompressed);

        // 📜 Obtiene una enumeración de todas las entradas (archivos o carpetas) dentro del ZIP.
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        // Recorre todas las entradas
        while (entries.hasMoreElements()) {

            // Siguiente entrada (archivo o carpeta)
            ZipEntry zipEntry = entries.nextElement();

            // Crea un nuevo archivo destino combinando la carpeta de salida + el nombre del archivo en el ZIP
            File file = new File(destinationFile, zipEntry.getName());

            // Si la entrada es una carpeta, la crea
            if (file.isDirectory()) {
                file.mkdirs();
            } else {
                // ✅ Asegura que las carpetas destino existen
                file.getParentFile().mkdirs();

                // 🧩 Si es un archivo, abre un InputStream para leer su contenido comprimido (secuencia bytes)
                InputStream inputStream = zipFile.getInputStream(zipEntry);

                // 📤 Crea un OutputStream para escribir el contenido descomprimido en el archivo destino.
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }

                fileOutputStream.close();
                inputStream.close();
            }
        }
        zipFile.close();
    }
}
