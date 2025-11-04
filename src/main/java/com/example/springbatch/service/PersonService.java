package com.example.springbatch.service;

import com.example.springbatch.dto.PersonDTO;
import com.example.springbatch.model.PersonEntity;
import com.example.springbatch.repository.PersonRepository;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class PersonService {

    private final PersonRepository personRepository;

    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<PersonDTO> saveAll(List<PersonEntity> list){
        return personRepository.saveAll(list).stream()
                .map(this::toDTO)
                .toList();
    }

    private PersonDTO toDTO(PersonEntity entity){
        return new PersonDTO(
                entity.getId(),
                entity.getName(),
                entity.getLastName(),
                entity.getAge(),
                entity.getEmail(),
                entity.getCity(),
                entity.getRegistered_date().toString(),
                entity.getProcessorDate().toString());
    }
}
