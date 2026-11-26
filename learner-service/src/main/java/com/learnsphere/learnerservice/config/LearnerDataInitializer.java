package com.learnsphere.learnerservice.config;

import com.learnsphere.learnerservice.entity.Learner;
import com.learnsphere.learnerservice.repository.LearnerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LearnerDataInitializer implements CommandLineRunner {

    private final LearnerRepository learnerRepository;

    public LearnerDataInitializer(LearnerRepository learnerRepository) {
        this.learnerRepository = learnerRepository;
    }

    @Override
    public void run(String... args) {
        if (learnerRepository.count() == 0) {
            Learner learner1 = new Learner("Alice", "Smith", "alice.smith@example.com", "555-0101");
            learner1.addCourseId(101L);
            learnerRepository.save(learner1);

            Learner learner2 = new Learner("Bob", "Jones", "bob.jones@example.com", "555-0102");
            learnerRepository.save(learner2);
        }
    }
}
