package dev.irvanev.domain.config;

import dev.irvanev.domain.services.StudentServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class GrpcConfiguration implements CommandLineRunner {

    private final StudentServiceImpl studentService;

    @Autowired
    public GrpcConfiguration(StudentServiceImpl studentService) {
        this.studentService = studentService;
    }

    @Override
    public void run(String... args) throws Exception {
        Server server = ServerBuilder.forPort(8080)
                .addService(studentService)
                .build();

        server.start();
        System.out.println("Server started, listening on " + server.getPort());
        server.awaitTermination();
    }
}
