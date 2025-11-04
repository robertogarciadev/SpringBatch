package com.example.springbatch.step;

import com.example.springbatch.model.PersonEntity;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
public class ItemReaderStep implements Tasklet {

    private final ResourceLoader resourceLoader; // Clase que permite importar recursos

    public ItemReaderStep(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {


        log.info("-------------INICIO DEL PASO DE LECTURA-------------");

        File file = resourceLoader.getResource("classpath:file/destination/persons.csv").getFile();
        Reader reader = new FileReader(file); // Permite leer cualquier documento

        // Especifica el separador del archivo
        CSVParser parse = new CSVParserBuilder()
                .withSeparator(',')
                .build();

        // Lee el arcchivo csv. usa el Reader y CSVParse definidos anteriormente
        CSVReader csvReader = new CSVReaderBuilder(reader)
                .withSkipLines(1)
                .withCSVParser(parse)
                .build();

        var personList = new ArrayList<PersonEntity>();
        String[] actualLine;

        while ((actualLine = csvReader.readNext()) != null) {
            PersonEntity personEntity = PersonEntity.builder()
                    .name(actualLine[0])
                    .lastName(actualLine[1])
                    .age(Integer.parseInt(actualLine[2]))
                    .email(actualLine[3])
                    .city(actualLine[4])
                    .registered_date(LocalDateTime.parse(actualLine[5]))
                    .build();

            personList.add(personEntity);
        }

        csvReader.close();
        reader.close();

        log.info("-------------FIN DEL PASO DE LECTURA-------------");

        //Pasa al contexto de Spring Batch la lista para que pueda ser compartida por el resto de steps
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("personList", personList);

        return RepeatStatus.FINISHED;
    }
}
