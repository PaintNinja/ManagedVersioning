package dev.lukebemish.managedversioning.actions;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public abstract class Job {
    @Input
    public abstract Property<String> getName();
    @Nested
    public abstract ListProperty<Step> getSteps();
    @Input
    public abstract Property<String> getRunsOn();
    @Input
    public abstract MapProperty<String, String> getPermissions();
    @Input
    @Optional
    public abstract Property<String> getIf();

    private final ObjectFactory objectFactory;

    @Inject
    public Job(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
        this.getRunsOn().convention("ubuntu-22.04");
    }

    public Step step(Action<Step> action) {
        Step step = objectFactory.newInstance(Step.class, objectFactory);
        action.execute(step);
        this.getSteps().add(step);
        return step;
    }

    Object resolve() {
        Map<String, Object> job = new HashMap<>();
        job.put("runs-on", this.getRunsOn().get());
        job.put("steps", this.getSteps().get().stream().map(Step::resolve).toList());
        if (this.getIf().isPresent()) {
            job.put("if", this.getIf().get());
        }
        if (!this.getPermissions().get().isEmpty()) {
            job.put("permissions", this.getPermissions().get());
        }
        return job;
    }

    public Job configure(Action<Job> action) {
        action.execute(this);
        return this;
    }
}