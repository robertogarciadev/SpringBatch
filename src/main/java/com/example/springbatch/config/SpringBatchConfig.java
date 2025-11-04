package com.example.springbatch.config;


import com.example.springbatch.service.PersonService;
import com.example.springbatch.step.*;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.sql.init.DatabaseInitializationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class SpringBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public SpringBatchConfig(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager, DataSource dataSource) {
        this.jobRepository = jobRepository;
        this.transactionManager = platformTransactionManager;
    }

    @Bean
    public Job CSVReaderJob(
            ItemDescompressStep itemDescompressStep,
            ItemReaderStep itemReaderStep,
            ItemProcessorStep itemProcessorStep,
            ItemWriterStep itemWriterStep
    ){
        return new JobBuilder("CSVReaderJob", jobRepository)
                .start(step1(itemDescompressStep))
                .next(step2(itemReaderStep))
                .next(step3(itemProcessorStep))
                .next(step4(itemWriterStep))
                .build();
    }



    @Bean
    public Step step1(ItemDescompressStep tasklet) {
        return new StepBuilder("stepDescompress", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step step2(ItemReaderStep tasklet){
        return new StepBuilder("stepReader", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step step3(ItemProcessorStep tasklet){
        return  new StepBuilder("stepProcessor", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    public Step step4(ItemWriterStep tasklet){
        return new StepBuilder("stepWriter", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    @Bean
    @JobScope
    public ItemDescompressStep itemDescompressStep() {
        return new ItemDescompressStep();
    }

    @Bean
    @StepScope
    public ItemProcessorStep itemProcessorStep() {
        return new ItemProcessorStep();
    }

    @Bean
    @StepScope
    public ItemReaderStep itemReaderStep(ResourceLoader resourceLoader) {
        return new ItemReaderStep(resourceLoader);
    }

    @Bean
    @StepScope
    public ItemWriterStep itemWriterStep(PersonService personService) {
        return new ItemWriterStep(personService);
    }
}
