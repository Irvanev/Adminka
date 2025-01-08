package dev.irvanev.gateway;

import dev.irvanev.grpc.StudentProto;
import dev.irvanev.grpc.StudentServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    private final StudentServiceGrpc.StudentServiceBlockingStub stub;

    public StudentController() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("domain-service", 8080)
                .usePlaintext()
                .build();
        this.stub = StudentServiceGrpc.newBlockingStub(channel);
    }

    @PostMapping
    @CacheEvict(value = {"studentsList", "students"}, allEntries = true)
    public String createStudent(@RequestBody StudentRequestDto studentRequest) {
        log.info("Creating student {}", studentRequest);
        StudentProto.Student request = StudentProto.Student.newBuilder()
                .setFirstName(studentRequest.getFirstName())
                .setLastName(studentRequest.getLastName())
                .setAge(studentRequest.getAge())
                .setMajor(studentRequest.getMajor())
                .build();

        StudentProto.StudentResponse response = stub.createStudent(request);
        return response.getMessage();
    }

    @GetMapping("/{id}")
    @Cacheable(value = "students", key = "#id", unless = "#result == null")
    public StudentDto getStudentById(@PathVariable String id) {
        log.info("Getting student {}", id);
        try {
            System.out.println("Fetching data without cache...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted", e);
        }
        StudentProto.StudentRequest request = StudentProto.StudentRequest.newBuilder().setId(id).build();
        StudentProto.Student response = stub.getStudentById(request);

        return new StudentDto(response.getId(), response.getFirstName(), response.getLastName(),
                response.getAge(), response.getMajor());
    }

    @GetMapping
    @Cacheable(value = "studentsList", unless = "#result == null || #result.isEmpty()")
    public List<StudentDto> listStudents() {
        log.info("Listing students...");
        try {
            System.out.println("Fetching data without cache...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread interrupted", e);
        }
        StudentProto.Empty request = StudentProto.Empty.newBuilder().build();
        StudentProto.StudentListResponse response = stub.listStudents(request);

        List<StudentDto> students = new ArrayList<>();
        for (StudentProto.Student student : response.getStudentsList()) {
            students.add(new StudentDto(student.getId(), student.getFirstName(), student.getLastName(), student.getAge(), student.getMajor()));
        }

        return students;
    }

    @PutMapping("/{id}")
    @CacheEvict(value = {"studentsList", "students"}, allEntries = true)
    public String updateStudent(@PathVariable String id, @RequestBody StudentRequestDto studentRequest) {
        log.info("Updating student {}", id);
        StudentProto.Student request = StudentProto.Student.newBuilder()
                .setId(id)
                .setFirstName(studentRequest.getFirstName())
                .setLastName(studentRequest.getLastName())
                .setAge(studentRequest.getAge())
                .setMajor(studentRequest.getMajor())
                .build();

        StudentProto.StudentResponse response = stub.updateStudent(request);

        return response.getMessage();
    }

    @DeleteMapping("/{id}")
    @CacheEvict(value = {"studentsList", "students"}, allEntries = true)
    public String deleteStudent(@PathVariable String id) {
        log.info("Deleting student {}", id);
        StudentProto.StudentRequest request = StudentProto.StudentRequest.newBuilder().setId(id).build();
        StudentProto.StudentResponse response = stub.deleteStudent(request);
        return response.getMessage();
    }
}
