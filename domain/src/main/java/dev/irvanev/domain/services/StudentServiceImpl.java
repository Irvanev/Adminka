package dev.irvanev.domain.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.irvanev.domain.models.StudentDto;
import dev.irvanev.domain.models.StudentEntity;
import dev.irvanev.domain.repositories.StudentRepository;
import dev.irvanev.grpc.StudentProto.*;
import dev.irvanev.grpc.StudentServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentServiceImpl extends StudentServiceGrpc.StudentServiceImplBase {

    private final StudentRepository studentRepository;
    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository, RabbitTemplate rabbitTemplate) {
        this.studentRepository = studentRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void createStudent(Student request, StreamObserver<StudentResponse> responseObserver) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(new StudentDto(
                    null,
                    request.getFirstName(),
                    request.getLastName(),
                    request.getAge(),
                    request.getMajor()
            ));

            System.out.println("Sending JSON message: " + jsonMessage);

            rabbitTemplate.convertAndSend("students.exchange", "students.key", jsonMessage);

            StudentResponse response = StudentResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Student creation request sent for processing")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

    @Override
    public void getStudentById(StudentRequest request, StreamObserver<Student> responseObserver) {
        Optional<StudentEntity> studentOpt = studentRepository.findById(request.getId());
        if (studentOpt.isPresent()) {
            Student student = Student.newBuilder()
                    .setId(studentOpt.get().getId())
                    .setFirstName(studentOpt.get().getFirstName())
                    .setLastName(studentOpt.get().getLastName())
                    .setAge(studentOpt.get().getAge())
                    .setMajor(studentOpt.get().getMajor())
                    .build();
            responseObserver.onNext(student);
        } else {
            responseObserver.onError(new RuntimeException("Student not found"));
        }
        responseObserver.onCompleted();
    }

    @Override
    public void listStudents(Empty request, StreamObserver<StudentListResponse> responseObserver) {

        List<StudentEntity> students = studentRepository.findAll();

        StudentListResponse.Builder responseBuilder = StudentListResponse.newBuilder();

        for (StudentEntity studentEntity : students) {
            Student student = Student.newBuilder()
                    .setId(studentEntity.getId())
                    .setFirstName(studentEntity.getFirstName())
                    .setLastName(studentEntity.getLastName())
                    .setAge(studentEntity.getAge())
                    .setMajor(studentEntity.getMajor())
                    .build();
            responseBuilder.addStudents(student);
        }

        StudentListResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateStudent(Student request, StreamObserver<StudentResponse> responseObserver) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(new StudentDto(
                    request.getId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getAge(),
                    request.getMajor()
            ));

            System.out.println("Sending JSON update message: " + jsonMessage);

            rabbitTemplate.convertAndSend("students.exchange", "students.key", jsonMessage);

            StudentResponse response = StudentResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Student update request sent for processing")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }

    @Override
    public void deleteStudent(StudentRequest request, StreamObserver<StudentResponse> responseObserver) {
        try {
            // Преобразование объекта StudentRequest в JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonMessage = objectMapper.writeValueAsString(new StudentDto(
                    request.getId(),
                    null,
                    null,
                    0,
                    null
            ));

            // Лог для проверки
            System.out.println("Sending JSON delete message: " + jsonMessage);

            rabbitTemplate.convertAndSend("students.exchange", "students.key", jsonMessage);

            StudentResponse response = StudentResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Student deletion request sent for processing")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        }
    }
}
