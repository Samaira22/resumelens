package com.resumelens.engine;

import com.resumelens.model.JobRequirements;
import com.resumelens.model.MatchResult;
import com.resumelens.model.ScoreReport;

import java.util.Set;

public class ScoreCalculator {

    private static final double REQUIRED_WEIGHT = 0.75;
    private static final double PREFERRED_WEIGHT = 0.25;

    private final SkillMatcher matcher;

    public ScoreCalculator(SkillMatcher matcher) {
        this.matcher = matcher;
    }

    public ScoreReport calculate(Set<String> resumeSkills, JobRequirements job) {
        MatchResult required = matcher.match(resumeSkills, job.required());
        MatchResult preferred = matcher.match(resumeSkills, job.preferred());

        boolean hasRequired = required.totalRequired() > 0;
        boolean hasPreferred = preferred.totalRequired() > 0;

        double s;
        if (hasRequired && hasPreferred) {
            s = REQUIRED_WEIGHT * required.coverage() + PREFERRED_WEIGHT * preferred.coverage();
        } else if (hasRequired) {
            s = required.coverage();
        } else if (hasPreferred) {
            s = preferred.coverage();
        } else {
            s = 0.0;
        }
        return new ScoreReport(required, preferred, s);
    }
}
