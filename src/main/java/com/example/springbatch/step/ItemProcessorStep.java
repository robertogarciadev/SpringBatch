package com.example.springbatch.step;

import com.example.springbatch.model.PersonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ItemProcessorStep implements Tasklet {
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {


        log.info("-------------INICIO DEL PASO DE PROCESAMIENTO-------------");

        List<PersonEntity> personList;

        try {
            personList = (List<PersonEntity>) chunkContext
                    .getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .get("personList");
        } catch (ClassCastException e) {
            log.error("Error en el casteo del objeto en el paso de PROCESAMIENTO");
            return RepeatStatus.FINISHED;
        }


        if (personList.isEmpty()) {
            log.error("La lista de personas está vacía o no se encontró en el contexto.");
            return RepeatStatus.FINISHED;
        }

        // Procesamiento
        personList.forEach(person -> person.setProcessorDate(LocalDateTime.now()));


        //Pasa al contexto de Spring Batch la lista para que pueda ser compartida por el resto de steps
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("personList", personList);

        log.info("-------------FIN DEL PASO DE PROCESAMIENTO-------------");

        return RepeatStatus.FINISHED;
    }
}
