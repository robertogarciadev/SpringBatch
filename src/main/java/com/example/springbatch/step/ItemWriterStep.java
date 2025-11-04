package com.example.springbatch.step;

import com.example.springbatch.model.PersonEntity;
import com.example.springbatch.repository.PersonRepository;
import com.example.springbatch.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

@Slf4j
public class ItemWriterStep implements Tasklet {

    private final PersonService personService;

    public ItemWriterStep(PersonService personService){
        this.personService=personService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        log.info("-------------INICIO DEL PASO DE ESCRITURA-------------");

        List<PersonEntity> personList;
        try {
            personList = (List<PersonEntity>) chunkContext
                    .getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .get("personList");
        } catch (ClassCastException e) {
            log.error("Error en el casteo del objeto en el paso de ESCRITURA");
            return RepeatStatus.FINISHED;
        }

        personService.saveAll(personList);

        log.info("-------------FIN DEL PASO DE ESCRITURA-------------");
        return RepeatStatus.FINISHED;
    }
}
