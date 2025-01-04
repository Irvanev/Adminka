package dev.irvanev.domain.repositories;

import dev.irvanev.domain.models.StudentEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends MongoRepository<StudentEntity, String> {
}
