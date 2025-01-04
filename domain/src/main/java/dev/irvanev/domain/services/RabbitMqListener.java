package dev.irvanev.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.irvanev.domain.models.StudentDto;
import dev.irvanev.domain.models.StudentEntity;
import dev.irvanev.domain.repositories.StudentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class RabbitMqListener {
    private final StudentRepository studentRepository;

    @Autowired
    public RabbitMqListener(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @RabbitListener(queues = "students.queue")
    public void handleMessage(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            StudentDto studentDto = objectMapper.readValue(message, StudentDto.class);

            System.out.println("Message received: " + studentDto);

            if (studentDto.getId() == null) {
                StudentEntity student = new StudentEntity(
                        studentDto.getFirstName(),
                        studentDto.getLastName(),
                        studentDto.getAge(),
                        studentDto.getMajor()
                );
                studentRepository.save(student);
                System.out.println("Student created: " + student);
            } else if (studentDto.getFirstName() != null) {
                Optional<StudentEntity> studentOpt = studentRepository.findById(studentDto.getId());
                if (studentOpt.isPresent()) {
                    StudentEntity student = studentOpt.get();
                    student.setFirstName(studentDto.getFirstName());
                    student.setLastName(studentDto.getLastName());
                    student.setAge(studentDto.getAge());
                    student.setMajor(studentDto.getMajor());
                    studentRepository.save(student);
                    System.out.println("Student updated: " + student);
                }
            } else {
                studentRepository.deleteById(studentDto.getId());
                System.out.println("Student deleted with ID: " + studentDto.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
